package org.aion.avm.core;

import org.aion.kernel.*;

import i.IInstrumentation;
import i.IInstrumentationFactory;
import i.InstrumentationHelpers;
import i.JvmError;
import i.RuntimeAssertionError;

import org.aion.parallel.TransactionTask;


/**
 * The thread specific to the AVM internals.  All smart contract code is executed on one of these threads and each of them is directly owned by AvmImpl.
 */
public class AvmExecutorThread extends Thread {
    public static AvmExecutorThread currentThread() {
        try {
            return (AvmExecutorThread) Thread.currentThread();
        } catch (ClassCastException e) {
            // We do not support calling this on the wrong kind of thread.
            throw RuntimeAssertionError.unexpected(e);
        }
    }


    public final AvmThreadStats stats;

    private final IExecutorThreadHandler threadHandler;
    private final IInstrumentationFactory instrumentationFactory;
    private final boolean enableVerboseConcurrentExecutor;

    public AvmExecutorThread(String name
            , IExecutorThreadHandler threadHandler
            , IInstrumentationFactory instrumentationFactory
            , boolean enableVerboseConcurrentExecutor
    ) {
        super(name);
        this.stats = new AvmThreadStats();
        this.threadHandler = threadHandler;
        this.instrumentationFactory = instrumentationFactory;
        this.enableVerboseConcurrentExecutor = enableVerboseConcurrentExecutor;
    }

    @Override
    public void run() {
        IInstrumentation instrumentation = this.instrumentationFactory.createInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
        try {
            // Run as long as we have something to do (null means shutdown).
            AvmWrappedTransactionResult outgoingResult = null;
            long nanosRunningStop = System.nanoTime();
            long nanosSleepingStart = nanosRunningStop;
            TransactionTask incomingTask = this.threadHandler.blockingPollForTransaction(null, null);
            long nanosRunningStart = System.nanoTime();
            long nanosSleepingStop = nanosRunningStart;
            this.stats.nanosSleeping += (nanosSleepingStop - nanosSleepingStart);

            while (null != incomingTask) {
                int abortCounter = 0;

                do {
                    if (this.enableVerboseConcurrentExecutor) {
                        System.out.println(this.getName() + " start  " + incomingTask.getIndex());
                    }

                    // Attach the IInstrumentation helper to the task to support asynchronous abort
                    // Instrumentation helper will abort the execution of the transaction by throwing an exception during chargeEnergy call
                    // Aborted transaction will be retried later
                    incomingTask.startNewTransaction();
                    incomingTask.attachInstrumentationForThread();
                    outgoingResult = this.threadHandler.backgroundProcessTransaction(incomingTask);
                    incomingTask.detachInstrumentationForThread();

                    if (outgoingResult.isAborted()) {
                        // If this was an abort, we want to clear the abort state on the instrumentation for this thread, since
                        // this is the point where that is "handled".
                        // Note that this is safe to do here since the instrumentation isn't exposed to any other threads.
                        instrumentation.clearAbortState();
                        
                        if (AvmExecutorThread.this.enableVerboseConcurrentExecutor) {
                            System.out.println(this.getName() + " abort  " + incomingTask.getIndex() + " counter " + (++abortCounter));
                        }
                    }
                } while (outgoingResult.isAborted());

                if (AvmExecutorThread.this.enableVerboseConcurrentExecutor) {
                    System.out.println(this.getName() + " finish " + incomingTask.getIndex() + " " + outgoingResult);
                }

                this.stats.transactionsProcessed += 1;
                nanosRunningStop = System.nanoTime();
                nanosSleepingStart = nanosRunningStop;
                this.stats.nanosRunning += (nanosRunningStop - nanosRunningStart);
                incomingTask = this.threadHandler.blockingPollForTransaction(outgoingResult, incomingTask);
                nanosRunningStart = System.nanoTime();
                nanosSleepingStop = nanosRunningStart;
                this.stats.nanosSleeping += (nanosSleepingStop - nanosSleepingStart);
            }
        } catch (JvmError e) {
            // This is a fatal error the AVM cannot generally happen so request an asynchronous shutdown.
            // We set the backgroundException without lock since any concurrently-written exception instance is equally valid.
            this.threadHandler.setBackgroundFatalError(e);
        } catch (Throwable t) {
            // Note that this case is primarily only relevant for unit tests or other new development which could cause internal exceptions.
            // Without this hand-off to the foreground thread, these exceptions would cause silent failures.
            // Uncaught exception - this is fatal but we need to communicate it to the outside.
            this.threadHandler.setBackgroundFatalThrowable(t);
        } finally {
            InstrumentationHelpers.detachThread(instrumentation);
            this.instrumentationFactory.destroyInstrumentation(instrumentation);
        }
    }


    /**
     * The interface the AvmImpl implements in order to expose its functionality to these threads without them knowing directly about it.
     * (this allows us to remove a cycle in a logical dependency graph but also allows the AvmImpl to not expose this via its public interface)
     */
    public interface IExecutorThreadHandler {
        /**
         * Blocks the calling thread until work is available or a shutdown request is issued.
         * 
         * @param previousResult The result of the previously completed task, to pass back to anyone waiting.
         * @param previousTask The task returned by the previous call to this (corresponds to previousResult).
         * @return The next work item to execute or null if a shutdown has been requested.
         */
        TransactionTask blockingPollForTransaction(AvmWrappedTransactionResult previousResult, TransactionTask previousTask);
        /**
         * Asks for the given incomingTask to be executed.
         * Note that this is distinct from blockingPollForTransaction since the logic loop containing them is implemented here
         * even though both pieces of machinery are implemented in the receiver.
         * 
         * @param incomingTask The task to process.
         * @return The result of the call (never null).
         */
        AvmWrappedTransactionResult backgroundProcessTransaction(TransactionTask incomingTask);
        /**
         * Called when an unexpected throwable is experienced during a thread's lifetime.
         * An exception here would represent a bug in the AVM.
         * 
         * @param backgroundFatalThrowable The throwable which couldn't be handled.
         */
        void setBackgroundFatalThrowable(Throwable backgroundFatalThrowable);
        /**
         * Called when a fatal JVM error is experienced during a thread's lifetime.
         * Such an error represents a problem with the execution environment or JVM state.
         * 
         * @param backgroundFatalError The JVM error.
         */
        void setBackgroundFatalError(JvmError backgroundFatalError);
    }
}

package org.aion.avm.core;

import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.SimpleFuture;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionResult;


/**
 * Used by the AvmImpl to manage communication between its internal execution thread and the external calling thread.
 * This just provides monitor-protected blocking input/output variables, exception handling, and a safe way to shutdown.
 * Note that once an instance of this has been shutdown, it can't be started back up.
 * 
 * NOTE:  This currently assumes only one external thread is interacting with it at any given time.  This means that
 * attempting to send transactions from multiple threads or shutdown with one thread while running a transaction on another
 * would result in undefined behaviour.
 */
public class HandoffMonitor {
    private Thread internalThread;
    private TransactionContext incomingTransaction;
    private TransactionResult outgoingResult;
    private Throwable backgroundThrowable;

    public HandoffMonitor(Thread thread) {
        this.internalThread = thread;
    }

    /**
     * Called by the external thread.
     * Called to send a new transaction to the internal thread and block until it returns a result.
     * 
     * @param newTransaction The new transaction to pass in.
     * @return The result of newTransaction as an asynchronous future.
     */
    public synchronized SimpleFuture<TransactionResult> sendTransactionAsynchronously(TransactionContext newTransaction) {
        // We lock-step these, so there can't already be a transaction in the hand-off.
        RuntimeAssertionError.assertTrue(null == this.incomingTransaction);
        RuntimeAssertionError.assertTrue(null == this.outgoingResult);
        // Also, we can't have already been shut down.
        if (null == this.internalThread) {
            throw new IllegalStateException("Thread already stopped");
        }
        
        // Set the new transaction and wake up the background thread.
        this.incomingTransaction = newTransaction;
        this.notifyAll();
        
        // Return the future result, which will do the waiting for us.
        return new ResultWaitFuture();
    }

    public synchronized TransactionResult blockingConsumeResult() {
        // Wait until we have the result or something went wrong.
        while ((null == this.outgoingResult) && (null == this.backgroundThrowable)) {
            // Throw an exception, if there is one.
            handleThrowable();
            // Otherwise, wait until state changes.
            try {
                this.wait();
            } catch (InterruptedException e) {
                // We don't use interruption.
                RuntimeAssertionError.unexpected(e);
            }
        }
        
        // Consume the result and return it.
        TransactionResult result = this.outgoingResult;
        this.outgoingResult = null;
        return result;
    }

    /**
     * Called by the internal thread.
     * The main blocking point for the internal thread.  It passes in the result from the last transaction it just completed
     * and then waits until a new transaction comes in or a shutdown is requested.
     * 
     * @param previousResult The result of the previous transaction returned by this call.
     * @return The next transaction to run or null if we should shut down.
     */
    public synchronized TransactionContext blockingPollForTransaction(TransactionResult previousResult) {
        // We are lock-step with the foreground so nothing can be still waiting for the foreground to consume.
        RuntimeAssertionError.assertTrue(null == this.outgoingResult);
        
        // Set the result (may be null) and notify the foreground.
        this.outgoingResult = previousResult;
        this.notifyAll();
        
        // Wait until we have been given the next transaction or told to terminate.
        while ((null != this.internalThread) && (null == this.incomingTransaction)) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // We don't use interruption.
                RuntimeAssertionError.unexpected(e);
            }
        }
        
        // Return the next transaction (might be null if we were told to shut down).
        TransactionContext nextTransaction = this.incomingTransaction;
        this.incomingTransaction = null;
        return nextTransaction;
    }

    /**
     * Called by the internal thread.
     * This is called if something goes wrong while running the transaction on the internal thread to communicate this problem to the external.
     * 
     * @param throwable The exception (expected to be RuntimeException or Error).
     */
    public synchronized void setBackgroundThrowable(Throwable throwable) {
        // This will terminate anything the foreground is doing so notify them.
        this.backgroundThrowable = throwable;
        this.notifyAll();
    }

    /**
     * Called by the external thread.
     * Requests that the internal thread stop.  Only returns once the internal thread has terminated.
     */
    public void stopAndWaitForShutdown() {
        // (called by the foreground thread)
        // Stop the thread and wait for it to join.
        Thread backgroundThread = null;
        synchronized (this) {
            backgroundThread = this.internalThread;
            this.internalThread = null;
            this.notifyAll();
        }
        
        // Join on the thread and throw any exceptions left over.
        // (note that we can't join under monitor since the thread needs the monitor to exit).
        try {
            backgroundThread.join();
        } catch (InterruptedException e) {
            // We don't use interruption.
            RuntimeAssertionError.unexpected(e);
        }
        handleThrowable();
    }


    /**
     * Called by the external thread.
     */
    private void handleThrowable() {
        // WARNING:  This is not always called under monitor but this should be safe so long as backgroundThrowable saturates to non-null.
        if (null != this.backgroundThrowable) {
            // Only RuntimeExceptions and Errors can actually be handled here.
            try {
                throw this.backgroundThrowable;
            } catch (RuntimeException e) {
                throw e;
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                // This can't happen since we only store those 2.
                RuntimeAssertionError.unexpected(t);
            }
        }
    }


    private class ResultWaitFuture implements SimpleFuture<TransactionResult> {
        // We will cache the result.
        private TransactionResult cachedResult;
        @Override
        public TransactionResult get() {
            if (null == this.cachedResult) {
                this.cachedResult = HandoffMonitor.this.blockingConsumeResult();
            }
            return this.cachedResult;
        }
    }
}

package org.aion.avm.core;

import org.aion.avm.core.persistence.IObjectGraphStore;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.persistence.keyvalue.KeyValueObjectGraph;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.SoftCache;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IInstrumentationFactory;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.JvmError;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.Transaction.Type;
import org.aion.parallel.AddressResourceMonitor;
import org.aion.parallel.TransactionTask;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.SimpleFuture;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.aion.avm.internal.RuntimeAssertionError.unexpected;


public class AvmImpl implements AvmInternal {

    private static final Logger logger = LoggerFactory.getLogger(AvmImpl.class);

    private final IInstrumentationFactory instrumentationFactory;

    // Long-lived state which is book-ended by the startup/shutdown calls.
    private static AvmImpl currentAvm;  // (only here for testing - makes sure that we properly clean these up between invocations)
    private SoftCache<ByteArrayWrapper, LoadedDApp> hotCache;
    private HandoffMonitor handoff;

    // Short-lived state which is reset for each batch of transaction request.
    private AddressResourceMonitor resourceMonitor;

    // Used in the case of a fatal JvmError in the background threads.  A shutdown() is the only option from this point.
    private AvmFailedException backgroundFatalError;

    private final int threadCount;
    private final boolean preserveDebuggability;
    private final boolean enableVerboseContractErrors;
    private final boolean enableVerboseConcurrentExecutor;

    public AvmImpl(IInstrumentationFactory instrumentationFactory, AvmConfiguration configuration) {
        this.instrumentationFactory = instrumentationFactory;
        // Make sure that the threadCount isn't totally invalid.
        if (configuration.threadCount < 1) {
            throw new IllegalArgumentException("Thread count must be a positive integer");
        }
        this.threadCount = configuration.threadCount;
        this.preserveDebuggability = configuration.preserveDebuggability;
        this.enableVerboseContractErrors = configuration.enableVerboseContractErrors;
        this.enableVerboseConcurrentExecutor = configuration.enableVerboseConcurrentExecutor;
    }

    private class AvmExecutorThread extends Thread{

        AvmExecutorThread(String name){
            super(name);
        }

        @Override
        public void run() {
            IInstrumentation instrumentation = AvmImpl.this.instrumentationFactory.createInstrumentation();
            InstrumentationHelpers.attachThread(instrumentation);
            try {
                // Run as long as we have something to do (null means shutdown).
                AvmTransactionResult outgoingResult = null;
                TransactionTask incomingTask = AvmImpl.this.handoff.blockingPollForTransaction(null, null);
                while (null != incomingTask) {
                    int abortCounter = 0;

                    do {
                        if (AvmImpl.this.enableVerboseConcurrentExecutor) {
                            System.out.println(this.getName() + " start  " + incomingTask.getIndex());
                        }

                        // TODO:  Determine if we can coalesce the IInstrumentation and TransactionTask to avoid this attach/detach.
                        incomingTask.startNewTransaction();
                        incomingTask.attachInstrumentationForThread();
                        outgoingResult = AvmImpl.this.backgroundProcessTransaction(incomingTask);
                        incomingTask.detachInstrumentationForThread();

                        if (AvmTransactionResult.Code.FAILED_ABORT == outgoingResult.getResultCode()) {
                            // If this was an abort, we want to clear the abort state on the instrumentation for this thread, since
                            // this is the point where that is "handled".
                            // Note that this is safe to do here since the instrumentation isn't exposed to any other threads.
                            instrumentation.clearAbortState();
                            
                            if (AvmImpl.this.enableVerboseConcurrentExecutor) {
                                System.out.println(this.getName() + " abort  " + incomingTask.getIndex() + " counter " + (++abortCounter));
                            }
                        }
                    }while (AvmTransactionResult.Code.FAILED_ABORT == outgoingResult.getResultCode());

                    if (AvmImpl.this.enableVerboseConcurrentExecutor) {
                        System.out.println(this.getName() + " finish " + incomingTask.getIndex() + " " + outgoingResult.getResultCode());
                    }

                    incomingTask = AvmImpl.this.handoff.blockingPollForTransaction(outgoingResult, incomingTask);
                }
            } catch (JvmError e) {
                // This is a fatal error the AVM cannot generally happen so request an asynchronous shutdown.
                // We set the backgroundException without lock since any concurrently-written exception instance is equally valid.
                AvmFailedException backgroundFatalError = new AvmFailedException(e.getCause());
                AvmImpl.this.backgroundFatalError = backgroundFatalError;
                AvmImpl.this.handoff.setBackgroundThrowable(backgroundFatalError);
            } catch (Throwable t) {
                // Note that this case is primarily only relevant for unit tests or other new development which could cause internal exceptions.
                // Without this hand-off to the foreground thread, these exceptions would cause silent failures.
                // Uncaught exception - this is fatal but we need to communicate it to the outside.
                AvmImpl.this.handoff.setBackgroundThrowable(t);
            } finally {
                InstrumentationHelpers.detachThread(instrumentation);
                AvmImpl.this.instrumentationFactory.destroyInstrumentation(instrumentation);
            }
        }

    }

    @Override
    public void start() {
        RuntimeAssertionError.assertTrue(null == AvmImpl.currentAvm);
        AvmImpl.currentAvm = this;
        
        RuntimeAssertionError.assertTrue(null == this.hotCache);
        this.hotCache = new SoftCache<>();

        RuntimeAssertionError.assertTrue(null == this.resourceMonitor);
        this.resourceMonitor = new AddressResourceMonitor();

        Set<Thread> executorThreads = new HashSet<>();
        for (int i = 0; i < this.threadCount; i++){
            executorThreads.add(new AvmExecutorThread("AVM Executor Thread " + i));
        }

        RuntimeAssertionError.assertTrue(null == this.handoff);
        this.handoff = new HandoffMonitor(executorThreads);
        this.handoff.startExecutorThreads();
    }

    @Override
    public SimpleFuture<TransactionResult>[] run(KernelInterface kernel, TransactionContext[] transactions) throws IllegalStateException {
        if (null != this.backgroundFatalError) {
            throw this.backgroundFatalError;
        }
        // Clear the states of resources
        this.resourceMonitor.clear();

        // Clear the hot cache
        if (transactions.length > 0) {
            long currentBlockNum = transactions[0].getBlockNumber();
            validateCodeCache(currentBlockNum);
        }
        
        // Create tasks for these new transactions and send them off to be asynchronously executed.
        TransactionTask[] tasks = new TransactionTask[transactions.length];
        for (int i = 0; i < transactions.length; i++){
            tasks[i] = new TransactionTask(kernel, transactions[i], i);
        }

        return this.handoff.sendTransactionsAsynchronously(tasks);
    }

    private AvmTransactionResult backgroundProcessTransaction(TransactionTask task) {
        // to capture any error during validation
        AvmTransactionResult.Code error = null;

        RuntimeAssertionError.assertTrue(task != null);
        TransactionContext ctx = task.getExternalTransactionCtx();
        RuntimeAssertionError.assertTrue(ctx != null);

        // value/energyPrice/energyLimit sanity check
        if ((ctx.getTransferValue().compareTo(BigInteger.ZERO) < 0) || (ctx.getTransactionEnergyPrice() <= 0)) {
            error = AvmTransactionResult.Code.REJECTED;
        }
        
        if (ctx.getTransactionKind() == Type.CREATE.toInt()) {
            if (!task.getThisTransactionalKernel().isValidEnergyLimitForCreate(ctx.getTransaction().getEnergyLimit())) {
                error = AvmTransactionResult.Code.REJECTED;
            }
        } else {
            if (!task.getThisTransactionalKernel().isValidEnergyLimitForNonCreate(ctx.getTransaction().getEnergyLimit())) {
                error = AvmTransactionResult.Code.REJECTED;
            }
        }

        // Acquire both sender and target resources
        Address sender = ctx.getSenderAddress();
        Address target = (ctx.getTransactionKind() == Type.CREATE.toInt()) ? ctx.getContractAddress() : ctx.getDestinationAddress();

        this.resourceMonitor.acquire(sender.toBytes(), task);
        this.resourceMonitor.acquire(target.toBytes(), task);

        // nonce check
        if (!task.getThisTransactionalKernel().accountNonceEquals(sender, new BigInteger(ctx.getTransaction().getNonce()))) {
            error = AvmTransactionResult.Code.REJECTED_INVALID_NONCE;
        }

        AvmTransactionResult result = null;
        if (null == error) {
            // If this is a GC, we need to handle it specially.  Otherwise, use the common invoke path (handles both CREATE and CALL).
            if (ctx.getTransactionKind() == Type.GARBAGE_COLLECT.toInt()) {
                // The GC case operates directly on the top-level KernelInterface.
                // (remember that the "sender" is who we are updating).
                result = runGc(task.getThisTransactionalKernel(), sender, ctx);
            } else {
                // The CREATE/CALL case is handled via the common external invoke path.
                result = runExternalInvoke(task.getThisTransactionalKernel(), task, ctx);
            }
        } else {
            result = new AvmTransactionResult(ctx.getTransaction().getEnergyLimit(), ctx.getTransaction().getEnergyLimit());
            result.setResultCode(error);
        }

        // Task transactional kernel commits are serialized through address resource monitor
        if (!this.resourceMonitor.commitKernelForTask(task, result.getResultCode().isRejected())){
            result.setResultCode(AvmTransactionResult.Code.FAILED_ABORT);
        }

        if (AvmTransactionResult.Code.FAILED_ABORT != result.getResultCode()){
            result.setKernelInterface(task.getThisTransactionalKernel());
        }

        return result;
    }

    @Override
    public void shutdown() {
        // Note that we can fail due to either a RuntimeException or an Error, so catch either and be explicit about re-throwing.
        Error errorDuringShutdown = null;
        RuntimeException exceptionDuringShutdown = null;
        try {
            this.handoff.stopAndWaitForShutdown();
        } catch (RuntimeException e) {
            // Note that this is usually the same instance as backgroundFatalError can fail for other reasons.  Catch this, complete
            // the shutdown, then re-throw it.
            exceptionDuringShutdown = e;
        } catch (Error e) {
            // Same thing for Error.
            errorDuringShutdown = e;
        }
        this.handoff = null;
        RuntimeAssertionError.assertTrue(this == AvmImpl.currentAvm);
        AvmImpl.currentAvm = null;
        this.hotCache = null;
        
        // Note that we don't want to hide the background exception, if one happened, but we do want to complete the shutdown, so we do this at the end.
        if (null != errorDuringShutdown) {
            throw errorDuringShutdown;
        }
        if (null != exceptionDuringShutdown) {
            throw exceptionDuringShutdown;
        }
        if (null != this.backgroundFatalError) {
            throw this.backgroundFatalError;
        }
    }

    @Override
    public AvmTransactionResult runInternalTransaction(KernelInterface parentKernel, TransactionTask task, TransactionContext context) {
        if (null != this.backgroundFatalError) {
            throw this.backgroundFatalError;
        }
        return commonInvoke(parentKernel, task, context);
    }

    private AvmTransactionResult runExternalInvoke(KernelInterface parentKernel, TransactionTask task, TransactionContext ctx) {
        // to capture any error during validation
        AvmTransactionResult.Code error = null;

        // Sanity checks around energy pricing and nonce are done in the caller.
        // balance check
        Address sender = ctx.getSenderAddress();

        BigInteger transactionCost = BigInteger.valueOf(ctx.getTransaction().getEnergyLimit() * ctx.getTransactionEnergyPrice()).add(ctx.getTransferValue());
        if (!parentKernel.accountBalanceIsAtLeast(sender, transactionCost)) {
            error = AvmTransactionResult.Code.REJECTED_INSUFFICIENT_BALANCE;
        }

        // exit if validation check fails
        if (error != null) {
            AvmTransactionResult result = new AvmTransactionResult(ctx.getTransaction().getEnergyLimit(), ctx.getTransaction().getEnergyLimit());
            result.setResultCode(error);
            return result;
        }

        /*
         * After this point, no rejection should occur.
         */

        // Deduct the total energy cost
        parentKernel.adjustBalance(sender, BigInteger.valueOf(ctx.getTransaction().getEnergyLimit() * ctx.getTransactionEnergyPrice()).negate());

        // Run the common logic with the parent kernel as the top-level one.
        AvmTransactionResult result = commonInvoke(parentKernel, task, ctx);

        // Refund energy for transaction
        long energyRemaining = result.getEnergyRemaining() * ctx.getTransactionEnergyPrice();
        parentKernel.refundAccount(sender, BigInteger.valueOf(energyRemaining));

        // Transfer fees to miner
        parentKernel.adjustBalance(ctx.getMinerAddress(), BigInteger.valueOf(result.getEnergyUsed() * ctx.getTransactionEnergyPrice()));

        return result;
    }

    private AvmTransactionResult commonInvoke(KernelInterface parentKernel, TransactionTask task, TransactionContext ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Transaction: address = {}, caller = {}, value = {}, data = {}, energyLimit = {}",
                    ctx.getDestinationAddress(),
                    ctx.getSenderAddress(),
                    ctx.getTransferValue(),
                    Helpers.bytesToHexString(ctx.getTransactionData()),
                    ctx.getTransaction().getEnergyLimit());
        }
        // We expect that the GC transactions are handled specially, within the caller.
        RuntimeAssertionError.assertTrue(ctx.getTransactionKind() != Type.GARBAGE_COLLECT.toInt());

        // Invoke calls must build their transaction on top of an existing "parent" kernel.
        TransactionalKernel thisTransactionKernel = new TransactionalKernel(parentKernel);

        // only one result (mutable) shall be created per transaction execution
        AvmTransactionResult result = new AvmTransactionResult(ctx.getTransaction().getEnergyLimit(), ctx.getTransaction().getTransactionCost());

        // grab the recipient address as either the new contract address or the given account address.
        Address recipient = (ctx.getTransactionKind() == Type.CREATE.toInt()) ? ctx.getContractAddress() : ctx.getDestinationAddress();

        // conduct value transfer
        thisTransactionKernel.adjustBalance(ctx.getSenderAddress(), ctx.getTransferValue().negate());
        thisTransactionKernel.adjustBalance(recipient, ctx.getTransferValue());

        // At this stage, transaction can no longer be rejected.
        // The nonce increment will be done regardless of the transaction result.
        task.getThisTransactionalKernel().incrementNonce(ctx.getSenderAddress());

        // do nothing for balance transfers of which the recipient is not a DApp address.
        if (!((ctx.getTransactionKind() == Type.BALANCE_TRANSFER.toInt()) &&
                (null == thisTransactionKernel.getCode(recipient)
                || (null != thisTransactionKernel.getCode(recipient) && 0 == thisTransactionKernel.getCode(recipient).length)))) {
            if (ctx.getTransactionKind() == Type.CREATE.toInt()) { // create
                DAppCreator.create(thisTransactionKernel, this, task, ctx, result, this.preserveDebuggability, this.enableVerboseContractErrors);
            } else { // call
                // See if this call is trying to reenter one already on this call-stack.  If so, we will need to partially resume its state.
                ReentrantDAppStack.ReentrantState stateToResume = task.getReentrantDAppStack().tryShareState(recipient);

                LoadedDApp dapp;
                // The reentrant cache is obviously the first priority.
                // (note that we also want to check the kernel we were given to make sure that this DApp hasn't been deleted since we put it in the cache.
                if ((null != stateToResume) && (null != thisTransactionKernel.getCode(recipient))) {
                    dapp = stateToResume.dApp;
                    // Call directly and don't interact with DApp cache (we are reentering the state, not the origin of it).
                    DAppExecutor.call(thisTransactionKernel, this, dapp, stateToResume, task, ctx, result, this.enableVerboseContractErrors);
                } else {
                    // If we didn't find it there (that is only for reentrant calls so it is rarely found in the stack), try the hot DApp cache.
                    ByteArrayWrapper addressWrapper = new ByteArrayWrapper(recipient.toBytes());
                    dapp = this.hotCache.checkout(addressWrapper);
                    if (null == dapp) {
                        // If we didn't find it there, just load it.
                        try {
                            dapp = DAppLoader.loadFromGraph(new KeyValueObjectGraph(thisTransactionKernel, recipient).getCode(), this.preserveDebuggability);

                            // If the dapp is freshly loaded, we set the block num
                            if (null != dapp){
                                dapp.setLoadedBlockNum(ctx.getBlockNumber());
                            }

                        } catch (IOException e) {
                            unexpected(e); // the jar was created by AVM; IOException is unexpected
                        }
                    }
                    // Run the call and, if successful, check this into the hot DApp cache.
                    if (null != dapp) {
                        DAppExecutor.call(thisTransactionKernel, this, dapp, stateToResume, task, ctx, result, this.enableVerboseContractErrors);
                        if (AvmTransactionResult.Code.SUCCESS == result.getResultCode()) {
                            dapp.cleanForCache();
                            this.hotCache.checkin(addressWrapper, dapp);
                        }
                    }
                }
            }
        }

        if (result.getResultCode().isSuccess()) {
            thisTransactionKernel.commit();
        } else {
            ctx.getSideEffects().getExecutionLogs().clear();
            ctx.getSideEffects().markAllInternalTransactionsAsRejected();
        }

        logger.debug("Result: {}", result);
        return result;
    }

    private AvmTransactionResult runGc(KernelInterface parentKernel, Address dappAddress, TransactionContext ctx) {
        RuntimeAssertionError.assertTrue(ctx.getTransactionKind() == Type.GARBAGE_COLLECT.toInt());

        ByteArrayWrapper addressWrapper = new ByteArrayWrapper(dappAddress.toBytes());
        IObjectGraphStore graphStore = new KeyValueObjectGraph(parentKernel, dappAddress);
        
        LoadedDApp dapp = this.hotCache.checkout(addressWrapper);
        if (null == dapp) {
            // If we didn't find it there, just load it.
            try {
                dapp = DAppLoader.loadFromGraph(graphStore.getCode(), this.preserveDebuggability);

                // If the dapp is freshly loaded, we set the block num
                if (null != dapp){
                    dapp.setLoadedBlockNum(ctx.getBlockNumber());
                }

            } catch (IOException e) {
                unexpected(e); // the jar was created by AVM; IOException is unexpected
            }
        }

        // There is no concept of an energy limit for a GC transaction, we treat it as zero. This
        // also keeps the energy used / remaining distinction meaningful in this case.
        AvmTransactionResult result = new AvmTransactionResult(0, 0);

        if (null != dapp) {
            // Run the GC and check this into the hot DApp cache.
            long instancesFreed = graphStore.gc();
            this.hotCache.checkin(addressWrapper, dapp);
            // We want to set this to success and report the energy used as the refund found by the GC.
            // NOTE:  This is the total value of the refund as splitting that between the DApp and node is a higher-level decision.
            long storageEnergyRefund = instancesFreed * InstrumentationBasedStorageFees.DEPOSIT_WRITE_COST;
            result.setResultCode(AvmTransactionResult.Code.SUCCESS);
            result.setEnergyUsed(-storageEnergyRefund);
        } else {
            // If we failed to find the application, we will currently return this as a generic FAILED_INVALID but we may want a more
            // specific code in the future.
            result.setResultCode(AvmTransactionResult.Code.FAILED_INVALID);
            result.setEnergyUsed(0);
        }
        return result;
    }

    @Override
    public AddressResourceMonitor getResourceMonitor() {
        if (null != this.backgroundFatalError) {
            throw this.backgroundFatalError;
        }
        return resourceMonitor;
    }

    private void validateCodeCache(long blockNum){
        Predicate<SoftReference<LoadedDApp>> condition = (v) -> null != v.get() && v.get().getLoadedBlockNum() >= blockNum;
        this.hotCache.removeValueIf(condition);
    }
}

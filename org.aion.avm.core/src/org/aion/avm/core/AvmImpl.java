package org.aion.avm.core;

import org.aion.avm.core.util.TransactionResultUtil;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.kernel.*;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.ContractCaptureTool;
import org.aion.avm.core.util.SoftCache;
import i.IInstrumentationFactory;
import i.JvmError;
import i.RuntimeAssertionError;
import org.aion.parallel.AddressResourceMonitor;
import org.aion.parallel.TransactionTask;


public class AvmImpl implements AvmInternal {
    private InternalLogger internalLogger;

    private final IInstrumentationFactory instrumentationFactory;
    private final IExternalCapabilities capabilities;

    // Long-lived state which is book-ended by the startup/shutdown calls.
    private static AvmImpl currentAvm;  // (only here for testing - makes sure that we properly clean these up between invocations)
    private SoftCache<ByteArrayWrapper, LoadedDApp> hotCache;
    private SoftCache<ByteArrayWrapper, byte[]> transformedCodeCache;
    private HandoffMonitor handoff;

    // Short-lived state which is reset for each batch of transaction request.
    private AddressResourceMonitor resourceMonitor;

    // Shared references to the stats structure - created when threads are started (since their stats are also held here).
    private AvmCoreStats stats;

    // Used in the case of a fatal JvmError in the background threads.  A shutdown() is the only option from this point.
    private AvmFailedException backgroundFatalError;

    private final int threadCount;
    private final boolean preserveDebuggability;
    private final boolean enableVerboseContractErrors;
    private final boolean enableVerboseConcurrentExecutor;
    private final boolean enableBlockchainPrintln;
    private final HistogramDataCollector histogramDataCollector;
    private final ContractCaptureTool contractCaptureTool;
    
    // We will put an implementation of AvmExecutorThread.IExecutorThreadHandler inside, instead of implementing it, ourselves, to be clear
    // that this implementation is for the threads created internally, only, and not part of our generaly public interface.
    private final AvmExecutorThread.IExecutorThreadHandler executorThreadHandler;

    public AvmImpl(IInstrumentationFactory instrumentationFactory, IExternalCapabilities capabilities, AvmConfiguration configuration) {
        this.instrumentationFactory = instrumentationFactory;
        this.capabilities = capabilities;
        // Make sure that the threadCount isn't totally invalid.
        if (configuration.threadCount < 1) {
            throw new IllegalArgumentException("Thread count must be a positive integer");
        }
        this.threadCount = configuration.threadCount;
        this.preserveDebuggability = configuration.preserveDebuggability;
        this.enableVerboseContractErrors = configuration.enableVerboseContractErrors;
        this.enableVerboseConcurrentExecutor = configuration.enableVerboseConcurrentExecutor;
        this.enableBlockchainPrintln = configuration.enableBlockchainPrintln;
        this.internalLogger = new InternalLogger(System.err);
        this.histogramDataCollector = (null != configuration.deploymentDataHistorgramOutput)
                ? new HistogramDataCollector(configuration.deploymentDataHistorgramOutput)
                : null;
        this.contractCaptureTool = (null != configuration.contractCaptureDirectory)
                ? new ContractCaptureTool(configuration.contractCaptureDirectory)
                : null;
        
        this.executorThreadHandler = new AvmExecutorThread.IExecutorThreadHandler() {
            @Override
            public TransactionTask blockingPollForTransaction(AvmWrappedTransactionResult previousResult, TransactionTask previousTask) {
                return AvmImpl.this.handoff.blockingPollForTransaction(previousResult, previousTask);
            }
            @Override
            public AvmWrappedTransactionResult backgroundProcessTransaction(TransactionTask incomingTask) {
                return AvmImpl.this.backgroundProcessTransaction(incomingTask);
            }
            @Override
            public void setBackgroundFatalThrowable(Throwable backgroundFatalThrowable) {
                AvmImpl.this.handoff.setBackgroundThrowable(backgroundFatalThrowable);
            }
            @Override
            public void setBackgroundFatalError(JvmError backgroundFatalError) {
                AvmFailedException fatalError = new AvmFailedException(backgroundFatalError.getCause());
                AvmImpl.this.backgroundFatalError = fatalError;
                AvmImpl.this.handoff.setBackgroundThrowable(fatalError);
            }
        };
    }

    public void start() {
        // An AVM instance can only be started once so we shouldn't yet have stats.
        RuntimeAssertionError.assertTrue(null == this.stats);
        
        // There are currently no consumers which have more than 1 AVM instance running concurrently so we enforce this in order to flag static errors.
        RuntimeAssertionError.assertTrue(null == AvmImpl.currentAvm);
        AvmImpl.currentAvm = this;
        
        // See if we need to enable the contract capture.
        if (null != this.contractCaptureTool) {
            this.contractCaptureTool.startup();
        }
        
        RuntimeAssertionError.assertTrue(null == this.hotCache);
        RuntimeAssertionError.assertTrue(null == this.transformedCodeCache);
        this.hotCache = new SoftCache<>();
        this.transformedCodeCache = new SoftCache<>();

        RuntimeAssertionError.assertTrue(null == this.resourceMonitor);
        this.resourceMonitor = new AddressResourceMonitor();

        AvmThreadStats[] threadStats = new AvmThreadStats[this.threadCount];
        Set<Thread> executorThreads = new HashSet<>();
        for (int i = 0; i < this.threadCount; i++){
            AvmExecutorThread thread = new AvmExecutorThread("AVM Executor Thread " + i
                    , this.executorThreadHandler
                    , this.instrumentationFactory
                    , this.enableVerboseConcurrentExecutor
            );
            executorThreads.add(thread);
            threadStats[i] = thread.stats;
        }
        this.stats = new AvmCoreStats(threadStats);

        RuntimeAssertionError.assertTrue(null == this.handoff);
        this.handoff = new HandoffMonitor(executorThreads);
        this.handoff.startExecutorThreads();
    }

    public FutureResult[] run(IExternalState kernel, Transaction[] transactions, ExecutionType executionType, long commonMainchainBlockNumber) throws IllegalStateException {
        long currentBlockNum = kernel.getBlockNumber();

        if (transactions.length <= 0) {
            throw new IllegalArgumentException("Number of transactions must be larger than 0");
        }

        // validate commonMainchainBlockNumber based on execution type
        if (executionType == ExecutionType.ASSUME_MAINCHAIN || executionType == ExecutionType.ASSUME_SIDECHAIN || executionType == ExecutionType.MINING) {
            // This check generally true for mining but it's added for rare cases of mining on top of an imported block which is not the latest
            if (currentBlockNum != commonMainchainBlockNumber + 1) {
                throw new IllegalArgumentException("Invalid commonMainchainBlockNumber for " + executionType + " currentBlock = " + currentBlockNum + " , commonMainchainBlockNumber = " + commonMainchainBlockNumber);
            }
        } else if (executionType == ExecutionType.ASSUME_DEEP_SIDECHAIN && commonMainchainBlockNumber != 0) {
            throw new IllegalArgumentException("commonMainchainBlockNumber must be zero for " + executionType);
        }

        // validate cache based on execution type
        if (executionType == ExecutionType.ASSUME_MAINCHAIN || executionType == ExecutionType.MINING) {
            validateCodeCache(currentBlockNum);
        } else if (executionType == ExecutionType.SWITCHING_MAINCHAIN) {
            // commonMainchainBlockNumber is the last valid block so anything after that should be removed from the cache
            validateCodeCache(commonMainchainBlockNumber + 1);
            purgeDataCache();
        }

        cleanupTransformedCodeCache();

        if (null != this.backgroundFatalError) {
            throw this.backgroundFatalError;
        }
        // Clear the states of resources
        this.resourceMonitor.clear();

        // Create tasks for these new transactions and send them off to be asynchronously executed.
        TransactionTask[] tasks = new TransactionTask[transactions.length];
        for (int i = 0; i < transactions.length; i++){
            tasks[i] = new TransactionTask(kernel, transactions[i], i, transactions[i].senderAddress, executionType, commonMainchainBlockNumber);
        }

        this.stats.batchesConsumed += 1;
        this.stats.transactionsConsumed += transactions.length;
        return this.handoff.sendTransactionsAsynchronously(tasks);
    }

    public AvmCoreStats getStats() {
        return this.stats;
    }

    private AvmWrappedTransactionResult backgroundProcessTransaction(TransactionTask task) {
        // to capture any error during validation
        AvmInternalError error = AvmInternalError.NONE;

        RuntimeAssertionError.assertTrue(task != null);
        Transaction tx = task.getTransaction();
        RuntimeAssertionError.assertTrue(tx != null);

        // value/energyPrice/energyLimit sanity check
        BigInteger value = tx.value;
        if (value.compareTo(BigInteger.ZERO) < 0) {
            error = AvmInternalError.REJECTED_INVALID_VALUE;
        }
        if (tx.energyPrice <= 0) {
            error = AvmInternalError.REJECTED_INVALID_ENERGY_PRICE;
        }
        
        if (tx.isCreate) {
            if (!task.getThisTransactionalKernel().isValidEnergyLimitForCreate(tx.energyLimit)) {
                error = AvmInternalError.REJECTED_INVALID_ENERGY_LIMIT;
            }
        } else {
            if (!task.getThisTransactionalKernel().isValidEnergyLimitForNonCreate(tx.energyLimit)) {
                error = AvmInternalError.REJECTED_INVALID_ENERGY_LIMIT;
            }
        }

        // Acquire both sender and target resources
        // Note that we calculate the target, in the case of a create, only to acquire the resource lock and then discard it (will be logically recreated later when required).
        AionAddress sender = tx.senderAddress;
        AionAddress target = (tx.isCreate) ? capabilities.generateContractAddress(tx.senderAddress, tx.nonce) : tx.destinationAddress;

        AvmWrappedTransactionResult result = null;

        boolean isSenderAcquired = this.resourceMonitor.acquire(sender.toByteArray(), task);
        boolean isTargetAcquired = this.resourceMonitor.acquire(target.toByteArray(), task);

        if (isSenderAcquired && isTargetAcquired) {
            // nonce check
            if (!task.getThisTransactionalKernel().accountNonceEquals(sender, tx.nonce)) {
                error = AvmInternalError.REJECTED_INVALID_NONCE;
            }

            if (AvmInternalError.NONE == error) {
                // The CREATE/CALL case is handled via the common external invoke path.
                result = runExternalInvoke(task.getThisTransactionalKernel(), task, tx.senderAddress, tx.isCreate, (tx.isCreate ? null : tx.destinationAddress), tx.copyOfTransactionData(), tx.copyOfTransactionHash(), tx.energyLimit, tx.energyPrice, tx.value, tx.nonce);
            } else {
                result = TransactionResultUtil.newRejectedResultWithEnergyUsed(error, tx.energyLimit);
            }
        } else {
            result = TransactionResultUtil.newAbortedResultWithZeroEnergyUsed();
        }

        // Task transactional kernel commits are serialized through address resource monitor
        // This should be done for all transaction result cases, including FAILED_ABORT, because one of the addresses might have been acquired
        if (!this.resourceMonitor.commitKernelForTask(task, result.isRejected())) {
            // A transaction task can be aborted even after it has finished.
            result = TransactionResultUtil.newAbortedResultWithZeroEnergyUsed();
        }

        if (!result.isAborted()){
            result = TransactionResultUtil.setExternalState(result, task.getThisTransactionalKernel());
        }

        return result;
    }

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
        this.transformedCodeCache = null;
        
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
        if (null != this.histogramDataCollector) {
            this.histogramDataCollector.dumpData();
        }
    }

    @Override
    public AvmWrappedTransactionResult runInternalTransaction(IExternalState parentKernel
            , TransactionTask task
            , AionAddress senderAddress
            , boolean isCreate
            , AionAddress normalCallTarget  // Null if this is a create.
            , AionAddress effectiveTransactionOrigin
            , byte[] transactionData
            , byte[] transactionHash
            , long energyLimit
            , long energyPrice
            , BigInteger transactionValue
            , BigInteger nonce
    ) {
        if (null != this.backgroundFatalError) {
            throw this.backgroundFatalError;
        }
        RuntimeAssertionError.assertTrue(!task.isSideEffectsStackEmpty());
        task.pushSideEffects(new SideEffects());
        AvmWrappedTransactionResult result = commonInvoke(parentKernel, task, senderAddress, isCreate, normalCallTarget, effectiveTransactionOrigin, transactionData, transactionHash, energyLimit, energyPrice, transactionValue, nonce, 0);
        SideEffects txSideEffects = task.popSideEffects();
        if (!result.isSuccess()) {
            txSideEffects.getExecutionLogs().clear();
            // unsuccessful transaction result can either be due to an error or an abort case. In abort case the rejection status will be overridden.
            txSideEffects.markAllInternalTransactionsAsRejected();
        }
        task.peekSideEffects().merge(txSideEffects);
        return result;
    }

    private AvmWrappedTransactionResult runExternalInvoke(IExternalState parentKernel
            , TransactionTask task
            , AionAddress senderAddress
            , boolean isCreate
            , AionAddress normalCallTarget  // Null if this is a create.
            , byte[] transactionData
            , byte[] transactionHash
            , long energyLimit
            , long energyPrice
            , BigInteger transactionValue
            , BigInteger nonce
    ) {
        // to capture any error during validation
        AvmInternalError error = AvmInternalError.NONE;

        // Sanity checks around energy pricing and nonce are done in the caller.
        // balance check
        long basicTransactionCost = BillingRules.getBasicTransactionCost(transactionData);
        BigInteger balanceRequired = BigInteger.valueOf(energyLimit).multiply(BigInteger.valueOf(energyPrice)).add(transactionValue);

        if (basicTransactionCost > energyLimit) {
            error = AvmInternalError.REJECTED_INVALID_ENERGY_LIMIT;
        }
        else if (!parentKernel.accountBalanceIsAtLeast(senderAddress, balanceRequired)) {
            error = AvmInternalError.REJECTED_INSUFFICIENT_BALANCE;
        }

        // exit if validation check fails
        if (error != AvmInternalError.NONE) {
            return TransactionResultUtil.newRejectedResultWithEnergyUsed(error, energyLimit);
        }

        /*
         * After this point, no rejection should occur.
         */

        // Deduct the total energy cost
        parentKernel.adjustBalance(senderAddress, BigInteger.valueOf(energyLimit).multiply(BigInteger.valueOf(energyPrice).negate()));

        // Run the common logic with the parent kernel as the top-level one.
        // (externally-originating transactions use sender as origin)
        AvmWrappedTransactionResult result = commonInvoke(parentKernel, task, senderAddress, isCreate, normalCallTarget, senderAddress, transactionData, transactionHash, energyLimit, energyPrice, transactionValue, nonce, basicTransactionCost);

        // Refund energy for transaction
        BigInteger refund = BigInteger.valueOf(energyLimit - result.energyUsed()).multiply(BigInteger.valueOf(energyPrice));
        parentKernel.refundAccount(senderAddress, refund);

        // Transfer fees to miner
        parentKernel.adjustBalance(parentKernel.getMinerAddress(), BigInteger.valueOf(result.energyUsed()).multiply(BigInteger.valueOf(energyPrice)));

        if (!result.isSuccess()) {
            task.peekSideEffects().getExecutionLogs().clear();
            // unsuccessful transaction result can either be due to an error or an abort case. In abort case the rejection status will be overridden.
            task.peekSideEffects().markAllInternalTransactionsAsRejected();
        }

        return result;
    }

    private AvmWrappedTransactionResult commonInvoke(IExternalState parentKernel
            , TransactionTask task
            , AionAddress senderAddress
            , boolean isCreate
            , AionAddress normalCallTarget  // Null if this is a create.
            , AionAddress effectiveTransactionOrigin
            , byte[] transactionData
            , byte[] transactionHash
            , long energyLimit
            , long energyPrice
            , BigInteger transactionValue
            , BigInteger nonce
            , long transactionBaseCost
    ) {
        // A create MUST provide a null target and non-creates must NOT have null targets.
        RuntimeAssertionError.assertTrue(isCreate == (null == normalCallTarget));
        
        // Invoke calls must build their transaction on top of an existing "parent" kernel.
        TransactionalState thisTransactionKernel = new TransactionalState(parentKernel);

        AvmWrappedTransactionResult result = TransactionResultUtil.newSuccessfulResultWithEnergyUsed(transactionBaseCost);

        // grab the recipient address as either the new contract address or the given account address.
        AionAddress recipient = isCreate ? capabilities.generateContractAddress(senderAddress, nonce) : normalCallTarget;

        // conduct value transfer
        thisTransactionKernel.adjustBalance(senderAddress, transactionValue.negate());
        thisTransactionKernel.adjustBalance(recipient, transactionValue);

        // At this stage, transaction can no longer be rejected.
        // The nonce increment will be done regardless of the transaction result so increment it in the parent kernel.
        parentKernel.incrementNonce(senderAddress);

        // do nothing for balance transfers of which the recipient is not a DApp address.
        if (isCreate) {
            if ((null != this.histogramDataCollector) || (null != this.contractCaptureTool)) {
                CodeAndArguments codeAndArguments = CodeAndArguments.decodeFromBytes(transactionData);
                // If this data is invalid, we will get null.  We don't bother tracking this.
                if (null != codeAndArguments) {
                    if (null != this.histogramDataCollector) {
                        this.histogramDataCollector.collectDataFromJarBytes(codeAndArguments.code);
                    }
                    if (null != this.contractCaptureTool) {
                        this.contractCaptureTool.captureDeployment(parentKernel.getBlockNumber(), senderAddress, recipient, nonce, codeAndArguments.code, codeAndArguments.arguments);
                    }
                }
            }
            result = DAppCreator.create(this.capabilities, thisTransactionKernel, this, task, senderAddress, recipient, effectiveTransactionOrigin, transactionData, transactionHash, energyLimit, energyPrice, transactionValue, result, this.preserveDebuggability, this.enableVerboseContractErrors, this.enableBlockchainPrintln);
        } else { // call
            // See if this call is trying to reenter one already on this call-stack.  If so, we will need to partially resume its state.
            ReentrantDAppStack.ReentrantState stateToResume = task.getReentrantDAppStack().tryShareState(recipient);

            LoadedDApp dapp = null;
            byte[] transformedCode = thisTransactionKernel.getTransformedCode(recipient);
            // The reentrant cache is obviously the first priority.
            // (note that we also want to check the kernel we were given to make sure that this DApp hasn't been deleted since we put it in the cache.
            if ((null != stateToResume) && (null != transformedCode)) {
                dapp = stateToResume.dApp;
                // Call directly and don't interact with DApp cache (we are reentering the state, not the origin of it).
                result = DAppExecutor.call(this.capabilities, thisTransactionKernel, this, dapp, stateToResume, task, senderAddress, recipient, effectiveTransactionOrigin, transactionData, transactionHash, energyLimit, energyPrice, transactionValue, result, this.enableVerboseContractErrors, true, this.enableBlockchainPrintln);
            } else {
                long currentBlockNumber = thisTransactionKernel.getBlockNumber();

                // If we didn't find it there (that is only for reentrant calls so it is rarely found in the stack), try the hot DApp cache.
                ByteArrayWrapper addressWrapper = new ByteArrayWrapper(recipient.toByteArray());
                LoadedDApp dappInHotCache = null;

                // This reflects if the dapp should be checked back into either cache after transaction execution
                boolean writeToCacheEnabled;

                // This reflects if the DApp data can be loaded from the cache.
                // It is used in DAppExecutor to determine whether to load the data by making a call to the database or from DApp object
                boolean readFromDataCacheEnabled;

                boolean updateDataCache;

                // We don't write back the data to the cache for internal transactions, since the external transaction might fail
                boolean isExternalTransaction = task.getTransactionStackDepth() == 0;

                // writes the transformed code into the cache, only when the recipient dApp does not have an associated transformed code in the database and the transaction fails.
                boolean writeToTransformedCodeCache = false;
                byte[] cachedTransformedCode = null;

                // there are no interactions with either cache in ASSUME_DEEP_SIDECHAIN
                if (task.executionType != ExecutionType.ASSUME_DEEP_SIDECHAIN) {
                    dappInHotCache = this.hotCache.checkout(addressWrapper);
                    if(transformedCode == null){
                        cachedTransformedCode = this.transformedCodeCache.checkout(addressWrapper);
                    }
                }

                if (task.executionType == ExecutionType.ASSUME_MAINCHAIN || task.executionType == ExecutionType.SWITCHING_MAINCHAIN) {
                    // cache has been validated for these two types before getting here
                    writeToCacheEnabled = true;
                    readFromDataCacheEnabled = dappInHotCache != null && dappInHotCache.hasValidCachedData(currentBlockNumber);
                    updateDataCache = true;
                } else if (task.executionType == ExecutionType.ASSUME_SIDECHAIN || task.executionType == ExecutionType.ETH_CALL) {
                    if (dappInHotCache != null) {
                        // Check if the code is valid at this height. The last valid block for code cache is the CommonMainchainBlockNumber
                        if (!dappInHotCache.hasValidCachedCode(task.commonMainchainBlockNumber + 1)) {
                            // if we cannot use the cache, put the dapp back and work with the database
                            this.hotCache.checkin(addressWrapper, dappInHotCache);
                            writeToCacheEnabled = false;
                            dappInHotCache = null;
                        } else {
                            // only dapp code is written back to the cache.
                            // this is enabled only if the dapp has valid code cache at current height
                            writeToCacheEnabled = true;
                        }
                    } else {
                        // if the dapp could not be found in the cache, do not write to the cache
                        writeToCacheEnabled = false;
                    }
                    readFromDataCacheEnabled = dappInHotCache != null && dappInHotCache.hasValidCachedData(task.commonMainchainBlockNumber + 1);
                    updateDataCache = false;
                } else if (task.executionType == ExecutionType.ASSUME_DEEP_SIDECHAIN) {
                    writeToCacheEnabled = false;
                    readFromDataCacheEnabled = false;
                    updateDataCache = false;
                } else {
                    // ExecutionType.MINING
                    // if the dapp was already present in the cache, its code is written back to the cache.
                    writeToCacheEnabled = dappInHotCache != null;
                    readFromDataCacheEnabled = false;
                    updateDataCache = false;
                }

                if (transformedCode == null) {
                    byte[] code = thisTransactionKernel.getCode(recipient);
                    //'thisTransactionKernel.getCode(recipient) != null' means this recipient's DApp is not self-destructed.
                    if (code != null) {
                        // if the transformed code was not in the cache, re-transform the code
                        if (cachedTransformedCode == null) {
                            transformedCode = CodeReTransformer.transformCode(code, thisTransactionKernel.getBlockTimestamp(), this.preserveDebuggability, this.enableVerboseContractErrors);
                            if (transformedCode == null) {
                                // re-transformation failed. This dApp is no longer supported in the new version of AVM.
                                result = TransactionResultUtil.setNonRevertedFailureAndEnergyUsed(result, AvmInternalError.FAILED_RETRANSFORMATION, energyLimit);
                            }
                        } else {
                            transformedCode = cachedTransformedCode;
                        }

                        if (transformedCode != null) {
                            thisTransactionKernel.setTransformedCode(recipient, transformedCode);
                            /* Writing the transformed code into a cache is only done for failed transactions and follows a similar pattern to the code cache, but it's much simpler. Transformed code cache does not depend on block number
                            because any transformed code after the fork point is valid as long as the contract has not been self destructed. So for mainchain, sidechain, and switching mainchain blocks cache is read and updated if necessary.
                            Only if a side chain has been created before the fork point where the avm version changes, cache might become invalid. To prevent this, on the kernel side we ensure when the avm version changes, a new instance of
                            avm2.0 is run. Also, we don't rely on the cache if the block is in a deep side chain. For mining and eth_call blocks, we read the cache but only write if the transformed code was already stored in the cache.
                            */
                            if (task.executionType == ExecutionType.ASSUME_MAINCHAIN || task.executionType == ExecutionType.SWITCHING_MAINCHAIN || task.executionType == ExecutionType.ASSUME_SIDECHAIN) {
                                writeToTransformedCodeCache = true;
                            }
                        }
                        if (task.executionType == ExecutionType.MINING || task.executionType == ExecutionType.ETH_CALL) {
                            // Write the transformed code back to the cache (regardless of the transaction result), if it was there to begin with.
                            // Mining and eth_call blocks should not change the state of the transformed code cache.
                            if (cachedTransformedCode != null) {
                                this.transformedCodeCache.checkin(addressWrapper, cachedTransformedCode);
                            }
                        }
                    }
                } else {
                // do not use the cache if the code has not been transformed for the latest version
                    dapp = dappInHotCache;
                }
                if (null == dapp) {
                    // If we didn't find it there, just load it.
                    try {
                        dapp = DAppLoader.loadFromGraph(transformedCode, this.preserveDebuggability);

                        // If the dapp is freshly loaded, we set the block num
                        if (null != dapp){
                            dapp.setLoadedCodeBlockNum(currentBlockNumber);
                        }

                    } catch (IOException e) {
                        throw RuntimeAssertionError.unexpected(e); // the jar was created by AVM; IOException is unexpected
                    }
                }

                if (null != dapp) {
                    result = DAppExecutor.call(this.capabilities, thisTransactionKernel, this, dapp, stateToResume, task, senderAddress, recipient, effectiveTransactionOrigin, transactionData, transactionHash, energyLimit, energyPrice, transactionValue, result, this.enableVerboseContractErrors, readFromDataCacheEnabled, this.enableBlockchainPrintln);

                    if (writeToCacheEnabled) {
                        // Update the data cache only if
                        // the result was successful,
                        // the execution type was ASSUME_MAINCHAIN or SWITCHING_MAINCHAIN,
                        // and it was not an internal transaction
                        if (result.isSuccess() && updateDataCache && isExternalTransaction) {
                            dapp.updateLoadedBlockForSuccessfulTransaction(currentBlockNumber);
                            this.hotCache.checkin(addressWrapper, dapp);
                        } else {
                            // Update the code cache for ASSUME_SIDECHAIN, ETH_CALL, MINING cases, and internal transactions
                            dapp.clearDataState();
                            this.hotCache.checkin(addressWrapper, dapp);
                        }
                    }
                    // Only add the transformed code to the cache if the transaction has failed and thisTransactionKernel.getTransformedCode(recipient) = null.
                    // This means the transformed code was either successfully retrieved from the cache or the consensus code was successfully re-transformed.
                    // This is only done for ASSUME_MAINCHAIN, SWITCHING_MAINCHAIN, and ASSUME_SIDECHAIN
                    if (!result.isSuccess() && writeToTransformedCodeCache) {
                        this.transformedCodeCache.checkin(addressWrapper, transformedCode);
                    }
                }
            }
        }

        if (result.isSuccess()) {
            thisTransactionKernel.commit();
        } else if (result.isFailedUnexpected()) {
            internalLogger.logFatal(result.exception);
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
        // getLoadedDataBlockNum will always be either equal or less than getLoadedCodeBlockNum
        Predicate<SoftReference<LoadedDApp>> condition = (v) -> {
            LoadedDApp dapp = v.get();
            // remove the map entry if the soft reference has been cleared and the referent is null, or dapp has been loaded after blockNum
            return dapp == null || dapp.getLoadedCodeBlockNum() >= blockNum;
        };
        this.hotCache.removeValueIf(condition);
    }

    private void purgeDataCache(){
        Consumer<SoftReference<LoadedDApp>> softReferenceConsumer = (value) -> {
            LoadedDApp dapp = value.get();
            if(dapp != null){
                dapp.clearDataState();
            }
        };
        this.hotCache.apply(softReferenceConsumer);
    }

    private void cleanupTransformedCodeCache() {
        Predicate<SoftReference<byte[]>> condition = (v) -> {
            // remove the map entry if the soft reference has been cleared and the referent is null
            return v.get() == null;
        };
        this.transformedCodeCache.removeValueIf(condition);
    }
}

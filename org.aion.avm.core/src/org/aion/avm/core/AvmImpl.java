package org.aion.avm.core;

import org.aion.avm.core.util.Helpers;
import org.aion.kernel.TransactionContext;

import java.io.IOException;
import java.math.BigInteger;

import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.persistence.keyvalue.KeyValueObjectGraph;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.SoftCache;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.SimpleFuture;
import org.aion.kernel.TransactionResult;
import org.aion.kernel.TransactionalKernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.aion.avm.internal.RuntimeAssertionError.unexpected;


public class AvmImpl implements AvmInternal {

    private static final Logger logger = LoggerFactory.getLogger(AvmImpl.class);

    private final KernelInterface kernel;
    private final ReentrantDAppStack dAppStack;

    // Long-lived state which is book-ended by the startup/shutdown calls.
    private static AvmImpl currentAvm;  // (only here for testing - makes sure that we properly clean these up between invocations)
    private SoftCache<ByteArrayWrapper, LoadedDApp> hotCache;
    private HandoffMonitor handoff;

    public AvmImpl(KernelInterface kernel) {
        this.kernel = kernel;
        this.dAppStack = new ReentrantDAppStack();
    }

    public void startup() {
        RuntimeAssertionError.assertTrue(null == AvmImpl.currentAvm);
        AvmImpl.currentAvm = this;
        
        RuntimeAssertionError.assertTrue(null == this.hotCache);
        this.hotCache = new SoftCache<>();
        Thread thread = new Thread("AVM Execution Thread") {
            @Override
            public void run() {
                try {
                    // Run as long as we have something to do (null means shutdown).
                    TransactionResult outgoingResult = null;
                    TransactionContext incomingTransaction = AvmImpl.this.handoff.blockingPollForTransaction(outgoingResult);
                    while (null != incomingTransaction) {
                        outgoingResult = AvmImpl.this.backgroundProcessTransaction(incomingTransaction);
                        incomingTransaction = AvmImpl.this.handoff.blockingPollForTransaction(outgoingResult);
                    }
                } catch (Throwable t) {
                    // Uncaught exception - this is fatal but we need to communicate it to the outside.
                    AvmImpl.this.handoff.setBackgroundThrowable(t);
                    // If the throwable makes it all the way to here, we can't handle it.
                    RuntimeAssertionError.unexpected(t);
                }
            }
        };
        
        RuntimeAssertionError.assertTrue(null == this.handoff);
        this.handoff = new HandoffMonitor(thread);
        thread.start();
    }

    @Override
    public SimpleFuture<TransactionResult> run(TransactionContext ctx) {
        return this.handoff.sendTransactionAsynchronously(ctx);
    }

    private TransactionResult backgroundProcessTransaction(TransactionContext ctx) {
        // to capture any error during validation
        TransactionResult.Code error = null;

        // value/energyPrice/energyLimit sanity check
        if (ctx.getValue() < 0 || ctx.getEneryPrice() <= 0 || ctx.getEnergyLimit() < ctx.getBasicCost()) {
            // Instead of charging the tx basic cost at the outer level, we moved the billing logic into "run".
            // The min energyLimit check here is to avoid spams
            error = TransactionResult.Code.REJECTED;
        }

        // nonce check
        byte[] sender = ctx.getCaller();
        if (kernel.getNonce(sender) != ctx.getNonce()) {
            error = TransactionResult.Code.REJECTED_INVALID_NONCE;
        }

        TransactionResult result = null;
        if (null == error) {
            // If this is a GC, we need to handle it specially.  Otherwise, use the common invoke path (handles both CREATE and CALL).
            if (ctx.isGarbageCollectionRequest()) {
                // The GC case operates directly on the top-level KernelInterface.
                // (remember that the "sender" is who we are updating).
                result = runGc(sender);
            } else {
                // The CREATE/CALL case is handled via the common external invoke path.
                result = runExternalInvoke(ctx);
            }
        } else {
            result = new TransactionResult();
            result.setStatusCode(error);
            result.setEnergyUsed(ctx.getEnergyLimit());
        }
        return result;
    }

    @Override
    public void shutdown() {
        this.handoff.stopAndWaitForShutdown();
        this.handoff = null;
        RuntimeAssertionError.assertTrue(this == AvmImpl.currentAvm);
        AvmImpl.currentAvm = null;
        this.hotCache = null;
    }

    @Override
    public TransactionResult runInternalTransaction(KernelInterface parentKernel, TransactionContext context) {
        return commonInvoke(parentKernel, context);
    }


    private TransactionResult runExternalInvoke(TransactionContext ctx) {
        // to capture any error during validation
        TransactionResult.Code error = null;

        // Sanity checks around energy pricing and nonce are done in the caller.
        // balance check
        byte[] sender = ctx.getCaller();
        long senderBalance = kernel.getBalance(sender);
        if (BigInteger.valueOf(ctx.getValue()).add(BigInteger.valueOf(ctx.getEnergyLimit()).multiply(BigInteger.valueOf(ctx.getEneryPrice())))
                .compareTo(BigInteger.valueOf(senderBalance)) > 0) {
            error = TransactionResult.Code.REJECTED_INSUFFICIENT_BALANCE;
        }

        // exit if validation check fails
        if (error != null) {
            TransactionResult result = new TransactionResult();
            result.setStatusCode(error);
            result.setEnergyUsed(ctx.getEnergyLimit());
            return result;
        }

        /*
         * After this point, no rejection should occur.
         */

        // withhold the total energy cost
        this.kernel.adjustBalance(ctx.getCaller(), -(ctx.getEnergyLimit() * ctx.getEneryPrice()));

        // Run the common logic with the parent kernel as the top-level one.
        TransactionResult result = commonInvoke(kernel, ctx);

        // refund the remaining energy
        long remainingEnergy = ctx.getEnergyLimit() - result.getEnergyUsed();
        if (remainingEnergy > 0) {
            this.kernel.adjustBalance(ctx.getCaller(), remainingEnergy * ctx.getEneryPrice());
        }

        return result;
    }

    private TransactionResult commonInvoke(KernelInterface parentKernel, TransactionContext ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Transaction: address = {}, caller = {}, value = {}, data = {}, energyLimit = {}",
                    Helpers.bytesToHexString(ctx.getAddress()),
                    Helpers.bytesToHexString(ctx.getCaller()),
                    ctx.getValue(),
                    Helpers.bytesToHexString(ctx.getData()),
                    ctx.getEnergyLimit());
        }
        // We expect that the GC transactions are handled specially, within the caller.
        RuntimeAssertionError.assertTrue(!ctx.isGarbageCollectionRequest());

        // Invoke calls must build their transaction on top of an existing "parent" kernel.
        TransactionalKernel thisTransactionKernel = new TransactionalKernel(parentKernel);

        // only one result (mutable) shall be created per transaction execution
        TransactionResult result = new TransactionResult();
        result.setStatusCode(TransactionResult.Code.SUCCESS);
        result.setEnergyUsed(ctx.getBasicCost()); // basic tx cost

        // conduct value transfer
        thisTransactionKernel.adjustBalance(ctx.getCaller(), -ctx.getValue());
        thisTransactionKernel.adjustBalance(ctx.getAddress(), ctx.getValue());

        // increase nonce
        thisTransactionKernel.incrementNonce(ctx.getCaller());

        if (ctx.isCreate()) {
            DAppCreator.create(thisTransactionKernel, this, ctx, result);
        } else {
            byte[] dappAddress = ctx.getAddress();
            // See if this call is trying to reenter one already on this call-stack.  If so, we will need to partially resume its state.
            ReentrantDAppStack.ReentrantState stateToResume = dAppStack.tryShareState(dappAddress);

            LoadedDApp dapp;
            // The reentrant cache is obviously the first priority.
            if (null != stateToResume) {
                dapp = stateToResume.dApp;
                // Call directly and don't interact with DApp cache (we are reentering the state, not the origin of it).
                DAppExecutor.call(thisTransactionKernel, this, this.dAppStack, dapp, stateToResume, ctx, result);
            } else {
                // If we didn't find it there (that is only for reentrant calls so it is rarely found in the stack), try the hot DApp cache.
                ByteArrayWrapper addressWrapper = new ByteArrayWrapper(dappAddress);
                dapp = this.hotCache.checkout(addressWrapper);
                if (null == dapp) {
                    // If we didn't find it there, just load it.
                    try {
                        dapp = DAppLoader.loadFromGraph(new KeyValueObjectGraph(thisTransactionKernel, dappAddress));
                    } catch (IOException e) {
                        unexpected(e); // the jar was created by AVM; IOException is unexpected
                    }
                }
                // Run the call and, if successful, check this into the hot DApp cache.
                if (null != dapp) {
                    DAppExecutor.call(thisTransactionKernel, this, this.dAppStack, dapp, stateToResume, ctx, result);
                    if (TransactionResult.Code.SUCCESS == result.getStatusCode()) {
                        dapp.cleanForCache();
                        this.hotCache.checkin(addressWrapper, dapp);
                    }
                }
            }
        }
        if (result.getStatusCode().isSuccess()) {
            thisTransactionKernel.commit();
        } else {
            result.clearLogs();
            result.rejectInternalTransactions();
        }

        logger.debug("Result: {}", result);
        return result;
    }

    private TransactionResult runGc(byte[] dappAddress) {
        ByteArrayWrapper addressWrapper = new ByteArrayWrapper(dappAddress);
        
        LoadedDApp dapp = this.hotCache.checkout(addressWrapper);
        if (null == dapp) {
            // If we didn't find it there, just load it.
            try {
                dapp = DAppLoader.loadFromGraph(new KeyValueObjectGraph(this.kernel, dappAddress));
            } catch (IOException e) {
                unexpected(e); // the jar was created by AVM; IOException is unexpected
            }
        }
        
        TransactionResult result = new TransactionResult();
        if (null != dapp) {
            // Run the GC and check this into the hot DApp cache.
            long instancesFreed = dapp.graphStore.gc();
            this.hotCache.checkin(addressWrapper, dapp);
            // We want to set this to success and report the energy used as the refund found by the GC.
            // NOTE:  This is the total value of the refund as splitting that between the DApp and node is a higher-level decision.
            long storageEnergyRefund = instancesFreed * HelperBasedStorageFees.DEPOSIT_WRITE_COST;
            result.setStatusCode(TransactionResult.Code.SUCCESS);
            result.setEnergyUsed(-storageEnergyRefund);
        } else {
            // If we failed to find the application, we will currently return this as a generic FAILED_INVALID but we may want a more
            // specific code in the future.
            result.setStatusCode(TransactionResult.Code.FAILED_INVALID);
        }
        return result;
    }
}

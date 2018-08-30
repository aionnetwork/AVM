package org.aion.avm.core;

import org.aion.avm.core.util.Helpers;
import org.aion.kernel.TransactionContext;

import java.io.IOException;
import java.math.BigInteger;

import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.SoftCache;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.TransactionResult;
import org.aion.kernel.TransactionalKernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.aion.avm.internal.RuntimeAssertionError.unexpected;


public class AvmImpl implements AvmInternal {

    private static final Logger logger = LoggerFactory.getLogger(AvmImpl.class);

    private final KernelInterface kernel;
    private final ReentrantDAppStack dAppStack;
    private final SoftCache<ByteArrayWrapper, LoadedDApp> hotCache;

    public AvmImpl(KernelInterface kernel) {
        this.kernel = kernel;
        this.dAppStack = new ReentrantDAppStack();
        // We currently assume that this is being called on a single thread.
        this.hotCache = new SoftCache<>();
    }

    @Override
    public TransactionResult run(TransactionContext ctx) {
        // to capture any error during validation
        TransactionResult.Code error = null;

        // value/energyPrice/energyLimit sanity check
        if (ctx.getValue() < 0 || ctx.getEneryPrice() <= 0 || ctx.getEnergyLimit() < ctx.getBasicCost()) {
            // Instead of charging the tx basic cost at the outer level, we moved the billing logic into "run".
            // The min energyLimit check here is to avoid spams
            error = TransactionResult.Code.REJECTED;
        }

        // balance check
        byte[] sender = ctx.getCaller();
        long senderBalance = kernel.getBalance(sender);
        if (BigInteger.valueOf(ctx.getValue()).add(BigInteger.valueOf(ctx.getEnergyLimit()).multiply(BigInteger.valueOf(ctx.getEneryPrice())))
                .compareTo(BigInteger.valueOf(senderBalance)) > 0) {
            error = TransactionResult.Code.REJECTED_INSUFFICIENT_BALANCE;
        }

        // nonce check
        if (kernel.getNonce(sender) != ctx.getNonce()) {
            error = TransactionResult.Code.REJECTED_INVALID_NONCE;
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

        // effectively, we're running an internal transaction with no parent
        TransactionResult result = runInternalTransaction(kernel, ctx);

        // refund the remaining energy
        long remainingEnergy = ctx.getEnergyLimit() - result.getEnergyUsed();
        if (remainingEnergy > 0) {
            this.kernel.adjustBalance(ctx.getCaller(), remainingEnergy * ctx.getEneryPrice());
        }

        return result;
    }

    @Override
    public TransactionResult runInternalTransaction(KernelInterface parentKernel, TransactionContext context) {
        // Internal calls must build their transaction on top of an existing "parent" kernel.
        TransactionalKernel childKernel = new TransactionalKernel(parentKernel);
        TransactionResult result = commonRun(childKernel, context);
        if (result.getStatusCode().isSuccess()) {
            childKernel.commit();
        } else {
            result.clearLogs();
            result.rejectInternalTransactions();
        }
        return result;
    }


    private TransactionResult commonRun(KernelInterface thisTransactionKernel, TransactionContext ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Transaction: address = {}, caller = {}, value = {}, data = {}, energyLimit = {}",
                    Helpers.bytesToHexString(ctx.getAddress()),
                    Helpers.bytesToHexString(ctx.getCaller()),
                    ctx.getValue(),
                    Helpers.bytesToHexString(ctx.getData()),
                    ctx.getEnergyLimit());
        }

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
                        dapp = DAppLoader.loadFromKernel(thisTransactionKernel, dappAddress);
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

        logger.debug("Result: {}", result);
        return result;
    }
}

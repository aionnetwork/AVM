package org.aion.avm.core;

import org.aion.avm.core.util.Helpers;
import org.aion.kernel.TransactionContext;

import java.io.IOException;

import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.SoftCache;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.TransactionResult;
import org.aion.kernel.TransactionalKernel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
        // We are the root call so use our root kernel.
        TransactionalKernel childKernel = new TransactionalKernel(this.kernel);
        TransactionResult result = commonRun(childKernel, ctx);
        if (TransactionResult.Code.SUCCESS == result.getStatusCode()) {
            childKernel.commit();
        }

        // finalize the transaction result at the root of the invocation.
        if (!result.getStatusCode().isSuccess()) {
            result.clearLogs();
            result.rejectInternalTransactions();
        }

        return result;
    }

    @Override
    public TransactionResult runInternalTransaction(KernelInterface parentKernel, TransactionContext context) {
        // Internal calls must build their transaction on top of an existing "parent" kernel.
        TransactionalKernel childKernel = new TransactionalKernel(parentKernel);
        TransactionResult result = commonRun(childKernel, context);
        if (TransactionResult.Code.SUCCESS == result.getStatusCode()) {
            childKernel.commit();
        }
        return result;
    }


    private TransactionResult commonRun(KernelInterface thisTransactionKernel, TransactionContext ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Transaction: address = {}, caller = {}, value = {}, data = {}, energyLimit = {}",
                    Helpers.toHexString(ctx.getAddress()),
                    Helpers.toHexString(ctx.getCaller()),
                    ctx.getValue(),
                    Helpers.toHexString(ctx.getData()),
                    ctx.getEnergyLimit());
        }

        // only one result (mutable) shall be created per transaction execution
        TransactionResult result = new TransactionResult();

        // TODO: charge basic transaction cost - note that there isn't yet an IHelper instance so there is nobody to bill.

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
                        result.setStatusCode(TransactionResult.Code.FAILED);
                        result.setEnergyUsed(ctx.getEnergyLimit());
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

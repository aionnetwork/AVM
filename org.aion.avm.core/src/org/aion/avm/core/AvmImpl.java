package org.aion.avm.core;

import org.aion.kernel.TransactionContext;

import java.io.IOException;

import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.util.ByteArrayWrapper;
import org.aion.avm.core.util.SoftCache;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.TransactionResult;


public class AvmImpl implements Avm {
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
        // only one result (mutable) shall be created per transaction execution
        TransactionResult result = new TransactionResult();

        // TODO: charge basic transaction cost

        if (ctx.isCreate()) {
            DAppCreator.create(this.kernel, this, ctx, result);
        } else {
            byte[] dappAddress = ctx.getAddress();
            // See if this call is trying to reenter one already on this call-stack.  If so, we will need to partially resume its state.
            ReentrantDAppStack.ReentrantState stateToResume = dAppStack.tryShareState(dappAddress);

            LoadedDApp dapp;
            // The reentrant cache is obviously the first priority.
            if (null != stateToResume) {
                dapp = stateToResume.dApp;
                // Call directly and don't interact with DApp cache (we are reentering the state, not the origin of it).
                DAppExecutor.call(this.kernel, this, this.dAppStack, dapp, stateToResume, ctx, result);
            } else {
                // If we didn't find it there (that is only for reentrant calls so it is rarely found in the stack), try the hot DApp cache.
                ByteArrayWrapper addressWrapper = new ByteArrayWrapper(dappAddress);
                dapp = this.hotCache.checkout(addressWrapper);
                if (null == dapp) {
                    // If we didn't find it there, just load it.
                    try {
                        dapp = DAppLoader.loadFromKernel(kernel, dappAddress);
                    } catch (IOException e) {
                        result.setStatusCode(TransactionResult.Code.FAILED);
                        result.setEnergyUsed(ctx.getEnergyLimit());
                    }
                }
                // Run the call and, if successful, check this into the hot DApp cache.
                if (null != dapp) {
                    DAppExecutor.call(this.kernel, this, this.dAppStack, dapp, stateToResume, ctx, result);
                    if (TransactionResult.Code.SUCCESS == result.getStatusCode()) {
                        dapp.cleanForCache();
                        this.hotCache.checkin(addressWrapper, dapp);
                    }
                }
            }
        }

        return result;
    }
}

package org.aion.avm.core;

import org.aion.avm.core.persistence.ContractEnvironmentState;
import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.internal.*;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;


public class DAppExecutor {
    public static void call(KernelInterface kernel, Avm avm, ReentrantDAppStack dAppStack, LoadedDApp dapp, ReentrantDAppStack.ReentrantState stateToResume, Transaction tx, TransactionContext ctx, TransactionResult result) {
        byte[] dappAddress = tx.getTo();
        // Load the initial state of the environment.
        // (note that ContractEnvironmentState is immutable, so it is safe to just access the environment from a different invocation).
        ContractEnvironmentState initialState = (null != stateToResume)
                ? stateToResume.getEnvironment()
                : ContractEnvironmentState.loadFromStorage(kernel, dappAddress);
        
        // Note that we need to store the state of this invocation on the reentrant stack in case there is another call into the same app.
        // We only put this here so that the call() mechanism can access it to save/reload its ContractEnvironmentState but we don't change it.
        ReentrantDAppStack.ReentrantState thisState = new ReentrantDAppStack.ReentrantState(dappAddress, dapp, initialState);
        dAppStack.pushState(thisState);
        
        // TODO:  We might be able to move this setup of IHelper to later in the load once we get rid of the <clinit> (requires energy).
        IHelper helper = dapp.instantiateHelperInApp(tx.getEnergyLimit(), initialState.nextHashCode);
        dapp.attachBlockchainRuntime(new BlockchainRuntimeImpl(kernel, avm, thisState, ctx, helper, result));

        // Now that we can load classes for the contract, load and populate all their classes.
        dapp.populateClassStaticsFromStorage(kernel);

        // Call the main within the DApp.
        try {
            byte[] ret = dapp.callMain();

            // Save back the state before we return.
            // -first, save out the classes
            // TODO:  Handle this save of the object graph differently when invoked reentrant (since that is about writing back to a different graph).
            long nextInstanceId = dapp.saveClassStaticsToStorage(initialState.nextInstanceId, kernel);
            // -finally, save back the final state of the environment so we restore it on the next invocation.
            if (null != stateToResume) {
                // Write this back into the resumed state.
                stateToResume.updateEnvironment(helper.externalGetNextHashCode());
            } else {
                // We are at the "top" so write this back to disk.
                ContractEnvironmentState updatedEnvironment = new ContractEnvironmentState(helper.externalGetNextHashCode(), nextInstanceId);
                ContractEnvironmentState.saveToStorage(kernel, dappAddress, updatedEnvironment);
            }

            result.setStatusCode(TransactionResult.Code.SUCCESS);
            result.setReturnData(ret);
            result.setEnergyUsed(tx.getEnergyLimit() - helper.externalGetEnergyRemaining());
        } catch (OutOfEnergyError e) {
            result.setStatusCode(TransactionResult.Code.OUT_OF_ENERGY);
            result.setEnergyUsed(tx.getEnergyLimit());
        } catch (Exception e) {
            result.setStatusCode(TransactionResult.Code.FAILURE);
            result.setEnergyUsed(tx.getEnergyLimit());
        } finally {
            // Once we are done running this, we want to clear the IHelper.currentContractHelper.
            IHelper.currentContractHelper.remove();
            // This state was only here while we were running, in case someone else needed to change it so now we can pop it.
            dAppStack.popState();
        }
    }
}

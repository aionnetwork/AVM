package org.aion.avm.core;

import org.aion.avm.core.persistence.ContractEnvironmentState;
import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.internal.*;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionResult;

import java.io.*;


public class DAppExecutor {
    public static void call(KernelInterface kernel, Avm avm, ReentrantDAppStack dAppStack, Transaction tx, TransactionContext ctx, TransactionResult result) {
        // retrieve the transformed bytecode
        byte[] dappAddress = tx.getTo();
        LoadedDApp dapp;
        try {
            dapp = DAppLoader.loadFromKernel(kernel, dappAddress);
        } catch (IOException e) {
            result.setStatusCode(TransactionResult.Code.INVALID_CALL);
            result.setEnergyUsed(tx.getEnergyLimit());
            return;
        }
        
        // Load the initial state of the environment.
        ContractEnvironmentState initialState = ContractEnvironmentState.loadFromStorage(kernel, dappAddress);
        
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
            long nextInstanceId = dapp.saveClassStaticsToStorage(initialState.nextInstanceId, kernel);
            // -finally, save back the final state of the environment so we restore it on the next invocation.
            ContractEnvironmentState.saveToStorage(kernel, dappAddress, new ContractEnvironmentState(helper.externalGetNextHashCode(), nextInstanceId));

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

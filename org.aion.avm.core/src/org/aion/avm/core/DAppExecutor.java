package org.aion.avm.core;

import org.aion.avm.core.persistence.ContractEnvironmentState;
import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.persistence.ReentrantGraphProcessor;
import org.aion.avm.internal.*;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.TransactionResult;


public class DAppExecutor {
    public static void call(KernelInterface kernel, AvmInternal avm, ReentrantDAppStack dAppStack, LoadedDApp dapp, ReentrantDAppStack.ReentrantState stateToResume, TransactionContext ctx, TransactionResult result) {
        byte[] dappAddress = ctx.getAddress();
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
        IHelper helper = dapp.instantiateHelperInApp(ctx.getEnergyLimit(), initialState.nextHashCode);
        dapp.attachBlockchainRuntime(new BlockchainRuntimeImpl(kernel, avm, thisState, helper, ctx, ctx.getData(), result));
        HelperBasedStorageFees feeProcessor = new HelperBasedStorageFees(helper);

        // Now that we can load classes for the contract, load and populate all their classes.
        ReentrantGraphProcessor reentrantGraphData = null;
        if (null != stateToResume) {
            // We are invoking a reentrant call so we don't want to pull this data from storage, but create in-memory duplicates which we can
            // swap out, pointing to memory-backed instance stubs.
            reentrantGraphData = dapp.replaceClassStaticsWithClones(feeProcessor);
        } else {
            // This is the first invocation of this DApp so just load the static state from disk.
            dapp.populateClassStaticsFromStorage(feeProcessor, kernel);
        }

        // Call the main within the DApp.
        try {
            byte[] ret = dapp.callMain();

            // Save back the state before we return.
            if (null != stateToResume) {
                // Write this back into the resumed state.
                reentrantGraphData.commitGraphToStoredFieldsAndRestore();
                stateToResume.updateEnvironment(helper.externalGetNextHashCode());
            } else {
                // We are at the "top" so write this back to disk.
                // -first, save out the classes
                long nextInstanceId = dapp.saveClassStaticsToStorage(initialState.nextInstanceId, feeProcessor, kernel);
                // -finally, save back the final state of the environment so we restore it on the next invocation.
                ContractEnvironmentState updatedEnvironment = new ContractEnvironmentState(helper.externalGetNextHashCode(), nextInstanceId);
                ContractEnvironmentState.saveToStorage(kernel, dappAddress, updatedEnvironment);
            }

            result.setStatusCode(TransactionResult.Code.SUCCESS);
            result.setReturnData(ret);
            result.setEnergyUsed(ctx.getEnergyLimit() - helper.externalGetEnergyRemaining());
        } catch (OutOfEnergyException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setStatusCode(TransactionResult.Code.FAILED_OUT_OF_ENERGY);
            result.setEnergyUsed(ctx.getEnergyLimit());

        } catch (OutOfStackException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setStatusCode(TransactionResult.Code.FAILED_OUT_OF_STACK);
            result.setEnergyUsed(ctx.getEnergyLimit());

        } catch (RevertException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setStatusCode(TransactionResult.Code.FAILED_REVERT);
            result.setEnergyUsed(ctx.getEnergyLimit() - helper.externalGetEnergyRemaining());

        } catch (InvalidException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setStatusCode(TransactionResult.Code.FAILED_INVALID);
            result.setEnergyUsed(ctx.getEnergyLimit());

        }  catch (UncaughtException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setStatusCode(TransactionResult.Code.FAILED_EXCEPTION);
            result.setEnergyUsed(ctx.getEnergyLimit());

        } catch (AvmException e) {
            // We handle the generic AvmException as some failure within the contract.
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setStatusCode(TransactionResult.Code.FAILED);
            result.setEnergyUsed(ctx.getEnergyLimit());

        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(-1);
        } finally {
            // Once we are done running this, we want to clear the IHelper.currentContractHelper.
            IHelper.currentContractHelper.remove();
            // This state was only here while we were running, in case someone else needed to change it so now we can pop it.
            dAppStack.popState();
        }
    }
}

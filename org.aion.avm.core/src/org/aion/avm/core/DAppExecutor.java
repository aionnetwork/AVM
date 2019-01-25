package org.aion.avm.core;

import org.aion.avm.core.persistence.ContractEnvironmentState;
import org.aion.avm.core.persistence.IObjectGraphStore;
import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.persistence.ReentrantGraphProcessor;
import org.aion.avm.core.persistence.ReflectionStructureCodec;
import org.aion.avm.core.persistence.keyvalue.KeyValueObjectGraph;
import org.aion.avm.internal.*;
import org.aion.kernel.AvmTransactionResult;
import org.aion.parallel.TransactionTask;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DAppExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DAppExecutor.class);

    public static void call(KernelInterface kernel, AvmInternal avm, LoadedDApp dapp,
                            ReentrantDAppStack.ReentrantState stateToResume, TransactionTask task,
                            TransactionContext ctx, AvmTransactionResult result) {
        Address dappAddress = ctx.getDestinationAddress();
        IObjectGraphStore graphStore = new KeyValueObjectGraph(kernel, dappAddress);
        // Load the initial state of the environment.
        // (note that ContractEnvironmentState is immutable, so it is safe to just access the environment from a different invocation).
        ContractEnvironmentState initialState = (null != stateToResume)
                ? stateToResume.getEnvironment()
                : ContractEnvironmentState.loadFromGraph(graphStore);
        
        // Note that we need to store the state of this invocation on the reentrant stack in case there is another call into the same app.
        // This is required so that the call() mechanism can access it to save/reload its ContractEnvironmentState and so that the underlying
        // instance loader (ReentrantGraphProcessor/ReflectionStructureCodec) can be notified when it becomes active/inactive (since it needs
        // to know if it is loading an instance
        ReentrantDAppStack.ReentrantState thisState = new ReentrantDAppStack.ReentrantState(dappAddress, dapp, initialState);
        task.getReentrantDAppStack().pushState(thisState);
        
        IInstrumentation threadInstrumentation = IInstrumentation.attachedThreadInstrumentation.get();
        InstrumentationHelpers.pushNewStackFrame(dapp.runtimeSetup, dapp.loader, ctx.getTransaction().getEnergyLimit() - result.getEnergyUsed(), initialState.nextHashCode);
        IBlockchainRuntime previousRuntime = dapp.attachBlockchainRuntime(new BlockchainRuntimeImpl(kernel, avm, thisState, task, ctx, ctx.getTransactionData(), result, dapp.runtimeSetup));
        InstrumentationBasedStorageFees feeProcessor = new InstrumentationBasedStorageFees(threadInstrumentation);

        ReentrantGraphProcessor reentrantGraphData = null;
        ReflectionStructureCodec directGraphData = null;

        // Call the main within the DApp.
        try {

            // Now that we can load classes for the contract, load and populate all their classes.
            if (null != stateToResume) {
                // We are invoking a reentrant call so we don't want to pull this data from storage, but create in-memory duplicates which we can
                // swap out, pointing to memory-backed instance stubs.
                reentrantGraphData = dapp.replaceClassStaticsWithClones(feeProcessor);
                thisState.setInstanceLoader(reentrantGraphData);
            } else {
                // This is the first invocation of this DApp so just load the static state from disk.
                directGraphData = dapp.populateClassStaticsFromStorage(feeProcessor, graphStore);
                thisState.setInstanceLoader(directGraphData);
            }

            byte[] ret = dapp.callMain();

            // Save back the state before we return.
            if (null != stateToResume) {
                // Write this back into the resumed state.
                reentrantGraphData.commitGraphToStoredFieldsAndRestore();
                stateToResume.updateEnvironment(threadInstrumentation.getNextHashCodeAndIncrement());
            } else {
                // We are at the "top" so write this back to disk.
                // -first, save out the classes
                dapp.saveClassStaticsToStorage(feeProcessor, directGraphData, graphStore);
                // -finally, save back the final state of the environment so we restore it on the next invocation.
                ContractEnvironmentState updatedEnvironment = new ContractEnvironmentState(threadInstrumentation.getNextHashCodeAndIncrement());
                ContractEnvironmentState.saveToGraph(graphStore, updatedEnvironment);
            }
            graphStore.flushWrites();

            result.setResultCode(AvmTransactionResult.Code.SUCCESS);
            result.setReturnData(ret);
            result.setEnergyUsed(ctx.getTransaction().getEnergyLimit() - threadInstrumentation.energyLeft());
            result.setStorageRootHash(graphStore.simpleHashCode());
        } catch (OutOfEnergyException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_OUT_OF_ENERGY);
            result.setEnergyUsed(ctx.getTransaction().getEnergyLimit());

        } catch (OutOfStackException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_OUT_OF_STACK);
            result.setEnergyUsed(ctx.getTransaction().getEnergyLimit());

        } catch (CallDepthLimitExceededException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_CALL_DEPTH_LIMIT_EXCEEDED);
            result.setEnergyUsed(ctx.getTransaction().getEnergyLimit());

        } catch (RevertException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_REVERT);
            result.setEnergyUsed(ctx.getTransaction().getEnergyLimit() - threadInstrumentation.energyLeft());

        } catch (InvalidException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_INVALID);
            result.setEnergyUsed(ctx.getTransaction().getEnergyLimit());

        } catch (EarlyAbortException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_ABORT);
            result.setEnergyUsed(0);

        } catch (UncaughtException e) {
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_EXCEPTION);
            result.setEnergyUsed(ctx.getTransaction().getEnergyLimit());
            result.setUncaughtException(e.getCause());
            logger.debug("Uncaught exception", e.getCause());
        } catch (AvmException e) {
            // We handle the generic AvmException as some failure within the contract.
            if (null != reentrantGraphData) {
                reentrantGraphData.revertToStoredFields();
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED);
            result.setEnergyUsed(ctx.getTransaction().getEnergyLimit());
        } catch (JvmError e) {
            // These are cases which we know we can't handle and have decided to handle by safely stopping the AVM instance so
            // re-throw this as the AvmImpl top-level loop will commute it into an asynchronous shutdown.
            throw e;
        } catch (RuntimeAssertionError e) {
            // If one of these shows up here, we are wanting to pass it back up to the top, where we can shut down.
            throw new AssertionError(e.getCause());
        } catch (Throwable e) {
            // Anything else we couldn't handle more specifically needs to be passed further up to the top.
            throw new AssertionError(e);
        } finally {
            // Once we are done running this, no matter how it ended, we want to detach our thread from the DApp.
            InstrumentationHelpers.popExistingStackFrame(dapp.runtimeSetup);
            // This state was only here while we were running, in case someone else needed to change it so now we can pop it.
            task.getReentrantDAppStack().popState();

            // Re-attach the previously detached IBlockchainRuntime instance.
            dapp.attachBlockchainRuntime(previousRuntime);
        }
    }
}

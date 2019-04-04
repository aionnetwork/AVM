package org.aion.avm.core;

import org.aion.avm.core.persistence.LoadedDApp;
import org.aion.avm.core.persistence.ReentrantGraph;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.*;
import org.aion.kernel.AvmTransactionResult;
import org.aion.parallel.TransactionTask;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DAppExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DAppExecutor.class);

    public static void call(IExternalCapabilities capabilities, KernelInterface kernel, AvmInternal avm, LoadedDApp dapp,
                            ReentrantDAppStack.ReentrantState stateToResume, TransactionTask task,
                            TransactionInterface tx, AvmTransactionResult result, boolean verboseErrors) {
        Address dappAddress = tx.getDestinationAddress();
        
        // If this is a reentrant call, we need to serialize the graph of the parent frame.  This is required to both copy-back our changes but also
        // is required in case we want to revert the state.
        ReentrantGraph callerState = (null != stateToResume)
                ? dapp.captureStateAsCaller(stateToResume.getNextHashCode(), StorageFees.MAX_GRAPH_SIZE)
                : null;
        
        // Note that the instrumentation is just a per-thread access to the state stack - we can grab it at any time as it never changes for this thread.
        IInstrumentation threadInstrumentation = IInstrumentation.attachedThreadInstrumentation.get();
        
        // We need to get the interned classes before load the graph since it might need to instantiate class references.
        InternedClasses initialClassWrappers = (null != stateToResume)
            ? stateToResume.getInternedClassWrappers()
            : new InternedClasses();

        // We are now ready to load the graph (note that we can't do any billing until after we install the InstrumentationHelpers new stack frame).
        byte[] rawGraphData = (null != callerState)
                ? callerState.rawState
                : kernel.getObjectGraph(dappAddress);
        int nextHashCode = dapp.loadEntireGraph(initialClassWrappers, rawGraphData);
        
        // Note that we need to store the state of this invocation on the reentrant stack in case there is another call into the same app.
        // This is required so that the call() mechanism can access it to save/reload its ContractEnvironmentState and so that the underlying
        // instance loader (ReentrantGraphProcessor/ReflectionStructureCodec) can be notified when it becomes active/inactive (since it needs
        // to know if it is loading an instance
        ReentrantDAppStack.ReentrantState thisState = new ReentrantDAppStack.ReentrantState(dappAddress, dapp, nextHashCode, initialClassWrappers);
        task.getReentrantDAppStack().pushState(thisState);
        
        InstrumentationHelpers.pushNewStackFrame(dapp.runtimeSetup, dapp.loader, tx.getEnergyLimit() - result.getEnergyUsed(), nextHashCode, initialClassWrappers);
        IBlockchainRuntime previousRuntime = dapp.attachBlockchainRuntime(new BlockchainRuntimeImpl(capabilities, kernel, avm, thisState, task, tx, tx.getData(), dapp.runtimeSetup));

        try {
            // It is now safe for us to bill for the cost of loading the graph (the cost is the same, whether this came from the caller or the disk).
            // (note that we do this under the try since aborts can happen here)
            threadInstrumentation.chargeEnergy(StorageFees.READ_PRICE_PER_BYTE * rawGraphData.length);
            
            // Call the main within the DApp.
            byte[] ret = dapp.callMain();

            // Save back the state before we return.
            if (null != stateToResume) {
                int updatedNextHashCode = threadInstrumentation.peekNextHashCode();
                ReentrantGraph calleeState = dapp.captureStateAsCallee(updatedNextHashCode, StorageFees.MAX_GRAPH_SIZE);
                // Bill for writing this size.
                threadInstrumentation.chargeEnergy(StorageFees.WRITE_PRICE_PER_BYTE * calleeState.rawState.length);
                // Now, commit this back into the callerState.
                dapp.commitReentrantChanges(initialClassWrappers, callerState, calleeState);
                // Update the final hash code.
                stateToResume.updateNextHashCode(updatedNextHashCode);
            } else {
                // We are at the "top" so write this back to disk.
                byte[] postCallGraphData = dapp.saveEntireGraph(threadInstrumentation.peekNextHashCode(), StorageFees.MAX_GRAPH_SIZE);
                // Bill for writing this size.
                threadInstrumentation.chargeEnergy(StorageFees.WRITE_PRICE_PER_BYTE * postCallGraphData.length);
                kernel.putObjectGraph(dappAddress, postCallGraphData);
            }

            result.setResultCode(AvmTransactionResult.Code.SUCCESS);
            result.setReturnData(ret);
            result.setEnergyUsed(tx.getEnergyLimit() - threadInstrumentation.energyLeft());
        } catch (OutOfEnergyException e) {
            if (verboseErrors) {
                System.err.println("DApp execution failed due to Out-of-Energy EXCEPTION: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
            if (null != stateToResume) {
                dapp.revertToCallerState(initialClassWrappers, callerState);
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_OUT_OF_ENERGY);
            result.setEnergyUsed(tx.getEnergyLimit());

        } catch (OutOfStackException e) {
            if (verboseErrors) {
                System.err.println("DApp execution failed due to stack overflow EXCEPTION: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
            if (null != stateToResume) {
                dapp.revertToCallerState(initialClassWrappers, callerState);
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_OUT_OF_STACK);
            result.setEnergyUsed(tx.getEnergyLimit());

        } catch (CallDepthLimitExceededException e) {
            if (verboseErrors) {
                System.err.println("DApp execution failed due to call depth limit EXCEPTION: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
            if (null != stateToResume) {
                dapp.revertToCallerState(initialClassWrappers, callerState);
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_CALL_DEPTH_LIMIT_EXCEEDED);
            result.setEnergyUsed(tx.getEnergyLimit());

        } catch (RevertException e) {
            if (verboseErrors) {
                System.err.println("DApp execution to REVERT due to uncaught EXCEPTION: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
            if (null != stateToResume) {
                dapp.revertToCallerState(initialClassWrappers, callerState);
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_REVERT);
            result.setEnergyUsed(tx.getEnergyLimit() - threadInstrumentation.energyLeft());

        } catch (InvalidException e) {
            if (verboseErrors) {
                System.err.println("DApp execution INVALID due to uncaught EXCEPTION: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
            if (null != stateToResume) {
                dapp.revertToCallerState(initialClassWrappers, callerState);
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_INVALID);
            result.setEnergyUsed(tx.getEnergyLimit());

        } catch (EarlyAbortException e) {
            if (verboseErrors) {
                System.err.println("FYI - concurrent abort (will retry) in transaction \"" + Helpers.bytesToHexString(tx.getTransactionHash()) + "\"");
            }
            if (null != stateToResume) {
                dapp.revertToCallerState(initialClassWrappers, callerState);
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_ABORT);
            result.setEnergyUsed(0);

        } catch (UncaughtException e) {
            if (verboseErrors) {
                System.err.println("DApp execution failed due to uncaught EXCEPTION: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
            if (null != stateToResume) {
                dapp.revertToCallerState(initialClassWrappers, callerState);
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED_EXCEPTION);
            result.setEnergyUsed(tx.getEnergyLimit());
            result.setUncaughtException(e.getCause());
            logger.debug("Uncaught exception", e.getCause());
        } catch (AvmException e) {
            // We handle the generic AvmException as some failure within the contract.
            if (verboseErrors) {
                System.err.println("DApp execution failed due to AvmException: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
            if (null != stateToResume) {
                dapp.revertToCallerState(initialClassWrappers, callerState);
            }
            result.setResultCode(AvmTransactionResult.Code.FAILED);
            result.setEnergyUsed(tx.getEnergyLimit());
        } catch (JvmError e) {
            // These are cases which we know we can't handle and have decided to handle by safely stopping the AVM instance so
            // re-throw this as the AvmImpl top-level loop will commute it into an asynchronous shutdown.
            if (verboseErrors) {
                System.err.println("FATAL JvmError: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
            throw e;
        } catch (RuntimeAssertionError e) {
            // If one of these shows up here, we are wanting to pass it back up to the top, where we can shut down.
            if (verboseErrors) {
                System.err.println("FATAL internal error: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
            throw new AssertionError(e);
        } catch (Throwable e) {
            // Anything else we couldn't handle more specifically needs to be passed further up to the top.
            if (verboseErrors) {
                System.err.println("FATAL unexpected Throwable: \"" + e.getMessage() + "\"");
                e.printStackTrace(System.err);
            }
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

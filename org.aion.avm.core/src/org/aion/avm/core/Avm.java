package org.aion.avm.core;

import org.aion.kernel.SimpleFuture;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionResult;
import org.aion.vm.api.interfaces.KernelInterface;


/**
 * The public interfaced exposed by the AVM for deploying or running transactions against DApps.
 * Note that it is expected that the AVM instance will be created once and then reused for each transaction.
 */
public interface Avm {
    /**
     * Executes the array of DApp transactions.
     * Note that the execution is asynchronous relative to the caller and returned via Future.
     * Throws IllegalStateException if the receiver has already been shutdown.
     *
     * @param transactions The array of transactions to run (cannot be empty).
     * @return The results as a corresponding array of asynchronous futures.
     */
    SimpleFuture<TransactionResult>[] run(TransactionContext[] transactions) throws IllegalStateException;

    /**
     * Tells the Avm implementation to shut down.  This means that it can assume it will not be called again.
     * The specifics of what is shut down are implementation-dependent:  threads, caches, other long-lived resources.
     */
    public void shutdown();

    /**
     * Updates the Avm to use the specified kernel.
     *
     * @param kernel The new kernel for the Avm to interact with.
     */
    public void setKernel(KernelInterface kernel);
}

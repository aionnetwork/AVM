package org.aion.avm.core;

import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionResult;


/**
 * The public interfaced exposed by the AVM for deploying or running transactions against DApps.
 * Note that it is expected that the AVM instance will be created once and then reused for each transaction.
 */
public interface Avm {
    /**
     * Executes the given DApp, with the provided runtime.
     * Throws IllegalStateException if the receiver has already been shutdown.
     *
     * @param context the transaction context
     * @return the result
     */
    TransactionResult run(TransactionContext context);

    /**
     * Tells the Avm implementation to shut down.  This means that it can assume it will not be called again.
     * The specifics of what is shut down are implementation-dependent:  threads, caches, other long-lived resources.
     */
    public void shutdown();
}

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
     *
     * @param context the transaction context
     * @return the result
     */
    TransactionResult run(TransactionContext context);
}
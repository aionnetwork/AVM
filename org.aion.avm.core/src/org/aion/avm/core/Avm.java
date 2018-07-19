package org.aion.avm.core;

import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionResult;

/**
 * High-level Aion Virtual Machine interface.
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
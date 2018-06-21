package org.aion.avm.core;

import org.aion.kernel.Block;
import org.aion.kernel.KernelApi;
import org.aion.kernel.Transaction;

/**
 * High-level Aion Virtual Machine interface.
 */
public interface Avm {
    /**
     * Executes the given DApp, with the provided runtime.
     *
     * @param tx    the transaction
     * @param block the block
     * @param cb    the kernel callback
     * @return the result
     */
    AvmResult run(Transaction tx, Block block, KernelApi cb);
}
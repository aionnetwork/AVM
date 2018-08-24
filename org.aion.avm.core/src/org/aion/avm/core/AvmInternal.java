package org.aion.avm.core;

import org.aion.kernel.KernelInterface;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionResult;


/**
 * The private/internal extension of the Avm public interface.  This is the internally-facing interface which supports things like internal
 * transactions, which aren't otherwise exposted/meaningful to calls originating outside an AVM implementation.
 */
public interface AvmInternal extends Avm {
    /**
     * Runs a transaction, originating from inside the AVM, itself.
     * 
     * @param parentKernel The KernelInterface of the origin of the transaction, in a parent call frame.
     * @param context The transaction to run.
     * @return The result of the transaction.
     */
    TransactionResult runInternalTransaction(KernelInterface parentKernel, TransactionContext context);
}
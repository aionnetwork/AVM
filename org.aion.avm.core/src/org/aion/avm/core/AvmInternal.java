package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.kernel.AvmWrappedTransactionResult;
import org.aion.parallel.AddressResourceMonitor;
import org.aion.parallel.TransactionTask;
import org.aion.types.AionAddress;


/**
 * The private/internal extension of the Avm public interface.  This is the internally-facing interface which supports things like internal
 * transactions, which aren't otherwise exposted/meaningful to calls originating outside an AVM implementation.
 */
public interface AvmInternal {
    /**
     * Runs a transaction, originating from inside the AVM, itself.
     * 
     * @param parentKernel The KernelInterface of the origin of the transaction, in a parent call frame.
     * @param task The current transaction task.
     * @param senderAddress The sender of the internal transaction (usually a contract but this could be an external user, in case of a meta-transaction).
     * @param isCreate True if this is a contract deployment, false for any other kind of call.
     * @param normalCallTarget The target of the normal call, null if this is a create.
     * @param effectiveTransactionOrigin The origin address of the internal transaction to create (usually, this is the sender of the top-most transaction but could be different if there is a meta-transaction in the stack).
     * @param transactionData The data being passed as an argument to the transaction (not null).
     * @param transactionHash The hash of the transaction sent by the effectiveTransactionOrigin (usually the hash of the top-most transaction but could be different if there is a meta-transaction in the stack).
     * @param energyLimit The energy limit of this internal transaction.
     * @param energyPrice The energy price applied to transactions in this stack.
     * @param transactionValue The value to transfer to the destination.
     * @param nonce The nonce of this transaction, as associated with the senderAddress.
     * @return The result of the transaction.
     */
    AvmWrappedTransactionResult runInternalTransaction(IExternalState parentKernel
            , TransactionTask task
            , AionAddress senderAddress
            , boolean isCreate
            , AionAddress normalCallTarget  // Null if this is a create.
            , AionAddress effectiveTransactionOrigin
            , byte[] transactionData
            , byte[] transactionHash
            , long energyLimit
            , long energyPrice
            , BigInteger transactionValue
            , BigInteger nonce
    );

    /**
     * Get the address resource monitor using by the current AVM.
     *
     * @return The address resource monitor.
     */
    AddressResourceMonitor getResourceMonitor();
}
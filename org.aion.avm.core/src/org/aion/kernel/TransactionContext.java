package org.aion.kernel;


/**
 * Interface for accessing data related to the context of the transaction.
 */
public interface TransactionContext {
    Transaction getTransaction();

    Block getBlock();
}

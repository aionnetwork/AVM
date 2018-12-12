package org.aion.kernel;


import java.math.BigInteger;

/**
 * Interface for accessing data related to the context of the transaction.
 */
public interface TransactionContext {

    boolean isCreate();

    boolean isBalanceTransfer();

    boolean isGarbageCollectionRequest();

    byte[] getAddress();

    byte[] getCaller();

    byte[] getOrigin();

    long getNonce();

    BigInteger getValue();

    byte[] getData();

    long getEnergyLimit();

    long getEnergyPrice();

    byte[] getTransactionHash();

    /**
     * The basic cost is the fixed minimum cost of any transaction, plus an extra fee for any data.
     * 
     * @return The basic cost, in energy, of this transaction and its data.
     */
    long getBasicCost();

    long getTransactionTimestamp();

    long getBlockTimestamp();

    long getBlockNumber();

    long getBlockEnergyLimit();

    byte[] getBlockCoinbase();

    byte[] getBlockPreviousHash();

    BigInteger getBlockDifficulty();

    int getInternalCallDepth();
}

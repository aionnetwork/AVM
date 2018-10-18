package org.aion.kernel;


import java.math.BigInteger;

/**
 * Interface for accessing data related to the context of the transaction.
 */
public interface TransactionContext {

    boolean isCreate();

    boolean isGarbageCollectionRequest();

    byte[] getAddress();

    byte[] getCaller();

    byte[] getOrigin();

    long getNonce();

    long getValue();

    byte[] getData();

    long getEnergyLimit();

    long getEneryPrice();

    int getBasicCost();

    long getTransactionTimestamp();

    long getBlockTimestamp();

    long getBlockNumber();

    long getBlockEnergyLimit();

    byte[] getBlockCoinbase();

    byte[] getBlockPreviousHash();

    BigInteger getBlockDifficulty();

    int getInternalCallDepth();
}

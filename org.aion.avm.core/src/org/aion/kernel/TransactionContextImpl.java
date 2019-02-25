package org.aion.kernel;

import java.math.BigInteger;

import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionInterface;
import org.aion.vm.api.interfaces.TransactionSideEffects;


public class TransactionContextImpl implements TransactionContext {
    /**
     * Called to create a transaction context from the top-level of a block (the common case).
     * 
     * @param tx The transaction.
     * @param block The top-level of the block.
     * @return The transaction context.
     */
    public static TransactionContextImpl forExternalTransaction(Transaction tx, Block block) {
        Address origin = tx.getSenderAddress();
        int internalCallDepth = 1;
        long blockNumber = block.getNumber();
        long blockTimestamp = block.getTimestamp();
        long blockEnergyLimit = block.getEnergyLimit();
        Address blockCoinbase = block.getCoinbase();
        BigInteger blockDifficulty = block.getDifficulty();
        return new TransactionContextImpl(tx, origin, internalCallDepth, blockNumber, blockTimestamp, blockEnergyLimit, blockCoinbase, blockDifficulty);
    }

    /**
     * Called to create a transaction context from within the execution of an existing one (the rare case).
     * 
     * @param parent The context of the parent transaction.
     * @param tx The transaction.
     * @return The transaction context.
     */
    public static TransactionContextImpl forInternalTransaction(TransactionContext parent, Transaction tx) {
        Address origin = parent.getOriginAddress();
        int internalCallDepth = parent.getTransactionStackDepth() + 1;
        long blockNumber = parent.getBlockNumber();
        long blockTimestamp = parent.getBlockTimestamp();
        long blockEnergyLimit = parent.getBlockEnergyLimit();
        Address blockCoinbase = parent.getMinerAddress();
        BigInteger blockDifficulty = BigInteger.valueOf(parent.getBlockDifficulty());
        return new TransactionContextImpl(tx, origin, internalCallDepth, blockNumber, blockTimestamp, blockEnergyLimit, blockCoinbase, blockDifficulty);
    }


    private final Transaction tx;
    private final TransactionSideEffects sideEffects;
    private byte[] transactionHash;
    private final byte[] originTransactionHash;
    private final Address origin;
    private final int internalCallDepth;
    private final long blockNumber;
    private final long blockTimestamp;
    private final long blockEnergyLimit;
    private final Address blockCoinbase;
    private final BigInteger blockDifficulty;

    private TransactionContextImpl(Transaction tx, Address origin, int internalCallDepth, long blockNumber, long blockTimestamp, long blockEnergyLimit, Address blockCoinbase, BigInteger blockDifficulty) {
        this.tx = tx;
        this.transactionHash = tx.getTransactionHash();
        this.originTransactionHash = this.transactionHash;
        this.origin = origin;
        this.internalCallDepth = internalCallDepth;

        this.blockNumber = blockNumber;
        this.blockTimestamp = blockTimestamp;
        this.blockEnergyLimit = blockEnergyLimit;
        this.blockCoinbase = blockCoinbase;
        this.blockDifficulty = blockDifficulty;
        this.sideEffects = new SideEffects();
    }

    @Override
    public void setTransactionHash(byte[] hash) {
        this.transactionHash = hash;
    }

    @Override
    public byte[] getHashOfOriginTransaction() {
        return this.originTransactionHash;
    }

    @Override
    public TransactionInterface getTransaction() {
        return tx;
    }

    @Override
    public int getTransactionKind() {
        return tx.getType().toInt();
    }

    @Override
    public Address getDestinationAddress() {
        return tx.getDestinationAddress();
    }

    @Override
    public void setDestinationAddress(Address address) {
        throw new AssertionError("No equivalent concept exists in the Avm for this.");
    }

    @Override
    public Address getMinerAddress() {
        return blockCoinbase;
    }

    @Override
    public Address getSenderAddress() {
        return tx.getSenderAddress();
    }

    @Override
    public Address getOriginAddress() {
        return origin;
    }

    @Override
    public BigInteger getTransferValue() {
        return tx.getValueAsBigInteger();
    }

    @Override
    public byte[] getTransactionData() {
        return tx.getData();
    }

    @Override
    public long getTransactionEnergy() {
        return tx.getEnergyLimit() - tx.getTransactionCost();
    }

    @Override
    public long getTransactionEnergyPrice() {
        return tx.getEnergyPrice();
    }

    @Override
    public byte[] getTransactionHash() {
        return tx.getTransactionHash();
    }

    @Override
    public long getBlockTimestamp() {
        return blockTimestamp;
    }

    @Override
    public long getBlockNumber() {
        return blockNumber;
    }

    @Override
    public long getBlockEnergyLimit() {
        return blockEnergyLimit;
    }

    @Override
    public long getBlockDifficulty() {
        return blockDifficulty.longValue();
    }

    @Override
    public int getTransactionStackDepth() {
        return internalCallDepth;
    }

    @Override
    public TransactionSideEffects getSideEffects() {
        return this.sideEffects;
    }

    @Override
    public int getFlags() {
        throw RuntimeAssertionError.unimplemented("No equivalent concept exists in the Avm for this.");
    }

    @Override
    public byte[] toBytes() {
        throw RuntimeAssertionError.unimplemented("No equivalent concept exists in the Avm for this.");
    }

}

package org.aion.kernel;


import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.util.HashUtils;
import java.math.BigInteger;

import java.nio.ByteBuffer;

public class TransactionContextImpl implements TransactionContext {

    private Transaction tx;
    private byte[] origin;
    private int internalCallDepth;
    private long blockNumber;
    private long blockTimestamp;
    private long blockEnergyLimit;
    private byte[] blockCoinbase;
    private byte[] blockPrevHash;
    private BigInteger blockDifficulty;

    public TransactionContextImpl(Transaction tx, Block block) {
        this.tx = tx;
        this.origin = tx.getSenderAddress().toBytes();
        this.internalCallDepth = 1;

        this.blockNumber = block.getNumber();
        this.blockTimestamp = block.getTimestamp();
        this.blockEnergyLimit = block.getEnergyLimit();
        this.blockCoinbase = block.getCoinbase();
        this.blockPrevHash = block.getPrevHash();
        this.blockDifficulty = block.getDifficulty();
    }

    public TransactionContextImpl(TransactionContext parent, Transaction tx) {
        this.tx = tx;
        this.origin = parent.getOrigin();
        this.internalCallDepth = parent.getInternalCallDepth() + 1;

        this.blockNumber = parent.getBlockNumber();
        this.blockTimestamp = parent.getBlockTimestamp();
        this.blockEnergyLimit = parent.getBlockEnergyLimit();
        this.blockCoinbase = parent.getBlockCoinbase();
        this.blockPrevHash = parent.getBlockPreviousHash();
        this.blockDifficulty = parent.getBlockDifficulty();
    }

    @Override
    public boolean isCreate() {
        return tx.getType() == Transaction.Type.CREATE;
    }

    @Override
    public boolean isBalanceTransfer() {
        return tx.getType() == Transaction.Type.BALANCE_TRANSFER;
    }

    @Override
    public boolean isGarbageCollectionRequest() {
        return tx.getType() == Transaction.Type.GARBAGE_COLLECT;
    }

    @Override
    public byte[] getAddress() {
        if (isCreate()) {
            ByteBuffer buffer = ByteBuffer.allocate(32 + 8).put(tx.getSenderAddress().toBytes()).putLong(tx.getNonceAsLong());
            byte[] hash = HashUtils.sha256(buffer.array());
            hash[0] = NodeEnvironment.CONTRACT_PREFIX;
            return hash;
        } else {
            return tx.getDestinationAddress().toBytes();
        }
    }

    @Override
    public byte[] getCaller() {
        return tx.getSenderAddress().toBytes();
    }

    @Override
    public byte[] getOrigin() {
        return origin;
    }

    @Override
    public long getNonce() {
        return tx.getNonceAsLong();
    }

    @Override
    public BigInteger getValue() {
        return tx.getValueAsBigInteger();
    }

    @Override
    public byte[] getData() {
        return tx.getData();
    }

    @Override
    public long getEnergyLimit() {
        return tx.getEnergyLimit();
    }

    @Override
    public long getEnergyPrice() {
        return tx.getEnergyPrice();
    }

    @Override
    public byte[] getTransactionHash() {
        return tx.getTransactionHash();
    }

    @Override
    public long getBasicCost() {
        return tx.getTransactionCost();
    }

    @Override
    public long getTransactionTimestamp() { return tx.getTimestampAsLong(); }

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
    public byte[] getBlockCoinbase() {
        return blockCoinbase;
    }

    @Override
    public byte[] getBlockPreviousHash() {
        return blockPrevHash;
    }

    @Override
    public BigInteger getBlockDifficulty() {
        return blockDifficulty;
    }

    @Override
    public int getInternalCallDepth() {
        return internalCallDepth;
    }
}

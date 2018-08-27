package org.aion.kernel;


import org.aion.avm.core.util.HashUtils;
import java.math.BigInteger;

import java.nio.ByteBuffer;

public class TransactionContextImpl implements TransactionContext {

    private Transaction tx;

    private byte[] origin;
    private long blockNumber;
    private long blockTimestamp;
    private long blockEnergyLimit;
    private byte[] blockCoinbase;
    private byte[] blockPrevHash;
    private BigInteger blockDifficulty;

    public TransactionContextImpl(Transaction tx, Block block) {
        this.tx = tx;
        this.origin = tx.getFrom();

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
    public byte[] getAddress() {
        if (isCreate()) {
            ByteBuffer buffer = ByteBuffer.allocate(32 + 8).put(tx.getFrom()).putLong(tx.getNonce());
            return HashUtils.sha256(buffer.array());
        } else {
            return tx.getTo();
        }
    }

    @Override
    public byte[] getCaller() {
        return tx.getFrom();
    }

    @Override
    public byte[] getOrigin() {
        return origin;
    }

    @Override
    public long getNonce() {
        return tx.getNonce();
    }

    @Override
    public long getValue() {
        return tx.getValue();
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
    public long getEneryPrice() {
        return tx.getEnergyPrice();
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
}

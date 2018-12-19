package org.aion.kernel;


import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.util.HashUtils;
import java.math.BigInteger;

import java.nio.ByteBuffer;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionInterface;
import org.aion.vm.api.interfaces.TransactionSideEffects;

public class TransactionContextImpl implements TransactionContext {

    private Transaction tx;
    private TransactionSideEffects sideEffects;
    private byte[] transactionHash, originTransactionHash;
    private Address origin;
    private int internalCallDepth;
    private long blockNumber;
    private long blockTimestamp;
    private long blockEnergyLimit;
    private Address blockCoinbase;
    private BigInteger blockDifficulty;
    private Address contract;

    public TransactionContextImpl(Transaction tx, Block block) {
        this.tx = tx;
        this.transactionHash = tx.getTransactionHash();
        this.originTransactionHash = this.transactionHash;
        this.origin = tx.getSenderAddress();
        this.internalCallDepth = 1;

        this.blockNumber = block.getNumber();
        this.blockTimestamp = block.getTimestamp();
        this.blockEnergyLimit = block.getEnergyLimit();
        this.blockCoinbase = block.getCoinbase();
        this.blockDifficulty = block.getDifficulty();
        this.sideEffects = new SideEffects();
    }

    public TransactionContextImpl(TransactionContext parent, Transaction tx) {
        this.tx = tx;
        this.transactionHash = tx.getTransactionHash();
        this.originTransactionHash = this.transactionHash;
        this.origin = parent.getOriginAddress();
        this.internalCallDepth = parent.getTransactionStackDepth() + 1;

        this.blockNumber = parent.getBlockNumber();
        this.blockTimestamp = parent.getBlockTimestamp();
        this.blockEnergyLimit = parent.getBlockEnergyLimit();
        this.blockCoinbase = parent.getMinerAddress();
        this.blockDifficulty = BigInteger.valueOf(parent.getBlockDifficulty());
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
    public Address getContractAddress() {
        if (contract == null) {
            ByteBuffer buffer = ByteBuffer.allocate(32 + 8).put(tx.getSenderAddress().toBytes()).putLong(tx.getNonceAsLong());
            byte[] hash = HashUtils.sha256(buffer.array());
            hash[0] = NodeEnvironment.CONTRACT_PREFIX;
            contract = AvmAddress.wrap(hash);
        }
        return contract;
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

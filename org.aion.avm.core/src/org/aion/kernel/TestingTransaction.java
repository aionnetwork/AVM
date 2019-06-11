package org.aion.kernel;

import java.math.BigInteger;

import i.RuntimeAssertionError;
import org.aion.avm.core.BillingRules;
import org.aion.avm.core.util.Helpers;
import org.aion.vm.api.types.Address;

import java.util.Arrays;
import java.util.Objects;
import org.aion.vm.api.interfaces.TransactionInterface;


public final class TestingTransaction implements TransactionInterface {
    public static TestingTransaction create(Address from, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        return new TestingTransaction(from, null, nonce, value, data, energyLimit, energyPrice);
    }

    public static TestingTransaction call(Address from, Address to, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        if(to == null) {
            throw new IllegalArgumentException("The transaction to can't be NULL for non-CREATE");
        }
        return new TestingTransaction(from, to, nonce, value, data, energyLimit, energyPrice);
    }

    private final byte[] from;
    private final byte[] to;
    private final BigInteger nonce;
    private final BigInteger value;
    private final byte[] data;
    private final long energyLimit;
    private final long energyPrice;
    private final byte[] transactionHash;

    long timestamp;
    byte[] timestampAsBytes;

    private TestingTransaction(Address from, Address to, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        Objects.requireNonNull(from, "The transaction `from` can't be NULL");

        this.from = from.toBytes();
        this.to = (to == null) ? null : to.toBytes();
        this.nonce = nonce;
        this.value = value;
        this.data = data;
        this.energyLimit = energyLimit;
        this.energyPrice = energyPrice;
        this.transactionHash = new byte[32];
    }

    @Override
    public byte[] getTimestamp() {
        if (this.timestampAsBytes == null) {
            this.timestampAsBytes = BigInteger.valueOf(this.timestamp).toByteArray();
        }
        return this.timestampAsBytes;
    }

    /**
     * Returns the {@link org.aion.vm.api.interfaces.VirtualMachine} that this transaction must be
     * executed by in the case of a contract creation.
     *
     * @return The VM to use to create a new contract.
     */
    @Override
    public byte getTargetVM() {
        throw RuntimeAssertionError.unreachable("getTargetVM is not expected in vm transaction.");
    }


    @Override
    public Address getSenderAddress() {
        return org.aion.vm.api.types.Address.wrap(from);
    }

    @Override
    public Address getDestinationAddress() {
        // The destination can be null in the case of contract creation.
        return (to == null) ? null : org.aion.vm.api.types.Address.wrap(to);
    }

    @Override
    public Address getContractAddress() {
        throw RuntimeAssertionError.unreachable("getContractAddress is not expected in vm transaction.");
    }

    @Override
    public byte[] getNonce() {
        return this.nonce.toByteArray();
    }

    @Override
    public byte[] getValue() {
        return this.value.toByteArray();
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public long getEnergyLimit() {
        return energyLimit;
    }

    @Override
    public long getEnergyPrice() {
        return energyPrice;
    }

    @Override
    public byte[] getTransactionHash() {
        return transactionHash;
    }

    @Override
    public long getTransactionCost() {
        return BillingRules.getBasicTransactionCost(getData());
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean isContractCreationTransaction() {
        return this.to == null;
    }

    @Override
    public byte getKind() {
        throw RuntimeAssertionError.unreachable("getKind is not expected in vm transaction.");
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "type=" + (this.isContractCreationTransaction()? "CREATE" :"CALL" ) +
                ", from=" + Helpers.bytesToHexString(Arrays.copyOf(from, 4)) +
                ", to=" + Helpers.bytesToHexString(Arrays.copyOf(to, 4)) +
                ", value=" + value +
                ", data=" + Helpers.bytesToHexString(data) +
                ", energyLimit=" + energyLimit +
                ", transactionHash" + Helpers.bytesToHexString(transactionHash) +
                '}';
    }
}

package org.aion.kernel;

import java.math.BigInteger;

import org.aion.avm.core.BillingRules;
import org.aion.avm.core.util.Helpers;

import java.util.Arrays;
import java.util.Objects;
import org.aion.vm.api.interfaces.Address;
import org.aion.vm.api.interfaces.TransactionInterface;


public class Transaction implements TransactionInterface {
    public static Transaction create(Address from, long nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        return new Transaction(Type.CREATE, from, null, nonce, value, data, energyLimit, energyPrice);
    }

    public static Transaction call(Address from, Address to, long nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        return new Transaction(Type.CALL, from, to, nonce, value, data, energyLimit, energyPrice);
    }

    public static Transaction balanceTransfer(Address from, Address to, long nonce, BigInteger value, long energyPrice) {
        return new Transaction(Type.BALANCE_TRANSFER, from, to, nonce, value, new byte[0], BillingRules.BASIC_COST, energyPrice);
    }

    public static Transaction garbageCollect(Address target, long nonce, long energyLimit, long energyPrice) {
        // This may seem a bit odd but we state that the "target" of the GC is the "sender" address.
        // This is because, on a conceptual level, the GC is "sent to itself" but also allows the nonce check to be consistent.
        return new Transaction(Type.GARBAGE_COLLECT, target, target, nonce, BigInteger.ZERO, new byte[0], energyLimit, energyPrice);
    }

    public enum Type {
        /**
         * The CREATE is used to deploy a new DApp.
         */
        CREATE,
        /**
         * The CALL is used when sending an invocation to an existing DApp.
         */
        CALL,
        /**
         * The BALANCE_TRANSFER is used when ONLY a balance transfer is requested, without a DApp call or deployment.
         */
        BALANCE_TRANSFER,
        /**
         * The GARBAGE_COLLECT is a special transaction which asks that the target DApp's storage be deterministically collected.
         * Note that this is the only transaction type which will result in a negative TransactionResult.energyUsed.
         */
        GARBAGE_COLLECT,
    }

    Type type;

    byte[] from;

    byte[] to;

    long nonce;

    BigInteger value;

    long timestamp;

    byte[] timestampAsBytes;

    byte[] data;

    long energyLimit;

    long energyPrice;

    byte[] transactionHash;

    byte vm;

    protected Transaction(Type type, Address from, Address to, long nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        Objects.requireNonNull(type, "The transaction `type` can't be NULL");
        Objects.requireNonNull(from, "The transaction `from` can't be NULL");
        if (type == Type.CREATE) {
           if (to != null) {
               throw new IllegalArgumentException("The transaction `to` has to be NULL for CREATE");
           }
        } else {
            Objects.requireNonNull(to, "The transaction `to`  can't be NULL for non-CREATE");
        }

        this.type = type;
        this.from = from.toBytes();
        this.to = (to == null) ? null : to.toBytes();
        this.nonce = nonce;
        this.value = value;
        this.data = data;
        this.energyLimit = energyLimit;
        this.energyPrice = energyPrice;
        //TODO: Make sure this constructor is only used for testing purpose. Kernel should always pass AVM the transaction hash.
        this.transactionHash = Helpers.randomBytes(32);
    }

    protected Transaction(Type type, byte[] from, byte[] to, long nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice, byte[] transactionHash) {
        Objects.requireNonNull(type, "The transaction `type` can't be NULL");
        Objects.requireNonNull(from, "The transaction `from` can't be NULL");
        if (type == Type.CREATE) {
            if (to != null) {
                throw new IllegalArgumentException("The transaction `to` has to be NULL for CREATE");
            }
        } else {
            Objects.requireNonNull(to, "The transaction `to`  can't be NULL for non-CREATE");
        }

        this.type = type;
        this.from = from;
        this.to = to;
        this.nonce = nonce;
        this.value = value;
        this.data = data;
        this.energyLimit = energyLimit;
        this.energyPrice = energyPrice;
        this.transactionHash = transactionHash;
    }

    @Override
    public byte[] getTimestamp() {
        if (this.timestampAsBytes == null) {
            this.timestampAsBytes = BigInteger.valueOf(this.timestamp).toByteArray();
        }
        return this.timestampAsBytes;
    }

    long getTimestampAsLong() {
        return timestamp;
    }

    /**
     * Returns the {@link org.aion.vm.api.interfaces.VirtualMachine} that this transaction must be
     * executed by in the case of a contract creation.
     *
     * @return The VM to use to create a new contract.
     */
    @Override
    public byte getTargetVM() {
        return this.vm;
    }

    /**
     * Returns the type of transactional logic that this transaction will cause to be executed.
     */
    public Type getType() {
        return type;
    }

    @Override
    public AvmAddress getSenderAddress() {
        return AvmAddress.wrap(from);
    }

    @Override
    public AvmAddress getDestinationAddress() {
        return AvmAddress.wrap(to);
    }

    @Override
    public byte[] getNonce() {
        return BigInteger.valueOf(this.nonce).toByteArray();
    }

    long getNonceAsLong() {
        return nonce;
    }

    @Override
    public byte[] getValue() {
        return this.value.toByteArray();
    }

    BigInteger getValueAsBigInteger() {
        return value;
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
    public String toString() {
        return "Transaction{" +
                "type=" + type +
                ", from=" + Helpers.bytesToHexString(Arrays.copyOf(from, 4)) +
                ", to=" + Helpers.bytesToHexString(Arrays.copyOf(to, 4)) +
                ", value=" + value +
                ", data=" + Helpers.bytesToHexString(data) +
                ", energyLimit=" + energyLimit +
                ", transactionHash" + Helpers.bytesToHexString(transactionHash) +
                '}';
    }
}

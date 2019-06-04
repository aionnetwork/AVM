package org.aion.avm.core.types;

import i.RuntimeAssertionError;
import org.aion.types.AionAddress;
import org.aion.vm.api.interfaces.InternalTransactionInterface;

import java.math.BigInteger;

/**
 * Represents an AION internal transaction
 */
public final class InternalTransaction implements InternalTransactionInterface {

    private boolean rejected;

    // TODO: We should probably change these to just be AionAddress
    private final byte[] from;
    private final byte[] to;
    private final BigInteger nonce;
    private final BigInteger value;
    private final byte[] data;
    private final long energyLimit;
    private final long energyPrice;
    private final byte[] transactionHash;

    public static InternalTransaction buildTransactionOfTypeCreate(AionAddress from, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice){
        return new InternalTransaction(from, null, nonce, value, data, energyLimit, energyPrice);
    }

    public static InternalTransaction buildTransactionOfTypeCall(AionAddress from, AionAddress to, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice){
        if(to == null)  {
            throw new IllegalArgumentException("The transaction to can't be NULL for non-CREATE");
        }
        return new InternalTransaction(from, to, nonce, value, data, energyLimit, energyPrice);
    }

    public InternalTransaction(AionAddress from, AionAddress to, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        require(from != null, "The transaction from can't be NULL");
        require(nonce != null, "The transaction nonce can't be NULL");
        require(data != null, "The transaction data can't be NULL");
        require(value != null, "The transaction value can't be NULL");

        this.from = from.toByteArray();
        this.to = (to == null) ? null : to.toByteArray();
        this.nonce = nonce;
        this.value = value;
        this.data = data;
        this.energyLimit = energyLimit;
        this.energyPrice = energyPrice;
        //Note: Transaction hash is not set for internal transactions.
        this.transactionHash = new byte[32];
    }

    @Override
    public byte[] getTransactionHash() {
        return transactionHash;
    }

    @Override
    public AionAddress getSenderAddress() {
        return new AionAddress(from);
    }

    @Override
    public AionAddress getDestinationAddress() {
        // The destination can be null in the case of contract creation.
        return (to == null) ? null : new AionAddress(to);
    }

    @Override
    public AionAddress getContractAddress() {
        throw RuntimeAssertionError.unreachable("getContractAddress is not expected in internal transaction.");
    }

    @Override
    public byte[] getNonce() {
        return nonce.toByteArray();
    }

    @Override
    public byte[] getValue() {
        return value.toByteArray();
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public byte getTargetVM() {
        throw RuntimeAssertionError.unreachable("getTargetVM is not expected in internal transaction.");
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
    public long getTransactionCost() {
        return 0;
    }

    @Override
    public byte[] getTimestamp() {
        throw RuntimeAssertionError.unreachable("getTimestamp is not expected in internal transaction.");
    }

    @Override
    public boolean isContractCreationTransaction() {
        return to == null;
    }

    @Override
    public byte getKind() {
        throw RuntimeAssertionError.unreachable("getKind is not expected in internal transaction.");
    }

    @Override
    public void markAsRejected() {
        this.rejected = true;
    }

    @Override
    public boolean isRejected() {
        return rejected;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}

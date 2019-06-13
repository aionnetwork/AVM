package org.aion.avm.core.types;

import org.aion.types.AionAddress;

import java.math.BigInteger;

/**
 * Represents an AION internal transaction
 */
public final class InternalTransaction {

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

    public byte[] getTransactionHash() {
        return transactionHash;
    }

    public AionAddress getSenderAddress() {
        return new AionAddress(from);
    }

    public AionAddress getDestinationAddress() {
        // The destination can be null in the case of contract creation.
        return (to == null) ? null : new AionAddress(to);
    }

    public byte[] getNonce() {
        return nonce.toByteArray();
    }

    public byte[] getValue() {
        return value.toByteArray();
    }

    public byte[] getData() {
        return data;
    }

    public long getEnergyLimit() {
        return energyLimit;
    }

    public long getEnergyPrice() {
        return energyPrice;
    }

    public long getTransactionCost() {
        return 0;
    }

    public boolean isContractCreationTransaction() {
        return to == null;
    }

    public void markAsRejected() {
        this.rejected = true;
    }

    public boolean isRejected() {
        return rejected;
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}

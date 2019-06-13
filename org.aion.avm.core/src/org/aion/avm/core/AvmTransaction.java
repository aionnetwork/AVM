package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.types.AionAddress;


/**
 * An internal representation of the TransactionInterface the kernel passed to us.
 * This allows us to pass the data around as an immutable, plain-old-data type, instead of a container of behaviour.
 * NOTE:  The destinationAddress will be the new contract address, in the case of creation.
 */
public class AvmTransaction {
    public final AionAddress senderAddress;
    public final AionAddress destinationAddress;
    public final byte[] transactionHash;
    public final BigInteger value;
    public final BigInteger nonce;
    public final long energyPrice;
    public final long energyLimit;
    public final boolean isCreate;
    public final byte[] data;

    public AvmTransaction(AionAddress senderAddress
            , AionAddress destinationAddress
            , byte[] transactionHash
            , BigInteger value
            , BigInteger nonce
            , long energyPrice
            , long energyLimit
            , boolean isCreate
            , byte[] data
        ) {
        // We will throw any error here as IllegalArgumentException, since this is almost directly called from outside of from Blockchain, which defines this.
        if (null == senderAddress) {
            throw new IllegalArgumentException("No sender");
        }
        if (null == destinationAddress) {
            throw new IllegalArgumentException("No destination");
        }
        if (null == transactionHash) {
            throw new IllegalArgumentException("No transaction hash");
        }
        if (value.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Negative value");
        }
        if (nonce.compareTo(BigInteger.ZERO) < 0) {
            throw new IllegalArgumentException("Negative nonce");
        }
        if (energyPrice < 0) {
            throw new IllegalArgumentException("Negative energy price");
        }
        if (energyLimit < 0) {
            throw new IllegalArgumentException("Negative energy limit");
        }
        if (null == data) {
            throw new IllegalArgumentException("Null data");
        }
        
        this.senderAddress = senderAddress;
        this.destinationAddress = destinationAddress;
        this.transactionHash = transactionHash;
        this.value = value;
        this.nonce = nonce;
        this.energyPrice = energyPrice;
        this.energyLimit = energyLimit;
        this.isCreate = isCreate;
        this.data = data;
    }
}

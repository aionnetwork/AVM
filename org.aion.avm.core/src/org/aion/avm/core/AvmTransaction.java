package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.types.Address;
import org.aion.vm.api.interfaces.TransactionInterface;


/**
 * An internal representation of the TransactionInterface the kernel passed to us.
 * This allows us to pass the data around as an immutable, plain-old-data type, instead of a container of behaviour.
 * NOTE:  The destinationAddress will be the new contract address, in the case of creation.
 */
public class AvmTransaction {
    /**
     * Factory method to create the AvmTransaction data type from a TransactionInterface.
     * 
     * @param capabilities The capabilities for generating a new contract address, if this is a create call.
     * @param external The transaction we were given.
     * @return The new AvmTransaction instance.
     * @throws IllegalArgumentException If any elements of external are statically invalid.
     */
    public static AvmTransaction from(IExternalCapabilities capabilities, TransactionInterface external) throws IllegalArgumentException {
        boolean isCreate = external.isContractCreationTransaction();
        
        Address senderAddress = external.getSenderAddress();
        Address destinationAddress = isCreate
                ? capabilities.generateContractAddress(external)
                : external.getDestinationAddress();
        byte[] transactionHash = external.getTransactionHash();
        BigInteger value = new BigInteger(1, external.getValue());
        BigInteger nonce = new BigInteger(1, external.getNonce());
        long energyPrice = external.getEnergyPrice();
        long energyLimit = external.getEnergyLimit();
        byte[] data = external.getData();
        return new AvmTransaction(senderAddress
                , destinationAddress
                , transactionHash
                , value
                , nonce
                , energyPrice
                , energyLimit
                , isCreate
                , data
            );
    }


    public final Address senderAddress;
    public final Address destinationAddress;
    public final byte[] transactionHash;
    public final BigInteger value;
    public final BigInteger nonce;
    public final long energyPrice;
    public final long energyLimit;
    public final boolean isCreate;
    public final byte[] data;

    private AvmTransaction(Address senderAddress
            , Address destinationAddress
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

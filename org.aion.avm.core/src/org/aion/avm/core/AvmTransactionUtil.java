package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.core.types.InternalTransaction;
import org.aion.types.AionAddress;

public class AvmTransactionUtil {

    /**
     * Factory method to create a 'call' AvmTransaction transaction.
     */
    public static AvmTransaction call(AionAddress sender, AionAddress destination, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        return fromInternal(sender
            , destination
            , new byte[32]
            , value.toByteArray()
            , nonce.toByteArray()
            , energyPrice
            , energyLimit
            , false
            , data
        );
    }

    /**
     * Factory method to create a 'create' AvmTransaction transaction.
     */
    public static AvmTransaction create(AionAddress sender, BigInteger nonce, BigInteger value, byte[] data, long energyLimit, long energyPrice) {
        return fromInternal(sender
            , null
            , new byte[32]
            , value.toByteArray()
            , nonce.toByteArray()
            , energyPrice
            , energyLimit
            , true
            , data
        );
    }

    /**
     * Factory method to create the AvmTransaction data type from an InternalTransaction.
     *
     * @param capabilities The capabilities for generating a new contract address, if this is a create call.
     * @param external The transaction we were given.
     * @return The new AvmTransaction instance.
     * @throws IllegalArgumentException If any elements of external are statically invalid.
     */
    public static AvmTransaction fromInternalTransaction(IExternalCapabilities capabilities, InternalTransaction external) {
        boolean isCreate = external.isContractCreationTransaction();
        AionAddress destinationAddress = isCreate
            ? capabilities.generateContractAddress(external)
            : external.getDestinationAddress();

        return fromInternal(external.getSenderAddress()
            , destinationAddress
            , external.getTransactionHash()
            , external.getValue()
            , external.getNonce()
            , external.getEnergyPrice()
            , external.getEnergyLimit()
            , isCreate
            , external.getData()
        );
    }

    private static AvmTransaction fromInternal(AionAddress senderAddress, AionAddress destinationAddress, byte[] transactionHash, byte[] value, byte[] nonce, long energyPrice, long energyLimit, boolean isCreate, byte[] data) {
        return new AvmTransaction(senderAddress
            , destinationAddress
            , transactionHash
            , new BigInteger(1, value)
            , new BigInteger(1, nonce)
            , energyPrice
            , energyLimit
            , isCreate
            , data
        );
    }
}

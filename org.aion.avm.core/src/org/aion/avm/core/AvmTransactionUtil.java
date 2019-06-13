package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.core.types.InternalTransaction;
import org.aion.kernel.TestingTransaction;
import org.aion.types.AionAddress;
import org.aion.vm.api.interfaces.TransactionInterface;

public class AvmTransactionUtil {

    /**
     * Factory method to create the AvmTransaction data type from an InternalTransaction.
     *
     * @param capabilities The capabilities for generating a new contract address, if this is a create call.
     * @param external The transaction we were given.
     * @return The new AvmTransaction instance.
     * @throws IllegalArgumentException If any elements of external are statically invalid.
     */
    public static AvmTransaction from(IExternalCapabilities capabilities, InternalTransaction external) {
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

    /**
     * Factory method to create the AvmTransaction data type from a TestingTransaction.
     *
     * @param capabilities The capabilities for generating a new contract address, if this is a create call.
     * @param external The transaction we were given.
     * @return The new AvmTransaction instance.
     * @throws IllegalArgumentException If any elements of external are statically invalid.
     */
    public static AvmTransaction from(IExternalCapabilities capabilities, TestingTransaction external) {
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

    /**
     * Factory method to create the AvmTransaction data type from a TransactionInterface.
     *
     * @param capabilities The capabilities for generating a new contract address, if this is a create call.
     * @param external The transaction we were given.
     * @return The new AvmTransaction instance.
     * @throws IllegalArgumentException If any elements of external are statically invalid.
     */
    public static AvmTransaction from(IExternalCapabilities capabilities, TransactionInterface external) {
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

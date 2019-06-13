package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.types.AionAddress;
import org.aion.vm.api.interfaces.TransactionInterface;

public class AvmTransactionUtil {

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

        AionAddress senderAddress = external.getSenderAddress();
        AionAddress destinationAddress = isCreate
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

}

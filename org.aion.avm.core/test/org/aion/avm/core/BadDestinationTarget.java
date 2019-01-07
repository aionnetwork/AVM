package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class BadDestinationTarget {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(BadDestinationTarget.class, BlockchainRuntime.getData());
    }

    public static void callDestinationNoExceptionCatching(Address destination) {
        BlockchainRuntime.call(destination, BigInteger.ZERO, new byte[0], BlockchainRuntime.getEnergyLimit());
    }

    public static void callDestinationAndCatchException(Address destination) {
        try {
            BlockchainRuntime.call(destination, BigInteger.ZERO, new byte[0], BlockchainRuntime.getEnergyLimit());
        } catch (IllegalArgumentException e) {
            BlockchainRuntime.println(e.getMessage());
        }
    }

}

package org.aion.avm.tooling;

import java.math.BigInteger;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.tooling.abi.Callable;

public class BadDestinationTarget {

    @Callable
    public static void callDestinationNoExceptionCatching(Address destination) {
        BlockchainRuntime.call(destination, BigInteger.ZERO, new byte[0], BlockchainRuntime.getEnergyLimit());
    }

    @Callable
    public static void callDestinationAndCatchException(Address destination) {
        try {
            BlockchainRuntime.call(destination, BigInteger.ZERO, new byte[0], BlockchainRuntime.getEnergyLimit());
        } catch (IllegalArgumentException e) {
            BlockchainRuntime.println(e.getMessage());
        }
    }

}

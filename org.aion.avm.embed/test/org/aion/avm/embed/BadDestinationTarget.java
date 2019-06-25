package org.aion.avm.embed;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class BadDestinationTarget {

    @Callable
    public static void callDestinationNoExceptionCatching(Address destination) {
        Blockchain.call(destination, BigInteger.ZERO, new byte[0], Blockchain.getEnergyLimit());
    }

    @Callable
    public static void callDestinationAndCatchException(Address destination) {
        try {
            Blockchain.call(destination, BigInteger.ZERO, new byte[0], Blockchain.getEnergyLimit());
        } catch (IllegalArgumentException e) {
            Blockchain.println(e.getMessage());
        }
    }

}

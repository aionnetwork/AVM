package org.aion.avm.embed.blockchainruntime;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

import java.math.BigInteger;

public class SelfDestructTarget {

    @Callable
    public static void selfDestruct(Address beneficiary) {
        Blockchain.selfDestruct(beneficiary);
    }

    @Callable
    public static void selfDestructMulti(Address beneficiary) {
        for (int i = 0; i < 5; i++) {
            Blockchain.selfDestruct(beneficiary);
        }
    }

    @Callable
    public static void killOtherContracts(Address[] addresses, byte[] txData) {
        for (Address addr : addresses) {
            Blockchain.call(addr, BigInteger.ONE, txData, Blockchain.getRemainingEnergy());
        }
    }

    @Callable
    public static void reentrantSelfDestruct(byte[] txData) {
        Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, txData, Blockchain.getRemainingEnergy());
    }

    @Callable
    public static void selfDestructDifferentAddress(Address[] addresses) {
        for(int i =0; i< addresses.length; i++) {
            Blockchain.selfDestruct(addresses[i]);
        }
    }
}

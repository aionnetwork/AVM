package org.aion.avm.core.bootstrapmethods;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class EnergyChargeConsistencyTarget {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(EnergyChargeConsistencyTarget.class, BlockchainRuntime.getData());
    }

    public static void run() {
        Runnable runnable = () -> {
            String hello = "Hello";
            String world = "world";
            String string = hello + " " + world + "!";
            BlockchainRuntime.println(string);
        };
        runnable.run();
    }

}

package org.aion.avm.tooling.bootstrapmethods;

import avm.BlockchainRuntime;
import org.aion.avm.tooling.abi.Callable;

public class EnergyChargeConsistencyTarget {

    @Callable
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

package org.aion.avm.tooling.bootstrapmethods;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class EnergyChargeConsistencyTarget {

    @Callable
    public static void run() {
        Runnable runnable = () -> {
            String hello = "Hello";
            String world = "world";
            String string = hello + " " + world + "!";
            Blockchain.println(string);
        };
        runnable.run();
    }

}

package org.aion.avm.embed.benchmark;

import org.aion.avm.tooling.abi.Callable;


/**
 * Used to measure the path-length into the smart contract.  The callable method does nothing.
 */
public class SimpleContractWithABI {
    @Callable
    public static void myFunction() {
    }
}

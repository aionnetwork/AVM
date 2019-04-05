package org.aion.avm.tooling;

import avm.BlockchainRuntime;
import org.aion.avm.tooling.abi.Callable;


public class EdverifyTestTargetClass {

    private EdverifyTestTargetClass(){
        // initialize to default
    }

    @Callable
    public static boolean callEdverify(byte[] message, byte[] signature, byte[]pk) throws IllegalArgumentException {
        return BlockchainRuntime.edVerify(message, signature, pk);
    }
}

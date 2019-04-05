package org.aion.avm.tooling;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;


public class EdverifyTestTargetClass {

    private EdverifyTestTargetClass(){
        // initialize to default
    }

    @Callable
    public static boolean callEdverify(byte[] message, byte[] signature, byte[]pk) throws IllegalArgumentException {
        return Blockchain.edVerify(message, signature, pk);
    }
}

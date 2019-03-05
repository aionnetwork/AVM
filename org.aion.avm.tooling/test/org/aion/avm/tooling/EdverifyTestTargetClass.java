package org.aion.avm.tooling;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


public class EdverifyTestTargetClass {

    private EdverifyTestTargetClass(){
        // initialize to default
    }

    public static boolean callEdverify(byte[] message, byte[] signature, byte[]pk) throws IllegalArgumentException {
        return BlockchainRuntime.edVerify(message, signature, pk);
    }

    /**
     * Entry point at a transaction call.
     */
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(EdverifyTestTargetClass.class, BlockchainRuntime.getData());
    }
}

package org.aion.avm.embed.blockchainruntime;

import org.aion.avm.userlib.abi.ABIDecoder;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class RequireTarget {

    static {
        byte[] data = Blockchain.getData();
        if ((data != null) && (data.length > 0)) {
            ABIDecoder decoder = new ABIDecoder(data);
            boolean dataAsBoolean = decoder.decodeOneBoolean();
            Blockchain.require(dataAsBoolean);
        }
    }

    @Callable
    public static void require(boolean condition) {
        Blockchain.require(condition);

        if (!condition) {
            throw new AssertionError("If I am here then condition MUST be true, but it is false!");
        }

    }

    @Callable
    public static void requireAndTryToCatch() {
        try {
            Blockchain.require(false);
        } catch (Exception e) {
            Blockchain.println(e.getMessage());
        }
    }

}

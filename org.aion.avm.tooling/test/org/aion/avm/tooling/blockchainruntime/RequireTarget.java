package org.aion.avm.tooling.blockchainruntime;

import org.aion.avm.userlib.abi.ABIDecoder;
import avm.BlockchainRuntime;
import org.aion.avm.tooling.abi.Callable;

public class RequireTarget {

    static {
        byte[] data = BlockchainRuntime.getData();
        if ((data != null) && (data.length > 0)) {
            ABIDecoder decoder = new ABIDecoder(data);
            boolean dataAsBoolean = decoder.decodeOneBoolean();
            BlockchainRuntime.require(dataAsBoolean);
        }
    }

    @Callable
    public static void require(boolean condition) {
        BlockchainRuntime.require(condition);

        if (!condition) {
            throw new AssertionError("If I am here then condition MUST be true, but it is false!");
        }

    }

    @Callable
    public static void requireAndTryToCatch() {
        try {
            BlockchainRuntime.require(false);
        } catch (Exception e) {
            BlockchainRuntime.println(e.getMessage());
        }
    }

}

package org.aion.avm.tooling.blockchainruntime;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.tooling.abi.Callable;

public class RequireTarget {

    static {
        byte[] data = BlockchainRuntime.getData();
        if ((data != null) && (data.length > 0)) {
            boolean dataAsBoolean = (boolean) ABIDecoder.decodeOneObject(data);
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

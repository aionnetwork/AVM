package org.aion.avm.tooling.blockchainruntime;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class RequireTarget {

    static {
        byte[] data = BlockchainRuntime.getData();
        if ((data != null) && (data.length > 0)) {
            boolean dataAsBoolean = (boolean) ABIDecoder.decodeOneObject(data);
            BlockchainRuntime.require(dataAsBoolean);
        }
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(RequireTarget.class, BlockchainRuntime.getData());
    }

    public static void require(boolean condition) {
        BlockchainRuntime.require(condition);

        if (!condition) {
            throw new AssertionError("If I am here then condition MUST be true, but it is false!");
        }

    }

    public static void requireAndTryToCatch() {
        try {
            BlockchainRuntime.require(false);
        } catch (Exception e) {
            BlockchainRuntime.println(e.getMessage());
        }
    }

}

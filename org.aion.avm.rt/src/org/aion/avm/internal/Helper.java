package org.aion.avm.internal;

import org.aion.avm.rt.BlockchainRuntime;

public class Helper {

    private static ThreadLocal<BlockchainRuntime> blockchainRuntime = new ThreadLocal<>();

    public static void setBlockchainRuntime(BlockchainRuntime rt) {
        blockchainRuntime.set(rt);
    }

    public static <T> org.aion.avm.java.lang.Class<T> wrapAsClass(Class<T> input) {
        return new org.aion.avm.java.lang.Class<T>(input);
    }

    public static org.aion.avm.java.lang.String wrapAsString(String input) {
        return new org.aion.avm.java.lang.String(input);
    }
}

package org.aion.avm.tooling.abi;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class DAppWithMainNoFallbackTarget {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(DAppWithMainNoFallbackTarget.class, BlockchainRuntime.getData());
    }

    @Callable()
    public static boolean test1(boolean b) {
        return true;
    }

    @Callable()
    public static boolean test2(int i, String s, long[] l) {
        return true;
    }

    @Deprecated
    public static boolean test3(int i, String s, long[] l) {
        return true;
    }
}

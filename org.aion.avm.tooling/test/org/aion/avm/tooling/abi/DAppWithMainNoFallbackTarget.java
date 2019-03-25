package org.aion.avm.tooling.abi;

public class DAppWithMainNoFallbackTarget {
    public static byte[] main() {
        // This contract is never called, the test only checks correct annotation extraction
        return null;
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

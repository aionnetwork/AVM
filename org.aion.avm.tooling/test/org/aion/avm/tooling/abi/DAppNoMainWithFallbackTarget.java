package org.aion.avm.tooling.abi;

public class DAppNoMainWithFallbackTarget {

    static private int a = 0;

    @Callable()
    public static boolean test1(boolean b) {
        return true;
    }

    @Callable()
    public static boolean test2(int i, String s, long[] l) {
        return true;
    }

    @Callable()
    public static int getValue() {
        return a;
    }

    @Deprecated
    public static boolean test3(int i, String s, long[] l) {
        return true;
    }

    @Fallback
    private static void fallbackIncrease10() {
        a += 10;
    }
}


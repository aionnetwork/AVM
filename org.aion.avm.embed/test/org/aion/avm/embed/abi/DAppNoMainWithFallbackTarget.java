package org.aion.avm.embed.abi;

import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.tooling.abi.Fallback;

//NOTE:  This is a copy of a test in org.aion.avm.tooling in order to support the IntegTest.
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


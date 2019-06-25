package org.aion.avm.embed.abi;

import org.aion.avm.tooling.abi.Callable;

//NOTE:  This is a copy of a test in org.aion.avm.tooling in order to support the IntegTest.
public class DAppNoMainNoFallbackTarget {

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

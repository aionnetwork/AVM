package org.aion.avm.tooling.abi;

public class OverloadedCallablesTarget {
    @Callable()
    public static boolean test1(boolean b) {
        return true;
    }

    @Callable()
    public static boolean test1(int i, String s, long[] l) {
        return true;
    }
}

package org.aion.avm.tooling.abi;

public class DAppForbiddenStringArrayTarget {

    @Callable()
    public static String[][] badStringArray() {
        return new String[1][2];
    }
}


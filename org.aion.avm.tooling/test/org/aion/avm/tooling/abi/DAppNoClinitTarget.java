package org.aion.avm.tooling.abi;

public class DAppNoClinitTarget {

    @Initializable
    static int valueInt;

    @Initializable
    static String valueString;

    @Callable
    public static int getInt() {
        return valueInt;
    }

    @Callable
    public static String getString() {
        return valueString;
    }
}

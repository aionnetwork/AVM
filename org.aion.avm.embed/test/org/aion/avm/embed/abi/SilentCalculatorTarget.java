package org.aion.avm.embed.abi;

import org.aion.avm.tooling.abi.Callable;

//NOTE:  This is a copy of a test in org.aion.avm.tooling in order to support the IntegTest.
public class SilentCalculatorTarget {
    @Callable()
    public static boolean greaterThan(int a, int b) {
        return a > b;
    }

    private static boolean greaterThanEq(int a, int b) {
        return a >= b;
    }

    @Callable()
    public static boolean lesserThan(int a, int b) {
        return !(greaterThanEq(a, b));
    }
}

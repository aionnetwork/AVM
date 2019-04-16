package org.aion.avm.tooling.abi;

public class StaticInitializersTarget {

    @Initializable
    static int valueInt;

    @Initializable
    static String valueString;

    static {
        valueInt = 10;
    }

    @Callable
    public static int getInt() {
        return valueInt;
    }

    @Callable
    public static String getString() {
        return valueString;
    }

    @Callable()
    public static String amIGreater(int a, int b) {
        if (SilentCalculatorTarget.greaterThan(a, b)) {
            return("Yes, " + a + ", you are greater than " + b);
        } else {
            return("No, " + a + ", you are NOT greater than " + b);
        }
    }
}

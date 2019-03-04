package org.aion.avm.tooling.abi;

public class ChattyCalculatorTarget {

    @Callable()
    public static String amIGreater(int a, int b) {
        if (SilentCalculatorTarget.greaterThan(a, b)) {
            return("Yes, " + a + ", you are greater than " + b);
        } else {
            return("No, " + a + ", you are NOT greater than " + b);
        }
    }
}

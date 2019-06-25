package org.aion.avm.embed.abi;

import org.aion.avm.tooling.abi.Callable;

// NOTE:  This is a copy of a test in org.aion.avm.tooling in order to support the IntegTest.
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

package org.aion.avm.embed.abi;

import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.tooling.abi.Initializable;

//NOTE:  This is a copy of a test in org.aion.avm.tooling in order to support the IntegTest.
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

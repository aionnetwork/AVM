package org.aion.avm.tooling.abi;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

public class StaticInitializersTarget {

    @Initializable
    static int valueInt;

    @Initializable
    static String valueString;

    static {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        valueInt = decoder.decodeOneInteger();
        valueString = decoder.decodeOneString();
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

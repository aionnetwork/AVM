package org.aion.avm.embed.shadowing.testInterface;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class UserDefinedInterfaceTarget {
    interface InnerInterface {
        int a = 1;
        String b = "abc";
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String method = decoder.decodeMethodName();

        if (method.equals("addInt")) {
            return ABIEncoder.encodeOneInteger(addInt());
        } else if (method.equals("concatString")) {
            return ABIEncoder.encodeOneString(concatString());
        }
        return new byte[0];
    }

    private static int addInt() {
        return InnerInterface.a + OuterInterface.a;
    }

    private static String concatString() {
        return InnerInterface.b + OuterInterface.b;
    }

}

interface OuterInterface {
    int a = 100;
    String b = "def";
    Object c = new Object();
}

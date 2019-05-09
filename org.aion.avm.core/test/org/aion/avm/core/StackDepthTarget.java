package org.aion.avm.core;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

public class StackDepthTarget {

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String method = decoder.decodeMethodName();

        if (method.equals("recurse")) {
            recurse(decoder.decodeOneInteger());
        } else if (method.equals("fibonacci")) {
            fibonacci(decoder.decodeOneInteger());
        }

        return null;
    }

    public static void recurse(int depth) {
        if (depth > 0) {
            recurse(depth - 1);
        }
    }

    public static int fibonacci(int num) {
        if (num <= 1) {
            return num;
        } else {
            return fibonacci(num - 2) + fibonacci(num - 1);
        }
    }
}

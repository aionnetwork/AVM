package org.aion.avm.core;

import avm.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class EnergyUsageDebugModeTarget {

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("tryToDivideInteger")) {
                return ABIEncoder.encodeOneInteger(tryToDivideInteger(decoder.decodeOneInteger(), decoder.decodeOneInteger()));
            } else {
                return new byte[0];
            }
        }
    }

    public static int tryToDivideInteger(int a, int b) {
        int c = 0;
        try {
            int d = a / b;
            c = a + b;
            c = c * 2;
            c++;
            c--;
            c += d;
            c = c / a;
        } catch (ArithmeticException e) {
            c = 111;
        }
        return c;
    }
}

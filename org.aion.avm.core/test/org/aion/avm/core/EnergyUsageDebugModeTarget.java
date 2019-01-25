package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class EnergyUsageDebugModeTarget {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(EnergyUsageDebugModeTarget.class, BlockchainRuntime.getData());
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

package org.aion.avm.core;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class EnergyUsageDebugModeTarget {

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            Object[] argValues = ABIDecoder.decodeArguments(inputBytes);
            if (methodName.equals("tryToDivideInteger")) {
                return ABIEncoder.encodeOneObject(tryToDivideInteger((Integer) argValues[0], (Integer) argValues[1]));
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

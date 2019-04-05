package org.aion.avm.core;

import java.math.BigInteger;
import avm.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class ReentrantObjectArrayTestResource {

    private static String[] names = {"foo, bar"};

    public static void setString() {
        names[0] = "bar";
    }

    public static void testString() {
        byte[] data = ABIEncoder.encodeOneString("setString");
        BlockchainRuntime.call(BlockchainRuntime.getAddress(), BigInteger.ZERO, data, BlockchainRuntime.getEnergyLimit());
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("testString")) {
                testString();
                return new byte[0];
            } else if (methodName.equals("setString")) {
                testString();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }
}

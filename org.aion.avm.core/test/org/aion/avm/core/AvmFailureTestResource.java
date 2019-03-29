package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class AvmFailureTestResource {


    public static void reentrantCall(int n) {
        if (n > 0) {
            byte[] methodNameBytes = ABIEncoder.encodeOneString("reentrantCall");
            byte[] argBytes = ABIEncoder.encodeOneInteger(n - 1);
            byte[] data = new byte[methodNameBytes.length + argBytes.length];
            System.arraycopy(methodNameBytes, 0, data, 0, methodNameBytes.length);
            System.arraycopy(argBytes, 0, data, methodNameBytes.length, argBytes.length);
            BlockchainRuntime.call(BlockchainRuntime.getAddress(), BigInteger.ZERO, data, BlockchainRuntime.getEnergyLimit());
            BlockchainRuntime.log(new byte[]{(byte)n});
            BlockchainRuntime.revert();
        }
    }

    public static void testOutOfEnergy() {
        while (true) {
            byte[] bytes = new byte[1024];
            bytes.clone();
        }
    }

    private static void recursive(int n) {
        if (n > 0) {
            recursive(n - 1);
        }
    }

    public static void testOutOfStack() {
        recursive(1024);
    }

    public static void testRevert() {
        BlockchainRuntime.revert();
    }

    public static void testInvalid() {
        BlockchainRuntime.invalid();
    }

    public static void testUncaughtException() {
        byte[] bytes = new byte[2];
        bytes[3] = 1;
    }
    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("reentrantCall")) {
                reentrantCall(decoder.decodeOneInteger());
                return new byte[0];
            } else if (methodName.equals("testOutOfEnergy")) {
                testOutOfEnergy();
                return new byte[0];
            } else if (methodName.equals("testOutOfStack")) {
                testOutOfStack();
                return new byte[0];
            } else if (methodName.equals("testRevert")) {
                testRevert();
                return new byte[0];
            } else if (methodName.equals("testInvalid")) {
                testInvalid();
                return new byte[0];
            } else if (methodName.equals("testUncaughtException")) {
                testUncaughtException();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }
}

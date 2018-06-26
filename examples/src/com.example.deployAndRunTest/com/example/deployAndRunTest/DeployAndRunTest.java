package com.example.deployAndRunTest;

import org.aion.avm.api.BlockchainRuntime;

public class DeployAndRunTest {

    public int foo;

    public static int bar;

    public int add(int a, int b) {
        return a + b;
    }

    public int addArray(int[] a) {
        return a[0] + a[1];
    }

    public char[] concatenate(char[][] s) {
        char[] res = new char[6];
        for (int i = 0; i < s[0].length; i++) {
            res[i] = s[0][i];
        }
        for (int i = s[0].length; i < (s[0].length + s[1].length) && i < res.length; i++) {
            res[i] = s[1][i-s[0].length];
        }
        return res;
    }

    public byte[] run() {
        return "Hello, world!".getBytes();
    }

    public Object main() throws InvalidTxDataException {
        // copy txData here for now
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x41, 0x72, 0x72, 0x61, 0x79, 0x3C, 0x5B, 0x49, 0x32, 0x5D, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "addArray<[I2]>" + raw data 123, 1

        ABIDecoder decoder = new ABIDecoder();
        //ABIDecoder.MethodCaller methodCaller = decoder.decode(BlockchainRuntime.getData());
        ABIDecoder.MethodCaller methodCaller = decoder.decode(txData);
        switch (methodCaller.methodName) {
            case "add":
                return add((int)methodCaller.arguments[0], (int)methodCaller.arguments[1]);
            case "addArray":
                return addArray((int[])methodCaller.arguments[0]);
            case "concatenate":
                return concatenate((char[][]) methodCaller.arguments[0]);
            case "run":
                return run();
            default:
                throw new InvalidTxDataException();
        }
    }
}

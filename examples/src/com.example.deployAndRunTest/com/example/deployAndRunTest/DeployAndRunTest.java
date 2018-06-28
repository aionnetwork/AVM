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

    public int addArray2(int[][] a) {
        return a[0][0] + a[1][0];
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

    public byte[] main() throws InvalidTxDataException {
        ABIDecoder.MethodCaller methodCaller = ABIDecoder.decode(BlockchainRuntime.getData());
        switch (methodCaller.methodName) {
            case "add":
                return ABIEncoder.encodeInt(add((int)methodCaller.arguments[0], (int)methodCaller.arguments[1]));
            case "addArray":
                return ABIEncoder.encodeInt(addArray((int[])methodCaller.arguments[0]));
            case "addArray2":
                return ABIEncoder.encodeInt(addArray2((int[][])methodCaller.arguments[0]));
            case "concatenate":
                //return String.valueOf(((char[][])methodCaller.arguments[0])[0]).getBytes();
                //return Character.toString(concatenate((char[][]) methodCaller.arguments[0])[0]).getBytes();
                return ABIEncoder.encode1DArray(concatenate((char[][]) methodCaller.arguments[0]), ABIEncoder.ABITypes.CHAR);
            case "run":
                return run();
            default:
                throw new InvalidTxDataException();
        }
    }
}

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

    public char[][] swap(char[][] s) {
        char[][] res = new char[2][];
        res[0] = s[1];
        res[1] = s[0];
        return res;
    }

    public byte[] run() {
        return "Hello, world!".getBytes();
    }

    public byte[] encodeArgs() throws InvalidTxDataException {
        String methodAPI = "int addArray(int[] a)";
        int[] a = new int[]{123, 1};
        return ABIEncoder.encodeMethodArguments(methodAPI, a);
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
                return ABIEncoder.encode1DArray(concatenate((char[][]) methodCaller.arguments[0]), ABIEncoder.ABITypes.CHAR);
            case "swap":
                return ABIEncoder.encode2DArray(swap((char[][]) methodCaller.arguments[0]), ABIEncoder.ABITypes.CHAR);
            case "run":
                return run();
            case "encodeArgs":
                return encodeArgs();
            default:
                throw new InvalidTxDataException();
        }
    }
}

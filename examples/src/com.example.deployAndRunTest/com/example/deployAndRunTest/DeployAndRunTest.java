package com.example.deployAndRunTest;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.InvalidTxDataException;

import java.io.UnsupportedEncodingException;

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

    public Object main() throws InvalidTxDataException, UnsupportedEncodingException {
        ABIDecoder decoder = new ABIDecoder();
        ABIDecoder.MethodCaller methodCaller = decoder.decode(BlockchainRuntime.getData());
        switch (methodCaller.methodName) {
            case "add":
                return add((int)methodCaller.arguments.get(0), (int)methodCaller.arguments.get(1));
            case "addArray":
                return addArray((int[])methodCaller.arguments.get(0));
            case "concatenate":
                return concatenate((char[][]) methodCaller.arguments.get(0));
            case "run":
                return run();
            default:
                throw new InvalidTxDataException();
        }
    }
}

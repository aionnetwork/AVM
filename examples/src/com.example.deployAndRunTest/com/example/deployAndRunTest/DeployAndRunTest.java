package com.example.deployAndRunTest;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class DeployAndRunTest {

    public int foo;

    public static int bar;

    public int add(int a, int b) {
        return a + b;
    }

    public static int addArray(int[] a, int b) {
        return a[0] + a[1] + b;
    }

    public int addArray2(int[][] a) {
        return a[0][0] + a[1][0];
    }

    public char[] concatenate(char[][] s) {
        char[] res = new char[6];
        System.arraycopy(s[0], 0, res, 0, s[0].length);
        System.arraycopy(s[1], 0, res, s[0].length, s[1].length);
        return res;
    }

    public char[][] swap(char[][] s) {
        char[][] res = new char[2][2];
        res[0] = s[1];
        res[1] = s[0];
        return res;
    }

    public void setBar(int bar) {
        this.bar = bar;
    }

    public static byte[] run() {
        return "Hello, world!".getBytes();
    }

    public static byte[] encodeArgs(){
        String methodName = "addArray";
        int[] a = new int[]{123, 1};
        int b = 5;
        return ABIEncoder.encodeMethodArguments(methodName, a, b);
    }

    public static byte[] main() {
        String methodName = ABIDecoder.decodeMethodName(BlockchainRuntime.getData());
        if (methodName.equals("addArray")) {
            //return ABIDecoder.decodeAndRun(DeployAndRunTest.class, BlockchainRuntime.getData());
        }
        return ABIDecoder.decodeAndRun(new DeployAndRunTest(), BlockchainRuntime.getData());
    }
}

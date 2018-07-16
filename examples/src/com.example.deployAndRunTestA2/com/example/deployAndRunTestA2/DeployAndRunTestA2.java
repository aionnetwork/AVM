package com.example.deployAndRunTestA2;

import org.aion.avm.api.ABIA2Encoder;
import org.aion.avm.api.ABIA2Decoder;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.api.BlockchainRuntime;

public class DeployAndRunTestA2 {

    public int foo;

    public static int bar;

    public static byte[] add(int a, int b) {
        return ABIA2Encoder.encodeInt(a + b);
    }

    public static byte[] addArray(int[] a) {
        return ABIA2Encoder.encodeInt(a[0] + a[1]);
    }

    public static byte[] addArray2(int[][] a) {
        return ABIA2Encoder.encodeInt(a[0][0] + a[1][0]);
    }

    public static byte[] concatenate(char[][] s) {
        char[] res = new char[6];
        System.arraycopy(s[0], 0, res, 0, s[0].length);
        System.arraycopy(s[1], 0, res, s[0].length, s[1].length);
        return ABIA2Encoder.encode1DArray(res, ABIA2Encoder.ABITypes.CHAR);
    }

    public static byte[] swap(char[][] s) {
        char[][] res = new char[2][];
        res[0] = s[1];
        res[1] = s[0];
        return ABIA2Encoder.encode2DArray(res, ABIA2Encoder.ABITypes.CHAR);
    }

    public static byte[] run() {
        return "Hello, world!".getBytes();
    }

    public static byte[] encodeArgs(){
        String methodAPI = "int addArray(int[] a)";
        int[] a = new int[]{123, 1};
        return ABIA2Encoder.encodeMethodArguments(methodAPI, a);
    }

    public byte[] main() throws InvalidTxDataException {
        return ABIA2Decoder.decodeAndRun(this.getClass(), BlockchainRuntime.getData());
    }
}

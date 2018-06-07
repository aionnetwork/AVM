package org.aion.avm.rt;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TxDataDecoder {
    /*
     * ABI arguments descriptor symbols and the elementary type sizes.
     */
    public static final char BYTE = 'B';
    public static final int  BYTE_SIZE = 1;

    public static final char BOOLEAN = 'Z';
    public static final int  BOOLEAN_SIZE = 1;

    public static final char CHAR = 'C';
    // UTF-8 is variable-length encoding; minimum 1 byte, maximum 6 bytes
    public static final int  CHAR_SIZE_MIN = 1;
    public static final int  CHAR_SIZE_MAX = 6;

    public static final char SHORT = 'S';
    public static final int  SHORT_SIZE = 2;

    public static final char INT = 'I';
    public static final int  INT_SIZE = 4;

    public static final char FLOAT = 'F';
    public static final int  FLOAT_SIZE = 4;

    public static final char LONG = 'L';
    public static final int  LONG_SIZE = 8;

    public static final char DOUBLE = 'D';
    public static final int  DOUBLE_SIZE = 8;

    public static final char ARRAY_S = '[';
    public static final char ARRAY_E = ']';

    public static final char DESCRIPTOR_S = '<';
    public static final char DESCRIPTOR_E = '>';

    public static class MethodCaller {
        String methodName;
        List<Object> arguments;

        MethodCaller(String methodName, List<Object> arguments) {
            this.methodName = methodName;
            this.arguments  = arguments;
        }
    }

    public MethodCaller decode(byte[] txData) throws InvalidTxDataException, UnsupportedEncodingException {
        String decoded = new String(txData, "UTF-8");

        int m1 = decoded.indexOf(DESCRIPTOR_S);
        int m2 = decoded.indexOf(DESCRIPTOR_E);
        if (m1 == -1 || m2 == -1 || txData[m1] != 0x3C || txData[m2] != 0x3E) {
            throw new InvalidTxDataException();
        }

        String methodName = decoded.substring(0, m1);
        String argsDescriptor = decoded.substring(m1+1, m2);

        List<Object> arguments = getArguments(txData, m2+1, argsDescriptor);

        MethodCaller mc = new MethodCaller(methodName, arguments);
        return mc;
    }

    private List<Object> getArguments(byte[] txData, int start, String argsDescriptor) throws InvalidTxDataException, UnsupportedEncodingException {
        List<Object> args = new ArrayList<>();

        for (int idx = 0; idx < argsDescriptor.length(); idx++) {
            char c = argsDescriptor.charAt(idx);

            switch (c) {
                case BYTE:
                    if ((txData.length - start) < BYTE_SIZE) {
                        throw new InvalidTxDataException();
                    }

                    args.add(txData[start]);
                    start += BYTE_SIZE;
                    break;
                case BOOLEAN:
                    if ((txData.length - start) < BOOLEAN_SIZE) {
                        throw new InvalidTxDataException();
                    }

                    boolean b = (txData[start] != 0);
                    args.add(b);
                    start += BOOLEAN_SIZE;
                    break;
                case CHAR:
                    if ((txData.length - start) < CHAR_SIZE_MIN) {
                        throw new InvalidTxDataException();
                    }

                    char c1;
                    if ((txData.length - start) > CHAR_SIZE_MAX) {
                        c1 = (new String(Arrays.copyOfRange(txData, start, start + 6), "UTF-8")).charAt(0);
                    }
                    else {
                        c1 = (new String(Arrays.copyOfRange(txData, start, txData.length), "UTF-8")).charAt(0);
                    }
                    args.add(c1);
                    start += "c1".getBytes("UTF-8").length;
                    break;
                case SHORT:
                    if ((txData.length - start) < SHORT_SIZE) {
                        throw new InvalidTxDataException();
                    }

                    short s = (short)((txData[start]<<8)&0xFF00 | txData[start+1]&0xFF);
                    args.add(s);
                    start += SHORT_SIZE;
                    break;
                case INT:
                    if ((txData.length - start) < INT_SIZE) {
                        throw new InvalidTxDataException();
                    }

                    int i = (txData[start]<<24)&0xFF000000 | (txData[start+1]<<16)&0xFF0000 |
                            (txData[start+2]<<8)&0xFF00 | txData[start+3]&0xFF;
                    args.add(i);
                    start += INT_SIZE;
                    break;
                case FLOAT:
                    if ((txData.length - start) < FLOAT_SIZE) {
                        throw new InvalidTxDataException();
                    }

                    float f = (float) ((txData[start]<<24)&0xFF000000 | (txData[start+1]<<16)&0xFF0000 |
                                       (txData[start+2]<<8)&0xFF00 | txData[start+3]&0xFF);
                    args.add(f);
                    start += FLOAT_SIZE;
                    break;
                case LONG:
                    if ((txData.length - start) < LONG_SIZE) {
                        throw new InvalidTxDataException();
                    }

                    long l = (long) ((txData[start]<<24)&0xFF000000 | (txData[start+1]<<16)&0xFF0000 |
                                     (txData[start+2]<<8)&0xFF00 | txData[start+3]&0xFF);
                    args.add(l);
                    start += LONG_SIZE;
                    break;
                case DOUBLE:
                    if ((txData.length - start) < DOUBLE_SIZE) {
                        throw new InvalidTxDataException();
                    }

                    double d = (double) ((txData[start]<<24)&0xFF000000 | (txData[start+1]<<16)&0xFF0000 |
                                         (txData[start+2]<<8)&0xFF00 | txData[start+3]&0xFF);
                    args.add(d);
                    start += DOUBLE_SIZE;
                    break;
                case ARRAY_S:
                    int arrayDimension = 1;
                    char type;

                    // TODO add regex check

                    if (argsDescriptor.charAt(++ idx) == ARRAY_S) {
                        arrayDimension ++;
                        idx ++;
                    }

                    type = argsDescriptor.charAt(idx ++);

                    int m = Integer.parseInt(argsDescriptor.substring(idx, argsDescriptor.indexOf(']', idx)));
                    idx = argsDescriptor.indexOf(ARRAY_E, idx);

                    if (arrayDimension == 1) {
                        get1DArrayData(txData, start, args, type, m);
                    }
                    else {
                        int n = Integer.parseInt(argsDescriptor.substring(idx, argsDescriptor.indexOf(']', idx)));
                        idx = argsDescriptor.indexOf(ARRAY_E, idx);
                        get2DArrayData(txData, start, args, type, m, n);
                    }
                    break;
                default:
                    throw new InvalidTxDataException();
            }
        }

        return args;
    }

    private void get1DArrayData(byte[] txData, int start, List<Object> args, char type, int m) throws InvalidTxDataException, UnsupportedEncodingException{
        switch (type) {
            case BYTE:
                if ((txData.length - start) < BYTE_SIZE * m) {
                    throw new InvalidTxDataException();
                }

                byte[] argB = new byte[m];
                for (int idx = 0; idx < m; idx ++) {
                    argB[idx] = txData[start];
                    start += BYTE_SIZE;
                }
                args.add(argB);
                break;
            case BOOLEAN:
                if ((txData.length - start) < BOOLEAN_SIZE * m) {
                    throw new InvalidTxDataException();
                }

                boolean[] argZ = new boolean[m];
                for (int idx = 0; idx < m; idx ++) {
                    argZ[idx] = (txData[start] != 0);
                    start += BOOLEAN_SIZE;
                }
                args.add(argZ);
                break;
            case CHAR:
                if ((txData.length - start) < CHAR_SIZE_MIN * m) {
                    throw new InvalidTxDataException();
                }

                String argC;
                if ((txData.length - start) > CHAR_SIZE_MAX * m) {
                    argC = (new String(Arrays.copyOfRange(txData, start, start + CHAR_SIZE_MAX * m), "UTF-8"))
                            .substring(0, m);
                }
                else {
                    argC = (new String(Arrays.copyOfRange(txData, start, txData.length), "UTF-8"))
                            .substring(0, m);
                }
                start += argC.getBytes("UTF-8").length;
                args.add(argC);
                break;
            case SHORT:
                if ((txData.length - start) < SHORT_SIZE * m) {
                    throw new InvalidTxDataException();
                }
                short[] argS = new short[m];
                for (int idx = 0; idx < m; idx ++) {
                    argS[idx] = (short) ((txData[start] << 8) & 0xFF00 | txData[start + 1] & 0xFF);
                    start += SHORT_SIZE;
                }
                args.add(argS);
                break;
            case INT:
                if ((txData.length - start) < INT_SIZE * m) {
                    throw new InvalidTxDataException();
                }
                int[] argI = new int[m];
                for (int idx = 0; idx < m; idx ++) {
                    argI[idx] = (txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                                (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF;
                    start += INT_SIZE;
                }
                args.add(argI);
                break;
            case FLOAT:
                if ((txData.length - start) < FLOAT_SIZE * m) {
                    throw new InvalidTxDataException();
                }
                float[] argF = new float[m];
                for (int idx = 0; idx < m; idx ++) {
                    argF[idx] = (float) ((txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                                         (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF);
                    start += FLOAT_SIZE;
                }
                args.add(argF);
                break;
            case LONG:
                if ((txData.length - start) < LONG_SIZE * m) {
                    throw new InvalidTxDataException();
                }
                long[] argL = new long[m];
                for (int idx = 0; idx < m; idx ++) {
                    argL[idx] = (long) ((txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                                        (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF);
                    start += LONG_SIZE;
                }
                args.add(argL);
                break;
            case DOUBLE:
                if ((txData.length - start) < DOUBLE_SIZE * m) {
                    throw new InvalidTxDataException();
                }
                double[] argD = new double[m];
                for (int idx = 0; idx < m; idx ++) {
                    argD[idx] = (double) ((txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                                          (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF);
                    start += DOUBLE_SIZE;
                }
                args.add(argD);
                break;
        }
    }

    private void get2DArrayData(byte[] txData, int start, List<Object> args, char type, int m, int n) throws InvalidTxDataException, UnsupportedEncodingException{
        switch (type) {
            case BYTE:
                if ((txData.length - start) < BYTE_SIZE * m * n) {
                    throw new InvalidTxDataException();
                }
                byte[][] argB = new byte[m][n];
                for (int indexN = 0; indexN < n; indexN ++) {
                    for (int indexM = 0; indexM < m; indexM++) {
                        argB[indexM][indexN] = txData[start];
                        start += BYTE_SIZE;
                    }
                }
                args.add(argB);
                break;
            case BOOLEAN:
                if ((txData.length - start) < BOOLEAN_SIZE * m * n) {
                    throw new InvalidTxDataException();
                }
                boolean[][] argZ = new boolean[m][n];
                for (int indexN = 0; indexN < n; indexN ++) {
                    for (int indexM = 0; indexM < m; indexM++) {
                        argZ[indexM][indexN] = (txData[start] != 0);
                        start += BOOLEAN_SIZE;
                    }
                }
                args.add(argZ);
                break;
            case CHAR:
                if ((txData.length - start) < CHAR_SIZE_MIN * m * n) {
                    throw new InvalidTxDataException();
                }

                String[] argC = new String[n];
                for (int indexN = 0; indexN < n; indexN ++) {
                    String s;
                    if ((txData.length - start) > CHAR_SIZE_MAX * m) {
                        s = new String(Arrays.copyOfRange(txData, start, start + CHAR_SIZE_MAX * m), "UTF-8");
                    }
                    else {
                        s = new String(Arrays.copyOfRange(txData, start, txData.length), "UTF-8");
                    }
                    start += s.getBytes("UTF-8").length;
                    argC[indexN] = s;
                }
                args.add(argC);
                break;
            case SHORT:
                if ((txData.length - start) < SHORT_SIZE * m * n) {
                    throw new InvalidTxDataException();
                }
                short[][] argS = new short[m][n];
                for (int indexN = 0; indexN < n; indexN ++) {
                    for (int indexM = 0; indexM < m; indexM++) {
                        argS[indexM][indexN] = (short) ((txData[start] << 8) & 0xFF00 | txData[start + 1] & 0xFF);
                        start += SHORT_SIZE;
                    }
                }
                args.add(argS);
                break;
            case INT:
                if ((txData.length - start) < INT_SIZE * m * n) {
                    throw new InvalidTxDataException();
                }
                int[][] argI = new int[m][n];
                for (int indexN = 0; indexN < n; indexN ++) {
                    for (int indexM = 0; indexM < m; indexM++) {
                        argI[indexM][indexN] = (txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                                               (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF;
                        start += INT_SIZE;
                    }
                }
                args.add(argI);
                break;
            case FLOAT:
                if ((txData.length - start) < FLOAT_SIZE * m * n) {
                    throw new InvalidTxDataException();
                }
                float[][] argF = new float[m][n];
                for (int indexN = 0; indexN < n; indexN ++) {
                    for (int indexM = 0; indexM < m; indexM++) {
                        argF[indexM][indexN] = (float) ((txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                                                        (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF);
                        start += FLOAT_SIZE;
                    }
                }
                args.add(argF);
                break;
            case LONG:
                if ((txData.length - start) < LONG_SIZE * m * n) {
                    throw new InvalidTxDataException();
                }
                long[][] argL = new long[m][n];
                for (int indexN = 0; indexN < n; indexN ++) {
                    for (int indexM = 0; indexM < m; indexM++) {
                        argL[indexM][indexN] = (long) ((txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                                                       (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF);
                        start += LONG_SIZE;
                    }
                }
                args.add(argL);
                break;
            case DOUBLE:
                if ((txData.length - start) < DOUBLE_SIZE * m * n) {
                    throw new InvalidTxDataException();
                }
                double[][] argD = new double[m][n];
                for (int indexN = 0; indexN < n; indexN ++) {
                    for (int indexM = 0; indexM < m; indexM++) {
                        argD[indexM][indexN] = (double) ((txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                                                         (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF);
                        start += DOUBLE_SIZE;
                    }
                }
                args.add(argD);
                break;
        }
    }
}

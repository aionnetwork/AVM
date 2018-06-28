package com.example.helloworld;

public class ABIDecoder {
    /*
     * ABI arguments descriptor symbols and the elementary type sizes.
     */
    public static final char BYTE = 'B';
    public static final int  BYTE_SIZE = 1;

    public static final char BOOLEAN = 'Z';
    public static final int  BOOLEAN_SIZE = 1;

    public static final char CHAR = 'C';
    // UTF-8 is variable-length encoding; minimum 1 byte, maximum 6 bytes for 1 character
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

    public static final char JAGGED_D_S = '(';
    public static final char JAGGED_D_E = ')';

    public static final char DESCRIPTOR_S = '<';
    public static final char DESCRIPTOR_E = '>';

    public static class MethodCaller {
        public String methodName;
        public String argsDescriptor;
        public Object[] arguments;

        MethodCaller(String methodName, String argsDescriptor, Object[] arguments) {
            this.methodName = methodName;
            this.argsDescriptor = argsDescriptor;
            this.arguments  = arguments;
        }
    }

    public static MethodCaller decode(byte[] txData) throws InvalidTxDataException{
        String decoded = new String(txData);

        int m1 = decoded.indexOf(DESCRIPTOR_S);
        int m2 = decoded.indexOf(DESCRIPTOR_E);
        if (m1 == -1 && m2 == -1) {
            // no arguments
            return new MethodCaller(decoded, null, null);
        }
        if (m1 == -1 || m2 == -1) {
            throw new InvalidTxDataException();
        }

        String methodName = decoded.substring(0, m1);
        String argsDescriptor = decoded.substring(m1+1, m2);

        Object[] arguments = getArguments(txData, m2+1, argsDescriptor);

        return new MethodCaller(methodName, argsDescriptor, arguments);
    }

    private static Object[] getArguments(byte[] txData, int start, String argsDescriptor) throws InvalidTxDataException{
        Object[] args = new Object[10];
        int argsCount = 0;

        for (int idx = 0; idx < argsDescriptor.length(); idx++, argsCount++) {
            char c = argsDescriptor.charAt(idx);

            switch (c) {
                case BYTE:
                    checkRemainingDataSize(txData.length - start, BYTE_SIZE);

                    args[argsCount] = txData[start];
                    start += BYTE_SIZE;
                    break;
                case BOOLEAN:
                    checkRemainingDataSize(txData.length - start, BOOLEAN_SIZE);

                    boolean b = (txData[start] != 0);
                    args[argsCount] = b;
                    start += BOOLEAN_SIZE;
                    break;
                case CHAR:
                    checkRemainingDataSize(txData.length - start, CHAR_SIZE_MIN);

                    char c1 = getNextString(txData, start, 1).charAt(0);;
                    args[argsCount] = c1;
                    start += Character.toString(c1).getBytes().length;
                    break;
                case SHORT:
                    checkRemainingDataSize(txData.length - start, SHORT_SIZE);

                    args[argsCount] = getNextShort(txData, start);
                    start += SHORT_SIZE;
                    break;
                case INT:
                    checkRemainingDataSize(txData.length - start, INT_SIZE);

                    args[argsCount] = getNextInt(txData, start);
                    start += INT_SIZE;
                    break;
                case FLOAT:
                    checkRemainingDataSize(txData.length - start, FLOAT_SIZE);

                    args[argsCount] = getNextFloat(txData, start);
                    start += FLOAT_SIZE;
                    break;
                case LONG:
                    checkRemainingDataSize(txData.length - start, LONG_SIZE);

                    args[argsCount] = getNextLong(txData, start);
                    start += LONG_SIZE;
                    break;
                case DOUBLE:
                    checkRemainingDataSize(txData.length - start, DOUBLE_SIZE);

                    args[argsCount] = getNextDouble(txData, start);
                    start += DOUBLE_SIZE;
                    break;
                case ARRAY_S:
                    int arrayDimension = 1;
                    char type;

                    // shortest [XM]
                    if (argsDescriptor.length() - idx < 4) {
                        throw new InvalidTxDataException();
                    }

                    if (argsDescriptor.charAt(++ idx) == ARRAY_S) {
                        arrayDimension ++;
                        idx ++;
                    }

                    type = argsDescriptor.charAt(idx ++);

                    int[] res = readNumFromDescriptor(argsDescriptor, ARRAY_E, idx);
                    int m = res[0];
                    idx = res[1];

                    if (arrayDimension == 1) {
                        start = get1DArrayData(txData, start, args, argsCount, type, m);
                    }
                    else {
                        res = readNumFromDescriptor(argsDescriptor, ARRAY_E, idx + 1);
                        int n = res[0];
                        idx = res[1];

                        int[] dimensions = new int[n];
                        if (m == 0 && n > 0) {
                            // this is a jagged array
                            for (int i = 0; i < n; i ++) {
                                if (argsDescriptor.charAt(++ idx) == JAGGED_D_S) {
                                    res = readNumFromDescriptor(argsDescriptor, JAGGED_D_E, idx + 1);
                                    dimensions[i] = res[0];
                                    idx = res[1];
                                }
                                else {
                                    throw new InvalidTxDataException();
                                }
                            }
                        }
                        else if (m > 0 && n > 0) {
                            // this is a rectangular shape 2D array
                            for (int i = 0; i < n; i ++) {
                                dimensions[i] = m;
                            }
                        }
                        else {
                            throw new InvalidTxDataException();
                        }

                        start = get2DArrayData(txData, start, args, argsCount, type, dimensions, n);
                    }
                    break;
                default:
                    throw new InvalidTxDataException();
            }

            if (argsCount == args.length - 1) {
                Object[] argsNew = new Object[args.length + 10];
                for (int i = 0; i < args.length; i ++) {
                    argsNew[i] = args[i];
                }
                args = argsNew;
            }
        }

        return args;
    }

    /*
     * A helper method to read a number from the arguments descriptor.
     */
    private static int[] readNumFromDescriptor(String argsDescriptor, char stopChar, int startIdx) throws InvalidTxDataException {
        int[] res = new int[2];
        int idxE = argsDescriptor.indexOf(stopChar, startIdx);
        if ( idxE == -1) {
            throw new InvalidTxDataException();
        }

        res[1] = idxE;
        if (argsDescriptor.substring(startIdx, idxE).isEmpty()) {
            res[0] = 0; // may be a jagged array
        }
        else {
            try {
                res[0] = Integer.parseInt(argsDescriptor.substring(startIdx, idxE));
            }
            catch (NumberFormatException e) {
                throw new InvalidTxDataException();
            }
        }
        return res;

    }

    private static int get1DArrayData(byte[] txData, int start, Object[] args, int argsCount, char type, int m) throws InvalidTxDataException{
        if (m <= 0) {
            throw new InvalidTxDataException();
        }

        switch (type) {
            case BYTE:
                checkRemainingDataSize(txData.length - start, BYTE_SIZE * m);
                byte[] argB = new byte[m];
                for (int idx = 0; idx < m; idx ++) {
                    argB[idx] = txData[start];
                    start += BYTE_SIZE;
                }
                args[argsCount] = argB;
                break;
            case BOOLEAN:
                checkRemainingDataSize(txData.length - start, BOOLEAN_SIZE * m);
                boolean[] argZ = new boolean[m];
                for (int idx = 0; idx < m; idx ++) {
                    argZ[idx] = (txData[start] != 0);
                    start += BOOLEAN_SIZE;
                }
                args[argsCount] = argZ;
                break;
            case CHAR:
                checkRemainingDataSize(txData.length - start, CHAR_SIZE_MIN * m);
                String argC = getNextString(txData, start, m);
                start += argC.getBytes().length;
                args[argsCount] = argC.toCharArray();
                break;
            case SHORT:
                checkRemainingDataSize(txData.length - start, SHORT_SIZE * m);
                short[] argS = new short[m];
                for (int idx = 0; idx < m; idx ++) {
                    argS[idx] = getNextShort(txData, start);
                    start += SHORT_SIZE;
                }
                args[argsCount] = argS;
                break;
            case INT:
                checkRemainingDataSize(txData.length - start, INT_SIZE * m);
                int[] argI = new int[m];
                for (int idx = 0; idx < m; idx ++) {
                    argI[idx] = getNextInt(txData, start);
                    start += INT_SIZE;
                }
                args[argsCount] = argI;
                break;
            case FLOAT:
                checkRemainingDataSize(txData.length - start, FLOAT_SIZE * m);
                float[] argF = new float[m];
                for (int idx = 0; idx < m; idx ++) {
                    argF[idx] = getNextFloat(txData, start);
                    start += FLOAT_SIZE;
                }
                args[argsCount] = argF;
                break;
            case LONG:
                checkRemainingDataSize(txData.length - start, LONG_SIZE * m);
                long[] argL = new long[m];
                for (int idx = 0; idx < m; idx ++) {
                    argL[idx] = getNextLong(txData, start);
                    start += LONG_SIZE;
                }
                args[argsCount] = argL;
                break;
            case DOUBLE:
                checkRemainingDataSize(txData.length - start, DOUBLE_SIZE * m);
                double[] argD = new double[m];
                for (int idx = 0; idx < m; idx ++) {
                    argD[idx] = getNextDouble(txData, start);
                    start += DOUBLE_SIZE;
                }
                args[argsCount] = argD;
                break;
            default:
                throw new InvalidTxDataException();
        }
        return start;
    }

    private static int get2DArrayData(byte[] txData, int start, Object[] args, int argsCount, char type, int[] m, int n) throws InvalidTxDataException{
        if (n <= 0) {
            throw new InvalidTxDataException();
        }

        switch (type) {
            case BYTE:
                byte[][] argB = new byte[n][];
                for (int indexN = 0; indexN < n; indexN ++) {
                    int curM = m[indexN];
                    if (curM <= 0) {
                        throw new InvalidTxDataException();
                    }
                    checkRemainingDataSize(txData.length - start, BYTE_SIZE * curM);

                    byte[] row = new byte[curM];
                    for (int indexM = 0; indexM < curM; indexM ++) {
                        row[indexM] = txData[start];
                        start += BYTE_SIZE;
                    }
                    argB[indexN] = row;
                }
                args[argsCount] = argB;
                break;
            case BOOLEAN:
                boolean[][] argZ = new boolean[n][];
                for (int indexN = 0; indexN < n; indexN ++) {
                    int curM = m[indexN];
                    if (curM <= 0) {
                        throw new InvalidTxDataException();
                    }
                    checkRemainingDataSize(txData.length - start, BOOLEAN_SIZE * curM);

                    boolean[] row = new boolean[curM];
                    for (int indexM = 0; indexM < curM; indexM ++) {
                        row[indexM] = (txData[start] != 0);
                        start += BOOLEAN_SIZE;
                    }
                    argZ[indexN] = row;
                }
                args[argsCount] = argZ;
                break;
            case CHAR:
                char[][] argC = new char[n][];
                for (int indexN = 0; indexN < n; indexN ++) {
                    int curM = m[indexN];
                    if (curM <= 0) {
                        throw new InvalidTxDataException();
                    }
                    checkRemainingDataSize(txData.length - start, CHAR_SIZE_MIN * curM);

                    String s = getNextString(txData, start, curM);
                    argC[indexN] = s.toCharArray();
                    start += s.getBytes().length;
                }
                args[argsCount] = argC;
                break;
            case SHORT:
                short[][] argS = new short[n][];
                for (int indexN = 0; indexN < n; indexN ++) {
                    int curM = m[indexN];
                    if (curM <= 0) {
                        throw new InvalidTxDataException();
                    }
                    checkRemainingDataSize(txData.length - start, SHORT_SIZE * curM);

                    short[] row = new short[curM];
                    for (int indexM = 0; indexM < curM; indexM ++) {
                        row[indexM] = getNextShort(txData, start);
                        start += SHORT_SIZE;
                    }
                    argS[indexN] = row;
                }
                args[argsCount] = argS;
                break;
            case INT:
                int[][] argI = new int[n][];
                for (int indexN = 0; indexN < n; indexN ++) {
                    int curM = m[indexN];
                    if (curM <= 0) {
                        throw new InvalidTxDataException();
                    }
                    checkRemainingDataSize(txData.length - start, INT_SIZE * curM);

                    int[] row = new int[curM];
                    for (int indexM = 0; indexM < curM; indexM ++) {
                        row[indexM] = getNextInt(txData, start);
                        start += INT_SIZE;
                    }
                    argI[indexN] = row;
                }
                args[argsCount] = argI;
                break;
            case FLOAT:
                float[][] argF = new float[n][];
                for (int indexN = 0; indexN < n; indexN ++) {
                    int curM = m[indexN];
                    if (curM <= 0) {
                        throw new InvalidTxDataException();
                    }
                    checkRemainingDataSize(txData.length - start, FLOAT_SIZE * curM);

                    float[] row = new float[curM];
                    for (int indexM = 0; indexM < curM; indexM++) {
                        row[indexM] = getNextFloat(txData, start);
                        start += FLOAT_SIZE;
                    }
                    argF[indexN] = row;
                }
                args[argsCount] = argF;
                break;
            case LONG:
                long[][] argL = new long[n][];
                for (int indexN = 0; indexN < n; indexN ++) {
                    int curM = m[indexN];
                    if (curM <= 0) {
                        throw new InvalidTxDataException();
                    }
                    checkRemainingDataSize(txData.length - start, LONG_SIZE * curM);

                    long[] row = new long[curM];
                    for (int indexM = 0; indexM < curM; indexM++) {
                        row[indexM] = getNextLong(txData, start);
                        start += LONG_SIZE;
                    }
                    argL[indexN] = row;
                }
                args[argsCount] = argL;
                break;
            case DOUBLE:
                double[][] argD = new double[n][];
                for (int indexN = 0; indexN < n; indexN ++) {
                    int curM = m[indexN];
                    if (curM <= 0) {
                        throw new InvalidTxDataException();
                    }
                    checkRemainingDataSize(txData.length - start, DOUBLE_SIZE * curM);

                    double[] row = new double[curM];
                    for (int indexM = 0; indexM < curM; indexM++) {
                        row[indexM] = getNextDouble(txData, start);
                        start += DOUBLE_SIZE;
                    }
                    argD[indexN] = row;
                }
                args[argsCount] = argD;
                break;
            default:
                throw new InvalidTxDataException();
        }
        return start;
    }

    private static short getNextShort(byte[] txData, int start) {
        return (short)((txData[start] << 8) & 0xFF00 | txData[start + 1] & 0xFF);
    }

    private static int getNextInt(byte[] txData, int start) {
        return ((txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF);
    }

    private static long getNextLong(byte[] txData, int start) {
        return ((long)((txData[start] << 24) & 0xFF000000 | (txData[start + 1] << 16) & 0xFF0000 |
                (txData[start + 2] << 8) & 0xFF00 | txData[start + 3] & 0xFF) << 32)
                + (long)((txData[start + 4] << 24) & 0xFF000000 | (txData[start + 5] << 16) & 0xFF0000 |
                (txData[start + 6] << 8) & 0xFF00 | txData[start + 7] & 0xFF);
    }

    private static float getNextFloat(byte[] txData, int start) {
        return Float.intBitsToFloat(getNextInt(txData, start));
    }

    private static double getNextDouble(byte[] txData, int start) {
        return Double.longBitsToDouble(getNextLong(txData, start));
    }

    private static String getNextString(byte[] txData, int start, int m) {
        int newLength = Math.min(CHAR_SIZE_MAX * m, txData.length - start);
        byte[] bytes = new byte[newLength];

        for (int i = 0; i < newLength; i++) {
            bytes[i] = txData[start + i];
        }

        return new String(bytes).substring(0, m);
    }

    private static void checkRemainingDataSize(int remainingDataSize, int minRequiredDataSize) throws InvalidTxDataException {
        if(remainingDataSize < minRequiredDataSize) {
            throw new InvalidTxDataException();
        }
    }
}


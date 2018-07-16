package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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

    /*
     * Runtime-facing implementation.
     */
    public static ByteArray avm_decodeAndRun(org.aion.avm.shadow.java.lang.Class clazz, ByteArray txData) throws InvalidTxDataException{
        return decodeAndRun(clazz, txData.getUnderlying());
    }

    public static MethodCaller avm_decode(ByteArray txData) throws InvalidTxDataException{
        return decode(txData.getUnderlying());
    }


    /*
     * Underlying implementation.
     */
    public static ByteArray decodeAndRun(org.aion.avm.shadow.java.lang.Class clazz, byte[] txData) throws InvalidTxDataException{
        MethodCaller methodCaller = decode(txData);

        String newMethodName = "avm_" + methodCaller.methodName;
        String newArgDescriptor = methodCaller.argsDescriptor;

        // generate the method descriptor of each main class method, compare to the method selector to select or invalidate the txData
        Method method = matchMethodSelector(clazz, newMethodName, newArgDescriptor);

        ByteArray ret = null;
        try {
            if (methodCaller.arguments == null) {
                ret = (ByteArray) method.invoke(null);
            }
            else {
                ret = (ByteArray) method.invoke(null, convertArguments(methodCaller.arguments));
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new InvalidTxDataException();
        }

        return ret;
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
                System.arraycopy(args, 0, argsNew, 0, args.length);
                args = argsNew;
            }
        }

        Object[] argsNew = new Object[argsCount];
        System.arraycopy(args, 0, argsNew, 0, argsCount);

        return argsNew;
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
                System.arraycopy(txData, start, argB, 0, m);
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
                    System.arraycopy(txData, start, row, 0, curM);
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


    /**
     * A helper method to match the method selector with the main-class methods.
     */
    public static Method matchMethodSelector(org.aion.avm.shadow.java.lang.Class clazz, String methodName, String argsDescriptor) {
        Method[] methods = clazz.getMethods();

        // We only allow Java primitive types or 1D/2D array of the primitive types in the parameter list.
        Map<Character, String[]> elementaryTypesMap = new HashMap<>();
        elementaryTypesMap.put(ABIDecoder.BYTE,      new String[]{"B", "byte", "ByteArray"});
        elementaryTypesMap.put(ABIDecoder.BOOLEAN,   new String[]{"Z", "boolean", "ByteArray"});
        elementaryTypesMap.put(ABIDecoder.CHAR,      new String[]{"C", "char", "CharArray"});
        elementaryTypesMap.put(ABIDecoder.SHORT,     new String[]{"S", "short", "ShortArray"});
        elementaryTypesMap.put(ABIDecoder.INT,       new String[]{"I", "int", "IntArray"});
        elementaryTypesMap.put(ABIDecoder.FLOAT,     new String[]{"F", "float", "FloatArray"});
        elementaryTypesMap.put(ABIDecoder.LONG,      new String[]{"J", "long", "LongArray"});
        elementaryTypesMap.put(ABIDecoder.DOUBLE,    new String[]{"D", "double", "DoubleArray"});

        String ARRAY_WRAPPER_PREFIX = "org.aion.avm.arraywrapper.";

        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] parameterTypes = method.getParameterTypes();

                if ((parameterTypes == null || parameterTypes.length == 0) && (argsDescriptor==null || argsDescriptor.isEmpty())) {
                    return method;
                }

                int parIdx = 0;
                boolean matched = true;
                for (int idx = 0; idx < argsDescriptor.length(); idx++) {
                    char c = argsDescriptor.charAt(idx);
                    switch (c) {
                        case ABIDecoder.ARRAY_S:
                            String pType = parameterTypes[parIdx].getName();
                            if (pType.charAt(0) == '[') {
                                pType = pType.substring(1);
                            } else if (pType.startsWith(ARRAY_WRAPPER_PREFIX)) {
                                pType = pType.substring(ARRAY_WRAPPER_PREFIX.length());
                            } else {
                                matched = false;
                                break;
                            }

                            if (argsDescriptor.length() - idx < 2) {
                                matched = false;
                                break;
                            }

                            char eType;
                            if (argsDescriptor.charAt(++idx) == ABIDecoder.ARRAY_S) {
                                if (pType.charAt(0) == '$' && pType.charAt(1) == '$') {
                                    pType = pType.substring(2);
                                }
                                else {
                                    matched = false;
                                    break;
                                }
                                eType = argsDescriptor.charAt(++idx);
                                idx = argsDescriptor.indexOf(ABIDecoder.ARRAY_E, idx);
                            }
                            else {
                                eType = argsDescriptor.charAt(idx);
                            }
                            idx = argsDescriptor.indexOf(ABIDecoder.ARRAY_E, idx);

                            if (pType.charAt(0) == 'L') {
                                pType = pType.substring(1);
                            }

                            if (!(Arrays.asList(elementaryTypesMap.get(eType)).contains(pType))) {
                                matched = false;
                                break;
                            }
                            break;
                        default:
                            if (!(Arrays.asList(elementaryTypesMap.get(c)).contains(parameterTypes[parIdx].getName()))) {
                                matched = false;
                                break;
                            }
                    }
                    if (!matched) {
                        break;
                    }
                    else {
                        parIdx ++;
                        if (parIdx == parameterTypes.length) {
                            break;
                        }
                    }
                }
                if (matched && parIdx == parameterTypes.length) {
                    return method;
                }
            }
        }
        return null;
    }


    /**
     * Convert the method call arguments, 1) take care of the array wrapping; 2) convert to an array list.
     *
     * @param arguments
     * @return
     */
    private static Object[] convertArguments(Object... arguments)
            throws InvalidTxDataException {
        List<Object> argList = new LinkedList<>(Arrays.asList(arguments));
        int originalSize = argList.size();

        for (int index = 0; index < originalSize; index ++) {
            Object obj = argList.get(index);

            // need to remove the empty ones at the end of the list; ABI does not allow null arguments
            if (obj == null) {
                argList.remove(index);
                continue;
            }

            // generate the array wrapping objects
            if (obj.getClass().isArray()) {
                Object newObj = null;
                String originalClassName = obj.getClass().getName();
                switch (originalClassName) {
                    case "[C":
                        newObj = new CharArray((char[]) obj);
                        argList.set(index, obj);
                        break;
                    case "[D":
                        newObj = new DoubleArray((double[]) obj);
                        argList.set(index, obj);
                        break;
                    case "[F":
                        newObj = new FloatArray((float[]) obj);
                        argList.set(index, obj);
                        break;
                    case "[I":
                        newObj = new IntArray((int[]) obj);
                        argList.set(index, obj);
                        break;
                    case "[J":
                        newObj = new LongArray((long[]) obj);
                        argList.set(index, obj);
                        break;
                    case "[S":
                        newObj = new ShortArray((short[]) obj);
                        argList.set(index, obj);
                        break;
                    case "[B":
                    case "[Z":
                        newObj = new ByteArray((byte[]) obj);
                        argList.set(index, obj);
                        break;
                    default:
                        if (!originalClassName.matches("\\[\\[[BZCDFIJS]")) {
                            throw new InvalidTxDataException();
                        }

                        //TODO convert 2D array objects to the wrapped ones
/*
                        // this is a 2D array
                        String arrayWrapperClassName = "org.aion.avm.arraywrapper.$$" + originalClassName.charAt(originalClassName.length()-1);
                        Class<?> clazz = sharedClassLoader.loadClass(arrayWrapperClassName);

                        Method initArray = clazz.getMethod("initArray", int.class);
                        Method set = clazz.getMethod("set", int.class, Object.class);

                        int firstDimension = ((Object[])obj).length;
                        newObj = initArray.invoke(null, firstDimension);
                        switch (originalClassName) {
                            case "[[B":
                            case "[[Z":
                                for (int i = 0; i < firstDimension; i++) {
                                    set.invoke(newObj, i, new ByteArray(((byte[][])obj)[i]));
                                }
                                break;
                            case "[[C":
                                for (int i = 0; i < firstDimension; i++) {
                                    set.invoke(newObj, i, new CharArray(((char[][])obj)[i]));
                                }
                                break;
                            case "[[D":
                                for (int i = 0; i < firstDimension; i++) {
                                    set.invoke(newObj, i, new DoubleArray(((double[][])obj)[i]));
                                }
                                break;
                            case "[[F":
                                for (int i = 0; i < firstDimension; i++) {
                                    set.invoke(newObj, i, new FloatArray(((float[][])obj)[i]));
                                }
                                break;
                            case "[[I":
                                for (int i = 0; i < firstDimension; i++) {
                                    set.invoke(newObj, i, new IntArray(((int[][])obj)[i]));
                                }
                                break;
                            case "[[J":
                                for (int i = 0; i < firstDimension; i++) {
                                    set.invoke(newObj, i, new LongArray(((long[][])obj)[i]));
                                }
                                break;
                            case "[[S":
                                for (int i = 0; i < firstDimension; i++) {
                                    set.invoke(newObj, i, new ShortArray(((short[][])obj)[i]));
                                }
                                break;
                        }*/

                        break;
                }

                // replace the original object with the new one
                argList.set(index, newObj);
            }
        }

        // return the array list
        return argList.toArray();
    }
}



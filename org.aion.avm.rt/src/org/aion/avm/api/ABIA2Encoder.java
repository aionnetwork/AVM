package org.aion.avm.api;

import org.aion.avm.arraywrapper.*;
import org.aion.avm.internal.IObject;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;

public class ABIA2Encoder {
    public enum ABITypes {
        avm_BYTE    ('B'),
        avm_BOOLEAN ('Z'),
        avm_CHAR    ('C'),
        avm_SHORT   ('S'),
        avm_INT     ('I'),
        avm_LONG    ('L'),
        avm_FLOAT   ('F'),
        avm_DOUBLE  ('D');

        private final char val;

        ABITypes(char val) {
            this.val = val;
        }
    }

    /*
     * Runtime-facing implementation.
     */
    public static ByteArray avm_encodeByte(byte data) {
        return new ByteArray(encodeByte(data));
    }

    public static ByteArray avm_encodeBoolean(boolean data) {
        return new ByteArray(encodeBoolean(data));
    }

    public static ByteArray avm_encodeChar(char data) {
        return new ByteArray(encodeChar(data));
    }

    public static ByteArray avm_encodeShort(short data) {
        return new ByteArray(encodeShort(data));
    }

    public static ByteArray avm_encodeInt(int data) {
        return new ByteArray(encodeInt(data));
    }

    public static ByteArray avm_encodeLong(long data) {
        return new ByteArray(encodeLong(data));
    }

    public static ByteArray avm_encodeFloat(float data) {
        return new ByteArray(encodeFloat(data));
    }

    public static ByteArray avm_encodeDouble(double data) {
        return new ByteArray(encodeDouble(data));
    }

    public static ByteArray avm_encode1DArray(IObject data, ABITypes type) {
        return new ByteArray(encode1DArray(data, type));
    }

    public static ByteArray avm_encode2DArray(IObject data, ABITypes type) throws InvalidTxDataException {
        return new ByteArray(encode2DArray(data, type));
    }

    public static ByteArray avm_encodeMethodArguments(org.aion.avm.shadow.java.lang.String methodAPI, ObjectArray arguments)  throws InvalidTxDataException {
        return new ByteArray(encodeMethodArguments(methodAPI.toString(), arguments));
    }


    /*
     * Underlying implementation.
     */

    public static byte[] encodeByte(byte data) {
        return new byte[]{data};
    }

    public static byte[] encodeBoolean(boolean data) {
        return new byte[]{(byte) (data ? 1 : 0)};
    }

    public static byte[] encodeChar(char data) {
        return Character.toString(data).getBytes();
    }

    public static byte[] encodeShort(short data) {
        return new byte[]{
                (byte)(data >>> 8),
                (byte)(data)};
    }

    public static byte[] encodeInt(int data) {
        return new byte[]{
                (byte)(data >>> 24),
                (byte)(data >>> 16),
                (byte)(data >>> 8),
                (byte)(data)};
    }

    public static byte[] encodeLong(long data) {
        return new byte[]{
                (byte)(data >>> 56),
                (byte)(data >>> 48),
                (byte)(data >>> 40),
                (byte)(data >>> 32),
                (byte)(data >>> 24),
                (byte)(data >>> 16),
                (byte)(data >>> 8),
                (byte)(data)};
    }

    public static byte[] encodeFloat(float data) {
        int i = Float.floatToRawIntBits(data);
        return encodeInt(i);
    }

    public static byte[] encodeDouble(double data) {
        long l = Double.doubleToRawLongBits(data);
        return encodeLong(l);
    }

    public static byte[] encode1DArray(Object data, ABITypes type) {
        String argumentDescriptor = "<[";

        switch (type) {
            case avm_CHAR:
                char[] dataC = ((CharArray) data).getUnderlying();
                argumentDescriptor += "C" + String.valueOf(dataC.length) + "]>";
                return (argumentDescriptor + String.valueOf(dataC)).getBytes();
            case avm_BOOLEAN:
                byte[] dataZ = ((ByteArray) data).getUnderlying();
                argumentDescriptor += "Z" + String.valueOf(dataZ.length) + "]>";
                byte[] descriptorZ = argumentDescriptor.getBytes();
                byte[] retZ = new byte[descriptorZ.length + dataZ.length];
                for (int i = 0; i < descriptorZ.length; i ++) {
                    retZ[i] = descriptorZ[i];
                }
                for (int i = descriptorZ.length, idx = 0; idx < dataZ.length; i ++, idx ++) {
                    retZ[i] = (byte) ((dataZ[idx] == 0) ? 0 : 1);
                }
                return retZ;
            case avm_SHORT:
                short[] dataS = ((ShortArray) data).getUnderlying();
                argumentDescriptor += "S" + String.valueOf(dataS.length) + "]>";
                byte[] descriptorS = argumentDescriptor.getBytes();
                byte[] retS = new byte[descriptorS.length + dataS.length * 2];
                for (int i = 0; i < descriptorS.length; i ++) {
                    retS[i] = descriptorS[i];
                }
                for (int i = descriptorS.length, idx = 0; idx < dataS.length; i += 2, idx ++) {
                    retS[i] = (byte)(dataS[idx] >>> 8);
                    retS[i + 1] = (byte)(dataS[idx]);
                }
                return retS;
            case avm_INT:
                int[] dataI = ((IntArray) data).getUnderlying();
                argumentDescriptor += "I" + String.valueOf(dataI.length) + "]>";
                byte[] descriptorI = argumentDescriptor.getBytes();
                byte[] retI = new byte[descriptorI.length + dataI.length * 4];
                for (int i = 0; i < descriptorI.length; i ++) {
                    retI[i] = descriptorI[i];
                }
                for (int i = descriptorI.length, idx = 0; idx < dataI.length; i += 4, idx ++) {
                    retI[i] = (byte)(dataI[idx] >>> 24);
                    retI[i + 1] = (byte)(dataI[idx] >>> 16);
                    retI[i + 2] = (byte)(dataI[idx] >>> 8);
                    retI[i + 3] = (byte)(dataI[idx]);
                }
                return retI;
            case avm_LONG:
                long[] dataL = ((LongArray) data).getUnderlying();
                argumentDescriptor += "L" + String.valueOf(dataL.length) + "]>";
                byte[] descriptorL = argumentDescriptor.getBytes();
                byte[] retL = new byte[descriptorL.length + dataL.length * 8];
                for (int i = 0; i < descriptorL.length; i ++) {
                    retL[i] = descriptorL[i];
                }
                for (int i = descriptorL.length, idx = 0; idx < dataL.length; i += 8, idx ++) {
                    retL[i] = (byte)(dataL[idx] >>> 56);
                    retL[i + 1] = (byte)(dataL[idx] >>> 48);
                    retL[i + 2] = (byte)(dataL[idx] >>> 40);
                    retL[i + 3] = (byte)(dataL[idx] >>> 32);
                    retL[i + 4] = (byte)(dataL[idx] >>> 24);
                    retL[i + 5] = (byte)(dataL[idx] >>> 16);
                    retL[i + 6] = (byte)(dataL[idx] >>> 8);
                    retL[i + 7] = (byte)(dataL[idx]);
                }
                return retL;
            case avm_FLOAT:
                float[] dataF = ((FloatArray) data).getUnderlying();
                argumentDescriptor += "F" + String.valueOf(dataF.length) + "]>";
                byte[] descriptorF = argumentDescriptor.getBytes();
                byte[] retF = new byte[descriptorF.length + dataF.length * 4];
                for (int i = 0; i < descriptorF.length; i ++) {
                    retF[i] = descriptorF[i];
                }
                for (int i = descriptorF.length, idx = 0; idx < dataF.length; i += 4, idx ++) {
                    byte[] curF = encodeFloat(dataF[idx]);
                    retF[i] = curF[0];
                    retF[i + 1] = curF[1];
                    retF[i + 2] = curF[2];
                    retF[i + 3] = curF[3];
                }
                return retF;
            case avm_DOUBLE:
                double[] dataD = ((DoubleArray) data).getUnderlying();
                argumentDescriptor += "D" + String.valueOf(dataD.length) + "]>";
                byte[] descriptorD = argumentDescriptor.getBytes();
                byte[] retD = new byte[descriptorD.length + dataD.length * 8];
                for (int i = 0; i < descriptorD.length; i ++) {
                    retD[i] = descriptorD[i];
                }
                for (int i = descriptorD.length, idx = 0; idx < dataD.length; i += 8, idx ++) {
                    byte[] curD = encodeDouble(dataD[idx]);
                    retD[i] = curD[0];
                    retD[i + 1] = curD[1];
                    retD[i + 2] = curD[2];
                    retD[i + 3] = curD[3];
                    retD[i + 4] = curD[4];
                    retD[i + 5] = curD[5];
                    retD[i + 6] = curD[6];
                    retD[i + 7] = curD[7];
                }
                return retD;
            case avm_BYTE:
                byte[] dataB = ((ByteArray) data).getUnderlying();
                argumentDescriptor += "B" + String.valueOf(dataB.length) + "]>";
                byte[] descriptorB = argumentDescriptor.getBytes();
                byte[] retB = new byte[descriptorB.length + dataB.length];
                for (int i = 0; i < descriptorB.length; i ++) {
                    retB[i] = descriptorB[i];
                }
                for (int i = descriptorB.length; i < retB.length; i ++) {
                    retB[i] = dataB[i - descriptorB.length];
                }
                return retB;
            default:
                return null;
        }
    }

    /**
     * Encode a 2D array of an elementary type. Include the argument descriptor at the beginning of the return byte array.
     *
     * @param data
     * @param type
     * @return
     */
    public static byte[] encode2DArray(Object data, ABITypes type) throws InvalidTxDataException {
        int size = 0;
        String argumentDescriptor = "<[[";
        Method getUnderlyingAsObject;
        try {
            getUnderlyingAsObject = data.getClass().getMethod("getUnderlyingAsObject");
            data = getUnderlyingAsObject.invoke(data);

            // TODO data is 2D array wrapped; need to un-wrap it first
            switch (type) {
                case avm_CHAR:
                    char[][] dataC = new char[((Object[]) data).length][];
                    for (int i = 0; i < ((Object[]) data).length; i++) {
                        dataC[i] = ((CharArray) ((Object[])data)[i]).getUnderlying();
                    }
                    argumentDescriptor += "C]" + String.valueOf(dataC.length) + "]";
                    String s = "";
                    for (int i = 0; i < dataC.length; i ++) {
                        argumentDescriptor += "(" + String.valueOf(dataC[i].length) + ")";
                        s = s + String.valueOf(dataC[i]);
                    }
                    s = argumentDescriptor + ">" + s;
                    return s.getBytes();
                case avm_SHORT:
                    short[][] dataS = new short[((Object[]) data).length][];
                    for (int i = 0; i < ((Object[]) data).length; i++) {
                        dataS[i] = ((ShortArray) ((Object[])data)[i]).getUnderlying();
                    }
                    argumentDescriptor += "S]" + String.valueOf(dataS.length) + "]";
                    for (int i = 0; i < dataS.length; i ++) {
                        argumentDescriptor += "(" + String.valueOf(dataS[i].length) + ")";
                        size += dataS[i].length;
                    }
                    byte[] descriptorS = (argumentDescriptor + ">").getBytes();
                    byte[] retS = new byte[size * 2 + descriptorS.length];
                    System.arraycopy(descriptorS, 0, retS, 0, descriptorS.length);
                    for (int i = 0, idx = descriptorS.length; i < dataS.length; i ++) {
                        for (int j = 0; j < dataS[i].length; j ++, idx += 2) {
                            retS[idx] = (byte) (dataS[i][j] >>> 8);
                            retS[idx + 1] = (byte) (dataS[i][j]);
                        }
                    }
                    return retS;
                case avm_INT:
                    int[][] dataI = new int[((Object[]) data).length][];
                    for (int i = 0; i < ((Object[]) data).length; i++) {
                        dataI[i] = ((IntArray) ((Object[])data)[i]).getUnderlying();
                    }
                    argumentDescriptor += "I]" + String.valueOf(dataI.length) + "]";
                    for (int i = 0; i < dataI.length; i ++) {
                        argumentDescriptor += "(" + String.valueOf(dataI[i].length) + ")";
                        size += dataI[i].length;
                    }
                    byte[] descriptorI = (argumentDescriptor + ">").getBytes();
                    byte[] retI = new byte[size * 4 + descriptorI.length];
                    System.arraycopy(descriptorI, 0, retI, 0, descriptorI.length);
                    for (int i = 0, idx = descriptorI.length; i < dataI.length; i ++) {
                        for (int j = 0; j < dataI[i].length; j ++, idx += 4) {
                            retI[idx] = (byte) (dataI[i][j] >>> 24);
                            retI[idx + 1] = (byte) (dataI[i][j] >>> 16);
                            retI[idx + 2] = (byte) (dataI[i][j] >>> 8);
                            retI[idx + 3] = (byte) (dataI[i][j]);
                        }
                    }
                    return retI;
                case avm_LONG:
                    long[][] dataL = new long[((Object[]) data).length][];
                    for (int i = 0; i < ((Object[]) data).length; i++) {
                        dataL[i] = ((LongArray) ((Object[])data)[i]).getUnderlying();
                    }
                    argumentDescriptor += "L]" + String.valueOf(dataL.length) + "]";
                    for (int i = 0; i < dataL.length; i ++) {
                        argumentDescriptor += "(" + String.valueOf(dataL[i].length) + ")";
                        size += dataL[i].length;
                    }
                    byte[] descriptorL = (argumentDescriptor + ">").getBytes();
                    byte[] retL = new byte[size * 8 + descriptorL.length];
                    System.arraycopy(descriptorL, 0, retL, 0, descriptorL.length);
                    for (int i = 0, idx = descriptorL.length; i < dataL.length; i ++) {
                        for (int j = 0; j < dataL[i].length; j ++, idx += 8) {
                            retL[idx] = (byte) (dataL[i][j] >>> 56);
                            retL[idx + 1] = (byte) (dataL[i][j] >>> 48);
                            retL[idx + 2] = (byte) (dataL[i][j] >>> 40);
                            retL[idx + 3] = (byte) (dataL[i][j] >>> 32);
                            retL[idx + 4] = (byte) (dataL[i][j] >>> 24);
                            retL[idx + 5] = (byte) (dataL[i][j] >>> 16);
                            retL[idx + 6] = (byte) (dataL[i][j] >>> 8);
                            retL[idx + 7] = (byte) (dataL[i][j]);
                        }
                    }
                    return retL;
                case avm_FLOAT:
                    float[][] dataF = new float[((Object[]) data).length][];
                    for (int i = 0; i < ((Object[]) data).length; i++) {
                        dataF[i] = ((FloatArray) ((Object[])data)[i]).getUnderlying();
                    }
                    argumentDescriptor += "F]" + String.valueOf(dataF.length) + "]";
                    for (int i = 0; i < dataF.length; i ++) {
                        argumentDescriptor += "(" + String.valueOf(dataF[i].length) + ")";
                        size += dataF[i].length;
                    }
                    byte[] descriptorF = (argumentDescriptor + ">").getBytes();
                    byte[] retF = new byte[size * 4 + descriptorF.length];
                    System.arraycopy(descriptorF, 0, retF, 0, descriptorF.length);
                    for (int i = 0, idx = descriptorF.length; i < dataF.length; i ++) {
                        for (int j = 0; j < dataF[i].length; j ++, idx += 4) {
                            byte[] curF = encodeFloat(dataF[i][j]);
                            retF[idx] = curF[0];
                            retF[idx + 1] = curF[1];
                            retF[idx + 2] = curF[2];
                            retF[idx + 3] = curF[3];
                        }
                    }
                    return retF;
                case avm_DOUBLE:
                    double[][] dataD = new double[((Object[]) data).length][];
                    for (int i = 0; i < ((Object[]) data).length; i++) {
                        dataD[i] = ((DoubleArray) ((Object[])data)[i]).getUnderlying();
                    }
                    argumentDescriptor += "D]" + String.valueOf(dataD.length) + "]";
                    for (int i = 0; i < dataD.length; i ++) {
                        argumentDescriptor += "(" + String.valueOf(dataD[i].length) + ")";
                        size += dataD[i].length;
                    }
                    byte[] descriptorD = (argumentDescriptor + ">").getBytes();
                    byte[] retD = new byte[size * 8 + descriptorD.length];
                    System.arraycopy(descriptorD, 0, retD, 0, descriptorD.length);
                    for (int i = 0, idx = descriptorD.length; i < dataD.length; i ++) {
                        for (int j = 0; j < dataD[i].length; j ++, idx += 8) {
                            byte[] curD = encodeDouble(dataD[i][j]);
                            retD[idx] = curD[0];
                            retD[idx + 1] = curD[1];
                            retD[idx + 2] = curD[2];
                            retD[idx + 3] = curD[3];
                            retD[idx + 4] = curD[4];
                            retD[idx + 5] = curD[5];
                            retD[idx + 6] = curD[6];
                            retD[idx + 7] = curD[7];
                        }
                    }
                    return retD;
                case avm_BOOLEAN:
                    byte[][] dataZ = new byte[((Object[]) data).length][];
                    for (int i = 0; i < ((Object[]) data).length; i++) {
                        dataZ[i] = ((ByteArray) ((Object[])data)[i]).getUnderlying();
                    }
                    argumentDescriptor += "Z]" + String.valueOf(dataZ.length) + "]";
                    for (int i = 0; i < dataZ.length; i ++) {
                        argumentDescriptor += "(" + String.valueOf(dataZ[i].length) + ")";
                        size += dataZ[i].length;
                    }
                    byte[] descriptorZ = (argumentDescriptor + ">").getBytes();
                    byte[] retZ = new byte[size + descriptorZ.length];
                    System.arraycopy(descriptorZ, 0, retZ, 0, descriptorZ.length);
                    for (int i = 0, idx = descriptorZ.length; i < dataZ.length; i ++) {
                        for (int j = 0; j < dataZ[i].length; j ++, idx ++) {
                            retZ[idx] = (byte) ((dataZ[i][j] == 0) ? 0 : 1);
                        }
                    }
                    return retZ;
                case avm_BYTE:
                    byte[][] dataB = new byte[((Object[]) data).length][];
                    for (int i = 0; i < ((Object[]) data).length; i++) {
                        dataB[i] = ((ByteArray) ((Object[])data)[i]).getUnderlying();
                    }
                    argumentDescriptor += "B]" + String.valueOf(dataB.length) + "]";
                    for (int i = 0; i < dataB.length; i ++) {
                        argumentDescriptor += "(" + String.valueOf(dataB[i].length) + ")";
                        size += dataB[i].length;
                    }
                    byte[] descriptorB = (argumentDescriptor + ">").getBytes();
                    byte[] retB = new byte[size + descriptorB.length];
                    System.arraycopy(descriptorB, 0, retB, 0, descriptorB.length);
                    for (int i = 0, idx = descriptorB.length; i < dataB.length; i ++) {
                        for (int j = 0; j < dataB[i].length; j ++, idx ++) {
                            retB[idx] = dataB[i][j];
                        }
                    }
                    return retB;
                default:
                    return null;
            }
        } catch (Exception e) {
            throw new InvalidTxDataException();
        }
    }

    public static byte[] encodeArguments(String methodName, ABITypes[] argumentTypes, int[] dimensions, int numberOfArguments, Object... arguments)  throws InvalidTxDataException {
        if (arguments.length != numberOfArguments || argumentTypes.length != numberOfArguments) {
            throw new InvalidTxDataException();
        }

        String descriptor = methodName + "<";
        byte[][] argumentBytes = new byte[numberOfArguments][];
        int[] startByteIndex = new int[numberOfArguments];
        int size = 0;

        for (int idx = 0; idx < numberOfArguments; idx ++) {
            // encode each argument based on the type; record the size
            if (dimensions[idx] == 0) {
                switch (argumentTypes[idx]) {
                    case avm_CHAR:
                        descriptor += String.valueOf('C');
                        argumentBytes[idx] = encodeChar((char) arguments[idx]);
                        break;
                    case avm_BYTE:
                        descriptor += String.valueOf('B');
                        argumentBytes[idx] = encodeByte((byte) arguments[idx]);
                        break;
                    case avm_BOOLEAN:
                        descriptor += String.valueOf('Z');
                        argumentBytes[idx] = encodeBoolean((boolean) arguments[idx]);
                        break;
                    case avm_SHORT:
                        descriptor += String.valueOf('S');
                        argumentBytes[idx] = encodeShort((short) arguments[idx]);
                        break;
                    case avm_INT:
                        descriptor += String.valueOf('I');
                        argumentBytes[idx] = encodeInt((int) arguments[idx]);
                        break;
                    case avm_LONG:
                        descriptor += String.valueOf('L');
                        argumentBytes[idx] = encodeLong((long) arguments[idx]);
                        break;
                    case avm_FLOAT:
                        descriptor += String.valueOf('F');
                        argumentBytes[idx] = encodeFloat((float) arguments[idx]);
                        break;
                    case avm_DOUBLE:
                        descriptor += String.valueOf('D');
                        argumentBytes[idx] = encodeDouble((double) arguments[idx]);
                        break;
                    default:
                        throw new InvalidTxDataException();
                }
            }
            else {
                byte[] encodedData;
                if (dimensions[idx] == 1) {
                    encodedData = encode1DArray(((ObjectArray)arguments[idx]).getUnderlying()[0], argumentTypes[idx]);
                }
                else if (dimensions[idx] == 2) {
                    encodedData = encode2DArray(((ObjectArray)arguments[idx]).getUnderlying()[0], argumentTypes[idx]);
                }
                else {
                    throw new InvalidTxDataException();
                }

                String encoded = new String(encodedData);
                int m = encoded.indexOf(ABIA2Decoder.DESCRIPTOR_E);
                encoded = encoded.substring(0, m+1);

                descriptor += encoded.substring(1, m);
                argumentBytes[idx] = encodedData;
                startByteIndex[idx] = encoded.getBytes().length;
            }

            size += argumentBytes[idx].length - startByteIndex[idx];
        }

        byte[] descriptorB = (descriptor + ">").getBytes();

        // copy the descriptor and then the argumentBytes
        byte[] returnedBytes = new byte[descriptorB.length + size];
        System.arraycopy(descriptorB, 0, returnedBytes, 0, descriptorB.length);
        for (int i = 0, idx = descriptorB.length; i < numberOfArguments; i ++) {
            System.arraycopy(argumentBytes[i], startByteIndex[i], returnedBytes, idx, argumentBytes[i].length - startByteIndex[i]);
            idx += argumentBytes[i].length - startByteIndex[i];
        }

        return returnedBytes;
    }

    public static byte[] encodeMethodArguments(String methodAPI, Object... arguments)  throws InvalidTxDataException {
        int m2 = methodAPI.indexOf('(');
        int m1 = methodAPI.lastIndexOf(' ', m2);
        int m3 = methodAPI.indexOf(')', m2);

        if (m2 == -1 || m3 == -1) {
            throw new InvalidTxDataException();
        }

        String methodName = methodAPI.substring(m1+1, m2);

        int argsCount = 0;
        String[] argTypes = new String[10];
        int m4 = m2;
        while (m4 != -1 && m4 < m3) {
            while (methodAPI.charAt(++m4) == ' ') {}
            int m5 = methodAPI.indexOf(' ', m4);

            if (m5 != -1) {
                argTypes[argsCount] = methodAPI.substring(m4, m5);
                // Allow spaces in argument type, e.g. "int[]", "int [ ]", "int [ ] [ ]" are all valid
                while (methodAPI.charAt(m5) == ' ' || methodAPI.charAt(m5) == '[' || methodAPI.charAt(m5) == ']') {
                    if (methodAPI.charAt(m5) != ' ') {
                        argTypes[argsCount] += String.valueOf(methodAPI.charAt(m5));
                    }
                    m5++;
                }
                argsCount++;
            }

            m4 = methodAPI.indexOf(',', m4);

            if (argsCount == argTypes.length) {
                String[] argTypesNew = new String[argTypes.length + 10];
                System.arraycopy(argTypes, 0, argTypesNew, 0, argTypes.length);
                argTypes = argTypesNew;
            }
        }


        ABITypes[] argumentTypes = new ABITypes[argsCount];
        int[] dimensions = new int[argsCount];

        for (int i = 0; i < argsCount; i ++) {
            int n1 = argTypes[i].indexOf('[');
            if (n1 == -1) {
                dimensions[i] = 0;
            }
            else {
                int n2 = argTypes[i].indexOf('[', n1+1);
                if (n2 == -1) {
                    dimensions[i] = 1;
                }
                else {
                    dimensions[i] = 2;
                }
                argTypes[i] = argTypes[i].substring(0, n1);
            }

            switch (argTypes[i]) {
                case "byte":
                    argumentTypes[i] = ABITypes.avm_BYTE;
                    break;
                case "char":
                    argumentTypes[i] = ABITypes.avm_CHAR;
                    break;
                case "boolean":
                    argumentTypes[i] = ABITypes.avm_BOOLEAN;
                    break;
                case "short":
                    argumentTypes[i] = ABITypes.avm_SHORT;
                    break;
                case "int":
                    argumentTypes[i] = ABITypes.avm_INT;
                    break;
                case "long":
                    argumentTypes[i] = ABITypes.avm_LONG;
                    break;
                case "float":
                    argumentTypes[i] = ABITypes.avm_FLOAT;
                    break;
                case "double":
                    argumentTypes[i] = ABITypes.avm_DOUBLE;
                    break;
                default:
                    throw new InvalidTxDataException();
            }
        }

        return encodeArguments(methodName, argumentTypes, dimensions, argsCount, arguments);
    }
}




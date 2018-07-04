package com.example.deployAndRunTest;

public class ABIEncoder {
    public enum ABITypes {
        BYTE    ('B'),
        BOOLEAN ('Z'),
        CHAR    ('C'),
        SHORT   ('S'),
        INT     ('I'),
        LONG    ('L'),
        FLOAT   ('F'),
        DOUBLE  ('D');

        private final char val;

        ABITypes(char val) {
            this.val = val;
        }
    }

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
            case CHAR:
                char[] dataC = (char[]) data;
                argumentDescriptor += String.valueOf('C') + String.valueOf(dataC.length) + closeSquare + closeAngle;
                return (argumentDescriptor + String.valueOf(dataC)).getBytes();
            case BOOLEAN:
                boolean[] dataZ = (boolean[]) data;
                argumentDescriptor += String.valueOf('Z') + String.valueOf(dataZ.length) + closeSquare + closeAngle;
                byte[] descriptorZ = argumentDescriptor.getBytes();
                byte[] retZ = new byte[descriptorZ.length + dataZ.length];
                for (int i = 0; i < descriptorZ.length; i ++) {
                    retZ[i] = descriptorZ[i];
                }
                for (int i = descriptorZ.length, idx = 0; idx < dataZ.length; i ++, idx ++) {
                    retZ[i] = (byte) (dataZ[idx] ? 1 : 0);
                }
                return retZ;
            case SHORT:
                short[] dataS = (short[]) data;
                argumentDescriptor += String.valueOf('S') + String.valueOf(dataS.length) + closeSquare + closeAngle;
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
            case INT:
                int[] dataI = (int[]) data;
                argumentDescriptor += String.valueOf('I') + String.valueOf(dataI.length) + closeSquare + closeAngle;
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
            case LONG:
                long[] dataL = (long[]) data;
                argumentDescriptor += String.valueOf('L') + String.valueOf(dataL.length) + closeSquare + closeAngle;
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
            case FLOAT:
                float[] dataF = (float[]) data;
                argumentDescriptor += String.valueOf('F') + String.valueOf(dataF.length) + closeSquare + closeAngle;
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
            case DOUBLE:
                double[] dataD = (double[]) data;
                argumentDescriptor += String.valueOf('D') + String.valueOf(dataD.length) + closeSquare + closeAngle;
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
            case BYTE:
                argumentDescriptor += String.valueOf('B') + String.valueOf(((byte[])data).length) + closeSquare + closeAngle;
                byte[] descriptorB = argumentDescriptor.getBytes();
                byte[] retB = new byte[descriptorB.length + ((byte[])data).length];
                for (int i = 0; i < descriptorB.length; i ++) {
                    retB[i] = descriptorB[i];
                }
                for (int i = descriptorB.length; i < retB.length; i ++) {
                    retB[i] = ((byte[])data)[i - descriptorB.length];
                }
                return retB;
            default:
                return null;
        }
    }

    // temporary code before we can do string concatenation with constant Strings.
    private static String openSquare   = "[";
    private static String closeSquare  = "]";
    private static String openBracket  = "(";
    private static String closeBracket = ")";
    private static String openAngle    = "<";
    private static String closeAngle   = ">";

    /**
     * Encode a 2D array of an elementary type. Include the argument descriptor at the beginning of the return byte array.
     *
     * @param data
     * @param type
     * @return
     */
    public static byte[] encode2DArray(Object data, ABITypes type) {
        int size = 0;
        String argumentDescriptor = "<[[";

        switch (type) {
            case CHAR:
                char[][] dataC = (char[][]) data;
                argumentDescriptor += String.valueOf('C') + closeSquare + String.valueOf(dataC.length) + closeSquare;
                String s = "";
                for (int i = 0; i < dataC.length; i ++) {
                    argumentDescriptor += openBracket + String.valueOf(dataC[i].length) + closeBracket;
                    s = s + String.valueOf(dataC[i]);
                }
                s = argumentDescriptor + closeAngle + s;
                return s.getBytes();
            case BOOLEAN:
                boolean[][] dataZ = (boolean[][]) data;
                argumentDescriptor += String.valueOf('Z') + closeSquare + String.valueOf(dataZ.length) + closeSquare;
                for (int i = 0; i < dataZ.length; i ++) {
                    argumentDescriptor += openBracket + String.valueOf(dataZ[i].length) + closeBracket;
                    size += dataZ[i].length;
                }
                byte[] descriptorZ = (argumentDescriptor + closeAngle).getBytes();
                byte[] retZ = new byte[size + descriptorZ.length];
                for (int i = 0; i < descriptorZ.length; i ++) {
                    retZ[i] = descriptorZ[i];
                }
                for (int i = 0, idx = descriptorZ.length; i < dataZ.length; i ++) {
                    for (int j = 0; j < dataZ[i].length; j ++, idx ++) {
                        retZ[idx] = (byte) (dataZ[i][j] ? 1 : 0);
                    }
                }
                return retZ;
            case SHORT:
                short[][] dataS = (short[][]) data;
                argumentDescriptor += String.valueOf('S') + closeSquare + String.valueOf(dataS.length) + closeSquare;
                for (int i = 0; i < dataS.length; i ++) {
                    argumentDescriptor += openBracket + String.valueOf(dataS[i].length) + closeBracket;
                    size += dataS[i].length;
                }
                byte[] descriptorS = (argumentDescriptor + closeAngle).getBytes();
                byte[] retS = new byte[size * 2 + descriptorS.length];
                for (int i = 0; i < descriptorS.length; i ++) {
                    retS[i] = descriptorS[i];
                }
                for (int i = 0, idx = descriptorS.length; i < dataS.length; i ++) {
                    for (int j = 0; j < dataS[i].length; j ++, idx += 2) {
                        retS[idx] = (byte) (dataS[i][j] >>> 8);
                        retS[idx + 1] = (byte) (dataS[i][j]);
                    }
                }
                return retS;
            case INT:
                int[][] dataI = (int[][]) data;
                argumentDescriptor += String.valueOf('I') + closeSquare + String.valueOf(dataI.length) + closeSquare;
                for (int i = 0; i < dataI.length; i ++) {
                    argumentDescriptor += openBracket + String.valueOf(dataI[i].length) + closeBracket;
                    size += dataI[i].length;
                }
                byte[] descriptorI = (argumentDescriptor + closeAngle).getBytes();
                byte[] retI = new byte[size * 4 + descriptorI.length];
                for (int i = 0; i < descriptorI.length; i ++) {
                    retI[i] = descriptorI[i];
                }
                for (int i = 0, idx = descriptorI.length; i < dataI.length; i ++) {
                    for (int j = 0; j < dataI[i].length; j ++, idx += 4) {
                        retI[idx] = (byte) (dataI[i][j] >>> 24);
                        retI[idx + 1] = (byte) (dataI[i][j] >>> 16);
                        retI[idx + 2] = (byte) (dataI[i][j] >>> 8);
                        retI[idx + 3] = (byte) (dataI[i][j]);
                    }
                }
                return retI;
            case LONG:
                long[][] dataL = (long[][]) data;
                argumentDescriptor += String.valueOf('L') + closeSquare + String.valueOf(dataL.length) + closeSquare;
                for (int i = 0; i < dataL.length; i ++) {
                    argumentDescriptor += openBracket + String.valueOf(dataL[i].length) + closeBracket;
                    size += dataL[i].length;
                }
                byte[] descriptorL = (argumentDescriptor + closeAngle).getBytes();
                byte[] retL = new byte[size * 8 + descriptorL.length];
                for (int i = 0; i < descriptorL.length; i ++) {
                    retL[i] = descriptorL[i];
                }
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
            case FLOAT:
                float[][] dataF = (float[][]) data;
                argumentDescriptor += String.valueOf('F') + closeSquare + String.valueOf(dataF.length) + closeSquare;
                for (int i = 0; i < dataF.length; i ++) {
                    argumentDescriptor += openBracket + String.valueOf(dataF[i].length) + closeBracket;
                    size += dataF[i].length;
                }
                byte[] descriptorF = (argumentDescriptor + closeAngle).getBytes();
                byte[] retF = new byte[size * 4 + descriptorF.length];
                for (int i = 0; i < descriptorF.length; i ++) {
                    retF[i] = descriptorF[i];
                }
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
            case DOUBLE:
                double[][] dataD = (double[][]) data;
                argumentDescriptor += String.valueOf('D') + closeSquare + String.valueOf(dataD.length) + closeSquare;
                for (int i = 0; i < dataD.length; i ++) {
                    argumentDescriptor += openBracket + String.valueOf(dataD[i].length) + closeBracket;
                    size += dataD[i].length;
                }
                byte[] descriptorD = (argumentDescriptor + closeAngle).getBytes();
                byte[] retD = new byte[size * 8 + descriptorD.length];
                for (int i = 0; i < descriptorD.length; i ++) {
                    retD[i] = descriptorD[i];
                }
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
            case BYTE:
                byte[][] dataB = (byte[][]) data;
                argumentDescriptor += String.valueOf('B') + closeSquare + String.valueOf(dataB.length) + closeSquare;
                for (int i = 0; i < dataB.length; i ++) {
                    argumentDescriptor += openBracket + String.valueOf(dataB[i].length) + closeBracket;
                    size += dataB[i].length;
                }
                byte[] descriptorB = (argumentDescriptor + closeAngle).getBytes();
                byte[] retB = new byte[size + descriptorB.length];
                for (int i = 0; i < descriptorB.length; i ++) {
                    retB[i] = descriptorB[i];
                }
                for (int i = 0, idx = descriptorB.length; i < dataB.length; i ++) {
                    for (int j = 0; j < dataB[i].length; j ++, idx ++) {
                        retB[idx] = dataB[i][j];
                    }
                }
                return retB;
            default:
                return null;
        }
    }

    public static byte[] encodeArguments(String methodName, ABITypes[] argumentTypes, int[] dimensions, int numberOfArguments, Object... arguments)  throws InvalidTxDataException {
        if (arguments.length != numberOfArguments || argumentTypes.length != numberOfArguments) {
            throw new InvalidTxDataException();
        }

        String descriptor = methodName + openAngle;
        byte[][] argumentBytes = new byte[numberOfArguments][];
        int[] startByteIndex = new int[numberOfArguments];
        int size = 0;

        for (int idx = 0; idx < numberOfArguments; idx ++) {
            // encode each argument based on the type; record the size
            if (dimensions[idx] == 0) {
                switch (argumentTypes[idx]) {
                    case CHAR:
                        descriptor += String.valueOf('C');
                        argumentBytes[idx] = encodeChar((char) arguments[idx]);
                        break;
                    case BYTE:
                        descriptor += String.valueOf('B');
                        argumentBytes[idx] = encodeByte((byte) arguments[idx]);
                        break;
                    case BOOLEAN:
                        descriptor += String.valueOf('Z');
                        argumentBytes[idx] = encodeBoolean((boolean) arguments[idx]);
                        break;
                    case SHORT:
                        descriptor += String.valueOf('S');
                        argumentBytes[idx] = encodeShort((short) arguments[idx]);
                        break;
                    case INT:
                        descriptor += String.valueOf('I');
                        argumentBytes[idx] = encodeInt((int) arguments[idx]);
                        break;
                    case LONG:
                        descriptor += String.valueOf('L');
                        argumentBytes[idx] = encodeLong((long) arguments[idx]);
                        break;
                    case FLOAT:
                        descriptor += String.valueOf('F');
                        argumentBytes[idx] = encodeFloat((float) arguments[idx]);
                        break;
                    case DOUBLE:
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
                    encodedData = encode1DArray(arguments[idx], argumentTypes[idx]);
                }
                else if (dimensions[idx] == 2) {
                    encodedData = encode2DArray(arguments[idx], argumentTypes[idx]);
                }
                else {
                    throw new InvalidTxDataException();
                }

                String encoded = new String(encodedData);
                int m = encoded.indexOf(ABIDecoder.DESCRIPTOR_E);
                encoded = encoded.substring(0, m+1);

                descriptor += encoded.substring(1, m);
                argumentBytes[idx] = encodedData;
                startByteIndex[idx] = encoded.getBytes().length;
            }

            size += argumentBytes[idx].length - startByteIndex[idx];
        }

        byte[] descriptorB = (descriptor + closeAngle).getBytes();

        // copy the descriptor and then the argumentBytes
        byte[] returnedBytes = new byte[descriptorB.length + size];
        for (int i = 0; i < descriptorB.length; i ++) {
            returnedBytes[i] = descriptorB[i];
        }
        for (int i = 0, idx = descriptorB.length; i < numberOfArguments; i ++) {
            for (int j = startByteIndex[i]; j < argumentBytes[i].length; j ++, idx ++) {
                returnedBytes[idx] = argumentBytes[i][j];
            }
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
                for (int i = 0; i < argTypes.length; i ++) {
                    argTypesNew[i] = argTypes[i];
                }
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
                    argumentTypes[i] = ABITypes.BYTE;
                    break;
                case "char":
                    argumentTypes[i] = ABITypes.CHAR;
                    break;
                case "boolean":
                    argumentTypes[i] = ABITypes.BOOLEAN;
                    break;
                case "short":
                    argumentTypes[i] = ABITypes.SHORT;
                    break;
                case "int":
                    argumentTypes[i] = ABITypes.INT;
                    break;
                case "long":
                    argumentTypes[i] = ABITypes.LONG;
                    break;
                case "float":
                    argumentTypes[i] = ABITypes.FLOAT;
                    break;
                case "double":
                    argumentTypes[i] = ABITypes.DOUBLE;
                    break;
                default:
                    throw new InvalidTxDataException();
            }
        }

        return encodeArguments(methodName, argumentTypes, dimensions, argsCount, arguments);
    }
}


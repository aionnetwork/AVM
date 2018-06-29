package com.example.helloworld;

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
        switch (type) {
            case CHAR:
                return String.valueOf((char[]) data).getBytes();
            case BOOLEAN:
                boolean[] dataZ = (boolean[]) data;
                byte[] retZ = new byte[dataZ.length];
                for (int i = 0; i < dataZ.length; i ++) {
                    retZ[i] = (byte) (dataZ[i] ? 1 : 0);
                }
                return retZ;
            case SHORT:
                short[] dataS = (short[]) data;
                byte[] retS = new byte[dataS.length * 2];
                for (int i = 0; i < dataS.length * 2; i += 2) {
                    retS[i] = (byte)(dataS[i] >>> 8);
                    retS[i + 1] = (byte)(dataS[i]);
                }
                return retS;
            case INT:
                int[] dataI = (int[]) data;
                byte[] retI = new byte[dataI.length * 4];
                for (int i = 0; i < dataI.length * 4; i += 4) {
                    retI[i] = (byte)(dataI[i] >>> 24);
                    retI[i + 1] = (byte)(dataI[i] >>> 16);
                    retI[i + 2] = (byte)(dataI[i] >>> 8);
                    retI[i + 3] = (byte)(dataI[i]);
                }
                return retI;
            case LONG:
                long[] dataL = (long[]) data;
                byte[] retL = new byte[dataL.length * 8];
                for (int i = 0; i < dataL.length * 8; i += 8) {
                    retL[i] = (byte)(dataL[i] >>> 56);
                    retL[i + 1] = (byte)(dataL[i] >>> 48);
                    retL[i + 2] = (byte)(dataL[i] >>> 40);
                    retL[i + 3] = (byte)(dataL[i] >>> 32);
                    retL[i + 4] = (byte)(dataL[i] >>> 24);
                    retL[i + 5] = (byte)(dataL[i] >>> 16);
                    retL[i + 6] = (byte)(dataL[i] >>> 8);
                    retL[i + 7] = (byte)(dataL[i]);
                }
                return retL;
            case FLOAT:
                float[] dataF = (float[]) data;
                byte[] retF = new byte[dataF.length * 4];
                for (int i = 0; i < dataF.length * 4; i += 4) {
                    byte[] curF = encodeFloat(dataF[i]);
                    retF[i] = curF[0];
                    retF[i + 1] = curF[1];
                    retF[i + 2] = curF[2];
                    retF[i + 3] = curF[3];
                }
                return retF;
            case DOUBLE:
                double[] dataD = (double[]) data;
                byte[] retD = new byte[dataD.length * 8];
                for (int i = 0; i < dataD.length * 8; i += 8) {
                    byte[] curD = encodeDouble(dataD[i]);
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
                return (byte[]) data;
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
    public static byte[] encode2DArray(Object data, ABITypes type) {
        int size = 0;
        String argumentDescriptor = "<[[";

        // temporary code before we can do string concatenation with constant Strings.
        String closeSquare  = "]";
        String openBracket  = "(";
        String closeBracket = ")";
        String closeAngle   = ">";

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
}


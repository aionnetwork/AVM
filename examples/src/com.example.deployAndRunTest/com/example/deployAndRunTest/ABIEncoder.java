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

    public static byte[] encode2DArray(Object data, ABITypes type) {
        // TODO: now we assume the receiver knows the dimension of the 2D array. May revise later, aka, pass the argument descriptor
        int size = 0;

        switch (type) {
            case CHAR:
                char[][] dataC = (char[][]) data;
                String s = String.valueOf(dataC[0]);
                for (int i = 1; i < dataC.length; i ++) {
                    s = s + String.valueOf(dataC[i]);
                }
                return s.getBytes();
            case BOOLEAN:
                boolean[][] dataZ = (boolean[][]) data;
                for (int i = 0; i < dataZ.length; i ++) {
                    size += dataZ[i].length;
                }
                byte[] retZ = new byte[size];
                for (int i = 0, idx = 0; i < dataZ.length; i ++) {
                    for (int j = 0; j < dataZ[i].length; j ++, idx ++) {
                        retZ[idx] = (byte) (dataZ[i][j] ? 1 : 0);
                    }
                }
                return retZ;
            case SHORT:
                short[][] dataS = (short[][]) data;
                for (int i = 0; i < dataS.length; i ++) {
                    size += dataS[i].length;
                }
                byte[] retS = new byte[size * 2];
                for (int i = 0, idx = 0; i < dataS.length; i ++) {
                    for (int j = 0; j < dataS[i].length; j ++, idx += 2) {
                        retS[idx] = (byte) (dataS[i][j] >>> 8);
                        retS[idx + 1] = (byte) (dataS[i][j]);
                    }
                }
                return retS;
            case INT:
                int[][] dataI = (int[][]) data;
                for (int i = 0; i < dataI.length; i ++) {
                    size += dataI[i].length;
                }
                byte[] retI = new byte[size * 4];
                for (int i = 0, idx = 0; i < dataI.length; i ++) {
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
                for (int i = 0; i < dataL.length; i ++) {
                    size += dataL[i].length;
                }
                byte[] retL = new byte[size * 8];
                for (int i = 0, idx = 0; i < dataL.length; i ++) {
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
                for (int i = 0; i < dataF.length; i ++) {
                    size += dataF[i].length;
                }
                byte[] retF = new byte[size * 4];
                for (int i = 0, idx = 0; i < dataF.length; i ++) {
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
                for (int i = 0; i < dataD.length; i ++) {
                    size += dataD[i].length;
                }
                byte[] retD = new byte[size * 8];
                for (int i = 0, idx = 0; i < dataD.length; i ++) {
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
                for (int i = 0; i < dataB.length; i ++) {
                    size += dataB[i].length;
                }
                byte[] retB = new byte[size];
                for (int i = 0, idx = 0; i < dataB.length; i ++) {
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


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
}


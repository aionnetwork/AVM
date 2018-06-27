package com.example.deployAndRunTest;

public class ABIEncoder {
    public static byte[] encodeByte(byte data) {
        return new byte[]{data};
    }


    public static byte[] encodeBoolean(boolean data) {
        return new byte[]{(byte) (data? 1 : 0)};
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

}


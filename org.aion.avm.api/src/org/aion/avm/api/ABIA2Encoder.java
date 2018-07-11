package org.aion.avm.api;

public class ABIA2Encoder {
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
        return null;
    }

    public static byte[] encodeBoolean(boolean data) {
        return null;
    }

    public static byte[] encodeChar(char data) {
        return null;
    }

    public static byte[] encodeShort(short data) {
        return null;
    }

    public static byte[] encodeInt(int data) {
        return null;
    }

    public static byte[] encodeLong(long data) {
        return null;
    }

    public static byte[] encodeFloat(float data) {
        return null;
    }

    public static byte[] encodeDouble(double data) {
        return null;
    }

    public static byte[] encode1DArray(Object data, ABITypes type) {
        return null;
    }

    public static byte[] encode2DArray(Object data, ABITypes type) {
        return null;
    }

    public static byte[] encodeArguments(String methodName, ABITypes[] argumentTypes, int[] dimensions, int numberOfArguments, Object... arguments){
        return null;
    }

    public static byte[] encodeMethodArguments(String methodAPI, Object... arguments) {
        return null;
    }
}

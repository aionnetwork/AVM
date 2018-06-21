package org.aion.avm.core.testWallet;


/**
 * Helpers for manipulating byte[] in high-level ways.
 */
public class ByteArrayHelpers {
    public static byte[] appendLong(byte[] array, long l) {
        // We want to avoid any special JDK dependencies, so crack the long into big-endian bytes (network order).
        byte[] longBytes = new byte[] {
                (byte) (0xff & (l >> 56)),
                (byte) (0xff & (l >> 48)),
                (byte) (0xff & (l >> 40)),
                (byte) (0xff & (l >> 32)),
                (byte) (0xff & (l >> 24)),
                (byte) (0xff & (l >> 16)),
                (byte) (0xff & (l >>  8)),
                (byte) (0xff & (l >>  0)),
        };
        byte[] newArray = new byte[array.length + longBytes.length];
        arraycopy(array, 0, newArray, 0, array.length);
        arraycopy(longBytes, 0, newArray, array.length, longBytes.length);
        return newArray;
    }

    public static byte[] concatenate(byte[] one, byte[] two) {
        byte[] result = new byte[one.length + two.length];
        arraycopy(one, 0, result, 0, one.length);
        arraycopy(two, 0, result, one.length, two.length);
        return result;
    }

    public static byte[] serializeInt(int i) {
        // We want to avoid any special JDK dependencies, so crack the int into big-endian bytes (network order).
        byte[] intBytes = new byte[] {
                (byte) (0xff & (i >> 24)),
                (byte) (0xff & (i >> 16)),
                (byte) (0xff & (i >>  8)),
                (byte) (0xff & (i >>  0)),
        };
        return intBytes;
    }

    public static void arraycopy(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        for (int i = 0; i < length; ++i) {
            byte elt = src[srcPos + i];
            dest[destPos + i] = elt;
        }
    }
}

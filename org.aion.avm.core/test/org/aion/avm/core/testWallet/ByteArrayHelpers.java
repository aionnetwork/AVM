package org.aion.avm.core.testWallet;

import java.nio.ByteBuffer;


/**
 * Helpers for manipulating byte[] in high-level ways.
 */
public class ByteArrayHelpers {
    public static byte[] appendLong(byte[] array, long l) {
        // Just use a ByteBuffer to serialize the long.
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(l);
        byte[] longBytes = buffer.array();
        byte[] newArray = new byte[array.length + longBytes.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        System.arraycopy(longBytes, 0, newArray, array.length, longBytes.length);
        return newArray;
    }
}

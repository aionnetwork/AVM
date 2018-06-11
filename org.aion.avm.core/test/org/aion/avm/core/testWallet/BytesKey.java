package org.aion.avm.core.testWallet;


/**
 * Wraps byte[] to make it work as a key in a map.
 */
public class BytesKey extends ByteArrayWrapper {
    public static BytesKey from(byte[] data) {
        return new BytesKey(data);
    }

    private BytesKey(byte[] data) {
        super(data);
    }
}

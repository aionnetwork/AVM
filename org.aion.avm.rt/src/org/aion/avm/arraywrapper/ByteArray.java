package org.aion.avm.arraywrapper;

public class ByteArray extends Array {

    private byte[] underlying;

    public ByteArray(byte[] underlying) {
        this.underlying = underlying;
    }
}

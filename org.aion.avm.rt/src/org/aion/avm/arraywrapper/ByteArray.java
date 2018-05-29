package org.aion.avm.arraywrapper;

public class ByteArray extends Array {

    private byte[] underlying;

    public static ByteArray initArray(int c){
        return new ByteArray(c);
    }

    public ByteArray(int c) {
        this.underlying = new byte[c];
    }

    public ByteArray(byte[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public byte get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, byte val) {
        this.underlying[idx] = val;
    }

    public byte[] getUnderlying() {
        return underlying;
    }
}

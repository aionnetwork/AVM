package org.aion.avm.arraywrapper;

public class ShortArray extends Array {

    private short[] underlying;

    public ShortArray(short[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public short get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, short val) {
        this.underlying[idx] = val;
    }
}

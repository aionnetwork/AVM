package org.aion.avm.arraywrapper;

public class LongArray extends Array {

    private long[] underlying;

    public LongArray(long[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public long get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, long val) {
        this.underlying[idx] = val;
    }
}

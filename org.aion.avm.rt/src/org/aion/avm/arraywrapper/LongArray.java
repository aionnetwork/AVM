package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;

public class LongArray extends Array {

    private long[] underlying;

    public static LongArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new LongArray(c);
    }

    public LongArray(int c) {
        this.underlying = new long[c];
    }

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

package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class LongArray extends Array {

    private long[] underlying;

    // Static factory
    public static LongArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new LongArray(c);
    }

    // Constructor for newarray
    public LongArray(int c) {
        this.underlying = new long[c];
    }

    // Constructor for internal use
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

    // Implementation of Cloneable
    public IObject clone() {
        return new LongArray(Arrays.copyOf(underlying, underlying.length));
    }
}

package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class LongArray extends Array {

    private long[] underlying;

    public static LongArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new LongArray(c);
    }

    public LongArray(int c) {
        this.underlying = new long[c];
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

    public IObject avm_clone() {
        return new LongArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        return new LongArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public LongArray(long[] underlying) {
        this.underlying = underlying;
    }

    public long[] getUnderlying() {
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        this.underlying = (long[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }
}

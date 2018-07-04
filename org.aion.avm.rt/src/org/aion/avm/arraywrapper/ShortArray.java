package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class ShortArray extends Array {

    private short[] underlying;

    public static ShortArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        return new ShortArray(c);
    }

    public ShortArray(int c) {
        this.underlying = new short[c];
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

    // Implementation of Cloneable
    public IObject clone() {
        return new ShortArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject avm_clone() {
        return new ShortArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public ShortArray(short[] underlying) {
        this.underlying = underlying;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        this.underlying = (short[]) u;
    }
}

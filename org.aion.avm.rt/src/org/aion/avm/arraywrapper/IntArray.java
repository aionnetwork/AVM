package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class IntArray extends Array {

    private int[] underlying;

    public static IntArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 32);
        return new IntArray(c);
    }

    public IntArray(int c) {
        this.underlying = new int[c];
    }

    public int length() {
        return this.underlying.length;
    }

    public int get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, int val) {
        this.underlying[idx] = val;
    }

    public IObject avm_clone() {
        return new IntArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        return new IntArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public IntArray(int[] underlying) {
        this.underlying = underlying;
    }

    public int[] getUnderlying() {
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        this.underlying = (int[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }
}

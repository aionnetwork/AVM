package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class DoubleArray extends Array {

    private double[] underlying;

    public static DoubleArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new DoubleArray(c);
    }

    public DoubleArray(int c) {
        this.underlying = new double[c];
    }

    public int length() {
        return this.underlying.length;
    }

    public double get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, double val) {
        this.underlying[idx] = val;
    }

    public IObject avm_clone() {
        return new DoubleArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        return new DoubleArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public DoubleArray(double[] underlying) {
        this.underlying = underlying;
    }

    public double[] getUnderlying() {
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        this.underlying = (double[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }
}

package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;

public class DoubleArray extends Array {

    private double[] underlying;

    public static DoubleArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new DoubleArray(c);
    }

    public DoubleArray(int c) {
        this.underlying = new double[c];
    }

    public DoubleArray(double[] underlying) {
        this.underlying = underlying;
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
}

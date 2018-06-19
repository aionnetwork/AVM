package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class DoubleArray extends Array {

    private double[] underlying;

    // Static factory
    public static DoubleArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new DoubleArray(c);
    }

    // Constructor for newarray
    public DoubleArray(int c) {
        this.underlying = new double[c];
    }

    // Constructor for internal use
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

    // Implementation of Cloneable
    public IObject clone() {
        return new DoubleArray(Arrays.copyOf(underlying, underlying.length));
    }
}

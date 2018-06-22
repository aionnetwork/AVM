package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class IntArray extends Array {

    private int[] underlying;

    // Static factory
    public static IntArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 32);
        return new IntArray(c);
    }

    // Constructor for newarray
    public IntArray(int c) {
        this.underlying = new int[c];
    }

    // Constructor for internal use
    public IntArray(int[] underlying) {
        this.underlying = underlying;
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

    // Implementation of Cloneable
    public IObject clone() {
        return new IntArray(Arrays.copyOf(underlying, underlying.length));
    }

    // Internal
    public int[] getUnderlying() {
        return underlying;
    }
}

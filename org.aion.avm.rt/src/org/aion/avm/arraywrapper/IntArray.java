package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;

public class IntArray extends Array {

    private int[] underlying;

    public static IntArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 32);
        return new IntArray(c);
    }

    public IntArray(int c) {
        this.underlying = new int[c];
    }

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
}

package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;

public class ShortArray extends Array {

    private short[] underlying;

    public static ShortArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        return new ShortArray(c);
    }

    public ShortArray(int c) {
        this.underlying = new short[c];
    }

    public ShortArray(short[] underlying) {
        this.underlying = underlying;
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
}

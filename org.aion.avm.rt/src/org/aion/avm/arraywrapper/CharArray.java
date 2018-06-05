package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;

public class CharArray extends Array {

    private char[] underlying;

    public static CharArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 16);
        return new CharArray(c);
    }

    public CharArray(int c) {
        this.underlying = new char[c];
    }

    public CharArray(char[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public char get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, char val) {
        this.underlying[idx] = val;
    }
}

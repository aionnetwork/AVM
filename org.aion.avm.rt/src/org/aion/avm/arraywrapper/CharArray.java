package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class CharArray extends Array {

    private char[] underlying;

    // Static factory
    public static CharArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 16);
        return new CharArray(c);
    }

    // Constructor for newarray
    public CharArray(int c) {
        this.underlying = new char[c];
    }

    // Constructor for internal use
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

    // Implementation of Cloneable
    public IObject clone() {
        return new CharArray(Arrays.copyOf(underlying, underlying.length));
    }

    // Internal
    public char[] getUnderlying() {
        return underlying;
    }
}

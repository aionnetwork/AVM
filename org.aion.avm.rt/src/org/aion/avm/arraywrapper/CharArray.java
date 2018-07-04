package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class CharArray extends Array {

    private char[] underlying;

    public static CharArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 16);
        return new CharArray(c);
    }

    public CharArray(int c) {
        this.underlying = new char[c];
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

    public IObject avm_clone() {
        return new CharArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        return new CharArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public CharArray(char[] underlying) {
        this.underlying = underlying;
    }

    public char[] getUnderlying() {
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        this.underlying = (char[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }
}

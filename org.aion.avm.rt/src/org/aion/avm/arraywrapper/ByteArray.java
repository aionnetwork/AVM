package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.Object;

import java.util.Arrays;

public class ByteArray extends Array {

    private byte[] underlying;

    public static ByteArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        return new ByteArray(c);
    }

    public ByteArray(int c) {
        this.underlying = new byte[c];
    }

    public int length() {
        return this.underlying.length;
    }

    public byte get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, byte val) {
        this.underlying[idx] = val;
    }

    public IObject avm_clone() {
        return new ByteArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        return new ByteArray(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        return obj instanceof ByteArray && Arrays.equals(this.underlying, ((ByteArray) obj).underlying);
    }

    @Override
    public java.lang.String toString() {
        return Arrays.toString(this.underlying);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public ByteArray(byte[] underlying) {
        this.underlying = underlying;
    }

    public byte[] getUnderlying() {
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        this.underlying = (byte[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }
}

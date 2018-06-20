package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class ByteArray extends Array {

    private byte[] underlying;

    // Static factory
    public static ByteArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        return new ByteArray(c);
    }

    // Constructor for newarray
    public ByteArray(int c) {
        this.underlying = new byte[c];
    }

    // Constructor for internal use
    public ByteArray(byte[] underlying) {
        this.underlying = underlying;
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

    public byte[] getUnderlying() {
        return underlying;
    }

    // Implementation of Cloneable
    public IObject avm_clone() {
        return new ByteArray(Arrays.copyOf(underlying, underlying.length));
    }

    // This is here because one of the array wrapping tests requires.
    public IObject clone() {
        return new ByteArray(Arrays.copyOf(underlying, underlying.length));
    }

    // internal use
    @Override
    public boolean equals(java.lang.Object obj) {
        return obj instanceof ByteArray && Arrays.equals(this.underlying, ((ByteArray) obj).underlying);
    }

    // internal use
    @Override
    public java.lang.String toString() {
        return Arrays.toString(this.underlying);
    }
}

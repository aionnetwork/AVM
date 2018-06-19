package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class ObjectArray extends Array {

    private Object[] underlying;

    // Static factory
    public static ObjectArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new ObjectArray(c);
    }

    // Constructor for anewarray
    public ObjectArray(int c) {
        this.underlying = new Object[c];
    }

    // Constructor for internal use
    public ObjectArray(Object[] underlying) {
        this.underlying = underlying;
    }

    public int length() {
        return this.underlying.length;
    }

    public Object get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, Object val) {
        this.underlying[idx] = val;
    }

    // Implementation of Cloneable
    public IObject clone() {
        return new ObjectArray(Arrays.copyOf(underlying, underlying.length));
    }
}
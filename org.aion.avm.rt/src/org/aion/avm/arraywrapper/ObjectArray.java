package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class ObjectArray extends Array {

    protected Object[] underlying;

    public static ObjectArray initArray(int c){
        IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new ObjectArray(c);
    }

    public ObjectArray(int c) {
        this.underlying = new Object[c];
    }

    public ObjectArray(){};

    public int length() {
        return this.underlying.length;
    }

    public Object get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, Object val) {
        this.underlying[idx] = val;
    }

    public IObject clone() {
        return new ObjectArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public ObjectArray(Object[] underlying) {
        this.underlying = underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        this.underlying = (Object[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }
}
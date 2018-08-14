package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;


public class ShortArray extends Array {

    private short[] underlying;

    public static ShortArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        return new ShortArray(c);
    }

    public ShortArray(int c) {
        this.underlying = new short[c];
    }

    // Deserializer support.
    public ShortArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(ShortArray.class, deserializer);
        
        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        this.underlying = new short[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = deserializer.readShort();
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(ShortArray.class, serializer);
        
        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeShort(this.underlying[i]);
        }
    }

    public int length() {
        lazyLoad();
        return this.underlying.length;
    }

    public short get(int idx) {
        lazyLoad();
        return this.underlying[idx];
    }

    public void set(int idx, short val) {
        lazyLoad();
        this.underlying[idx] = val;
    }

    // Implementation of Cloneable
    public IObject clone() {
        lazyLoad();
        return new ShortArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject avm_clone() {
        lazyLoad();
        return new ShortArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public ShortArray(short[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public short[] getUnderlying() {
        lazyLoad();
        return underlying;
    }

    public java.lang.Object getUnderlyingAsObject(){
        lazyLoad();
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        lazyLoad();
        this.underlying = (short[]) u;
    }

    public java.lang.Object getAsObject(int idx){
        lazyLoad();
        return this.underlying[idx];
    }
}

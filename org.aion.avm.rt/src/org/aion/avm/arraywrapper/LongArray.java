package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;
import java.util.function.Consumer;


public class LongArray extends Array {

    private long[] underlying;

    public static LongArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new LongArray(c);
    }

    public LongArray(int c) {
        this.underlying = new long[c];
    }

    // Deserializer support.
    public LongArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(LongArray.class, deserializer);
        
        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        this.underlying = new long[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = deserializer.readLong();
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        super.serializeSelf(LongArray.class, serializer, nextObjectQueue);
        
        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeLong(this.underlying[i]);
        }
    }

    public int length() {
        return this.underlying.length;
    }

    public long get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, long val) {
        this.underlying[idx] = val;
    }

    public IObject avm_clone() {
        return new LongArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        return new LongArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public LongArray(long[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public long[] getUnderlying() {
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        this.underlying = (long[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }

    public java.lang.Object getAsObject(int idx){
        lazyLoad();
        return this.underlying[idx];
    }
}

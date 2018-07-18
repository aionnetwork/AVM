package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;

import java.util.Arrays;
import java.util.function.Consumer;


public class ObjectArray extends Array {

    protected Object[] underlying;

    public static ObjectArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 64);
        return new ObjectArray(c);
    }

    public ObjectArray(int c) {
        this.underlying = new Object[c];
    }

    // Deserializer support.
    public ObjectArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(ObjectArray.class, deserializer);
        
        int length = deserializer.readInt();
        this.underlying = new Object[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = deserializer.readStub();
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        super.serializeSelf(ObjectArray.class, serializer, nextObjectQueue);
        
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeStub((org.aion.avm.shadow.java.lang.Object)this.underlying[i], nextObjectQueue);
        }
    }

    public ObjectArray(){};

    public int length() {
        lazyLoad();
        return this.underlying.length;
    }

    public Object get(int idx) {
        lazyLoad();
        return this.underlying[idx];
    }

    public void set(int idx, Object val) {
        lazyLoad();
        this.underlying[idx] = val;
    }

    public IObject avm_clone() {
        lazyLoad();
        return new ObjectArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        lazyLoad();
        return new ObjectArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public ObjectArray(Object[] underlying) {
        this.underlying = underlying;
    }

    public Object[] getUnderlying() {
        lazyLoad();
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        lazyLoad();
        this.underlying = (Object[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        lazyLoad();
        return underlying;
    }

    public java.lang.Object getAsObject(int idx){
        lazyLoad();
        return this.underlying[idx];
    }
}
package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;
import java.util.function.Consumer;


public class ByteArray extends Array {

    private byte[] underlying;

    public static ByteArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 8);
        return new ByteArray(c);
    }

    public ByteArray(int c) {
        this.underlying = new byte[c];
    }

    // Deserializer support.
    public ByteArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(ByteArray.class, deserializer);
        
        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        this.underlying = new byte[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = deserializer.readByte();
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        super.serializeSelf(ByteArray.class, serializer, nextObjectQueue);
        
        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeByte(this.underlying[i]);
        }
    }

    public int length() {
        lazyLoad();
        return this.underlying.length;
    }

    public byte get(int idx) {
        lazyLoad();
        return this.underlying[idx];
    }

    public void set(int idx, byte val) {
        lazyLoad();
        this.underlying[idx] = val;
    }

    public IObject avm_clone() {
        lazyLoad();
        return new ByteArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        lazyLoad();
        return new ByteArray(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        lazyLoad();
        return obj instanceof ByteArray && Arrays.equals(this.underlying, ((ByteArray) obj).underlying);
    }

    @Override
    public java.lang.String toString() {
        lazyLoad();
        return Arrays.toString(this.underlying);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public ByteArray(byte[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public byte[] getUnderlying() {
        lazyLoad();
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        lazyLoad();
        this.underlying = (byte[]) u;
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

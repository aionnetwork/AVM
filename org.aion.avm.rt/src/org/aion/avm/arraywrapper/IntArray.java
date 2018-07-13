package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;

import java.util.Arrays;
import java.util.function.Consumer;


public class IntArray extends Array {

    private int[] underlying;

    public static IntArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 32);
        return new IntArray(c);
    }

    public IntArray(int c) {
        this.underlying = new int[c];
    }

    // Deserializer support.
    public IntArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(IntArray.class, deserializer);
        
        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        this.underlying = new int[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = deserializer.readInt();
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        super.serializeSelf(IntArray.class, serializer, nextObjectQueue);
        
        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeInt(this.underlying[i]);
        }
    }

    public int length() {
        return this.underlying.length;
    }

    public int get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, int val) {
        this.underlying[idx] = val;
    }

    public IObject avm_clone() {
        return new IntArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        return new IntArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public IntArray(int[] underlying) {
        this.underlying = underlying;
    }

    public int[] getUnderlying() {
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        this.underlying = (int[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }
}

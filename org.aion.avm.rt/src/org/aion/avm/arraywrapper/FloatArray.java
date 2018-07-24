package org.aion.avm.arraywrapper;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;

import java.util.Arrays;
import java.util.function.Consumer;


public class FloatArray extends Array {

    private float[] underlying;

    public static FloatArray initArray(int c){
        //IHelper.currentContractHelper.get().externalChargeEnergy(c * 32);
        return new FloatArray(c);
    }

    public FloatArray(int c) {
        this.underlying = new float[c];
    }

    // Deserializer support.
    public FloatArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(FloatArray.class, deserializer);
        
        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        this.underlying = new float[length];
        for (int i = 0; i < length; ++i) {
            this.underlying[i] = Float.intBitsToFloat(deserializer.readInt());
        }
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        super.serializeSelf(FloatArray.class, serializer, nextObjectQueue);
        
        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeInt(Float.floatToIntBits(this.underlying[i]));
        }
    }

    public int length() {
        return this.underlying.length;
    }

    public float get(int idx) {
        return this.underlying[idx];
    }

    public void set(int idx, float val) {
        this.underlying[idx] = val;
    }

    public IObject avm_clone() {
        return new FloatArray(Arrays.copyOf(underlying, underlying.length));
    }

    public IObject clone() {
        return new FloatArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public FloatArray(float[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public float[] getUnderlying() {
        return underlying;
    }

    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        this.underlying = (float[]) u;
    }

    public java.lang.Object getUnderlyingAsObject(){
        return underlying;
    }

    public java.lang.Object getAsObject(int idx){
        lazyLoad();
        return this.underlying[idx];
    }
}

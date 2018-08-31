package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;
import org.aion.avm.internal.IHelper;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class ObjectArray extends Array {

    protected Object[] underlying;

    /**
     * Static ObjectArray factory
     *
     * After instrumentation, NEWARRAY bytecode (with reference as type) will be replaced by a INVOKESTATIC to
     * this method.
     *
     * @param size Size of the object array
     *
     * @return New empty object array wrapper
     */
    public static ObjectArray initArray(int size){
        chargeEnergy(size * ArrayElement.REF.getEnergy());
        return new ObjectArray(size);
    }

    @Override
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

    @Override
    public IObject avm_clone() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ObjectArray_avm_clone);
        lazyLoad();
        return new ObjectArray(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new ObjectArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Internal Helper
    //========================================================

    public ObjectArray(int c) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ObjectArray_avm_constructor);
        this.underlying = new Object[c];
    }

    public ObjectArray(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ObjectArray_avm_constructor_1);
    };

    public ObjectArray(Object[] underlying) {
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public Object[] getUnderlying() {
        lazyLoad();
        return underlying;
    }

    @Override
    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        lazyLoad();
        this.underlying = (Object[]) u;
    }

    @Override
    public java.lang.Object getUnderlyingAsObject(){
        lazyLoad();
        return underlying;
    }

    @Override
    public java.lang.Object getAsObject(int idx){
        lazyLoad();
        return this.underlying[idx];
    }

    //========================================================
    // Persistent Memory Support
    //========================================================

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

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(ObjectArray.class, serializer);

        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeStub((org.aion.avm.shadow.java.lang.Object)this.underlying[i]);
        }
    }
}
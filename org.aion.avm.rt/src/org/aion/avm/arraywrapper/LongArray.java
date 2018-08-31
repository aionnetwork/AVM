package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;
import org.aion.avm.internal.IHelper;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class LongArray extends Array {

    private long[] underlying;

    /**
     * Static LongArray factory
     *
     * After instrumentation, NEWARRAY bytecode (with long as type) will be replaced by a INVOKESTATIC to
     * this method.
     *
     * @param size Size of the long array
     *
     * @return New empty long array wrapper
     */
    public static LongArray initArray(int size){
        chargeEnergy(size * ArrayElement.LONG.getEnergy());
        return new LongArray(size);
    }

    @Override
    public int length() {
        lazyLoad();
        return this.underlying.length;
    }

    public long get(int idx) {
        lazyLoad();
        return this.underlying[idx];
    }

    public void set(int idx, long val) {
        lazyLoad();
        this.underlying[idx] = val;
    }

    @Override
    public IObject avm_clone() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.LongArray_avm_clone);
        lazyLoad();
        return new LongArray(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
    public IObject clone() {
        lazyLoad();
        return new LongArray(Arrays.copyOf(underlying, underlying.length));
    }

    //========================================================
    // Internal Helper
    //========================================================

    public LongArray(int c) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.LongArray_avm_constructor);
        this.underlying = new long[c];
    }

    public LongArray(long[] underlying) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.LongArray_avm_constructor_1);
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public long[] getUnderlying() {
        lazyLoad();
        return underlying;
    }

    @Override
    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        lazyLoad();
        this.underlying = (long[]) u;
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

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(LongArray.class, serializer);

        // TODO:  We probably want faster array copies.
        serializer.writeInt(this.underlying.length);
        for (int i = 0; i < this.underlying.length; ++i) {
            serializer.writeLong(this.underlying[i]);
        }
    }
}

package org.aion.avm.arraywrapper;

import org.aion.avm.internal.*;
import java.util.Arrays;
import org.aion.avm.internal.IHelper;
import org.aion.avm.RuntimeMethodFeeSchedule;

public class ByteArray extends Array {

    private byte[] underlying;

    /**
     * Static ByteArray factory
     *
     * After instrumentation, NEWARRAY bytecode (with byte/boolean as type) will be replaced by a INVOKESTATIC to
     * this method.
     *
     * @param size Size of the byte array
     *
     * @return New empty byte array wrapper
     */
    public static ByteArray initArray(int size){
        chargeEnergy(size * ArrayElement.BYTE.getEnergy());
        return new ByteArray(size);
    }

    @Override
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

    @Override
    public IObject avm_clone() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteArray_avm_clone + 5 * length());
        lazyLoad();
        return new ByteArray(Arrays.copyOf(underlying, underlying.length));
    }

    @Override
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
    // Internal Helper
    //========================================================

    public ByteArray(int c) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteArray_avm_constructor);
        this.underlying = new byte[c];
    }

    public ByteArray(byte[] underlying) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteArray_avm_constructor_1);
        RuntimeAssertionError.assertTrue(null != underlying);
        this.underlying = underlying;
    }

    public byte[] getUnderlying() {
        lazyLoad();
        return underlying;
    }

    @Override
    public void setUnderlyingAsObject(java.lang.Object u){
        RuntimeAssertionError.assertTrue(null != u);
        lazyLoad();
        this.underlying = (byte[]) u;
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

    public ByteArray(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(ByteArray.class, deserializer);

        this.underlying = CodecIdioms.deserializeByteArray(deserializer);
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(ByteArray.class, serializer);

        CodecIdioms.serializeByteArray(serializer, this.underlying);
    }
}

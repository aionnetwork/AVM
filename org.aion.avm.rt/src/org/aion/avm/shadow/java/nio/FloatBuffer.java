package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class FloatBuffer extends Buffer<java.nio.FloatBuffer> implements Comparable<FloatBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static FloatBuffer avm_allocate(int capacity) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_allocate);
        FloatArray array = FloatArray.initArray(capacity);
        java.nio.FloatBuffer buffer = java.nio.FloatBuffer.wrap(array.getUnderlying());
        return new FloatBuffer(buffer, array, null, null);
    }

    public static FloatBuffer avm_wrap(FloatArray array, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_wrap);
        java.nio.FloatBuffer buffer = java.nio.FloatBuffer.wrap(array.getUnderlying(), offset, length);
        return new FloatBuffer(buffer, array, null, null);
    }

    public static FloatBuffer avm_wrap(FloatArray array){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_wrap_1);
        java.nio.FloatBuffer buffer = java.nio.FloatBuffer.wrap(array.getUnderlying());
        return new FloatBuffer(buffer, array, null, null);
    }

    public FloatBuffer avm_slice(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_slice);
        lazyLoad();
        return new FloatBuffer(v.slice(), this.floatArray, this.byteArray, this.byteArrayOrder);
    }

    public FloatBuffer avm_duplicate(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_duplicate);
        lazyLoad();
        return new FloatBuffer(v.duplicate(), this.floatArray, this.byteArray, this.byteArrayOrder);
    }

    public FloatBuffer avm_asReadOnlyBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_asReadOnlyBuffer);
        lazyLoad();
        return new FloatBuffer(this.v.asReadOnlyBuffer(), this.floatArray, this.byteArray, this.byteArrayOrder);
    }

    public double avm_get(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_get);
        lazyLoad();
        return this.v.get();
    };

    public FloatBuffer avm_put(float b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_put);
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public double avm_get(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_get_1);
        lazyLoad();
        return this.v.get(index);
    }

    public FloatBuffer avm_put(int index, float b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_put_1);
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public FloatBuffer avm_get(FloatArray dst, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_get_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * length);
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_get(FloatArray dst){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_get_3 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * dst.length());
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public FloatBuffer avm_put(FloatBuffer src) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_put_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * src.avm_remaining());
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_put_3 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * length);
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_put_4 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * dst.length());
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_hasArray);
        lazyLoad();
        return v.hasArray();
    }

    public FloatArray avm_array(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_array);
        lazyLoad();
        // If we can make the underlying call, return the array wrapper we already have (otherwise, it will throw).
        this.v.array();
        return this.floatArray;
    }

    public int avm_arrayOffset(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_arrayOffset);
        lazyLoad();
        return v.arrayOffset();
    }

    public final FloatBuffer avm_position(int newPosition) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_position);
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final FloatBuffer avm_limit(int newLimit) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_limit);
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final FloatBuffer avm_mark() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_mark);
        lazyLoad();
        this.lastMark = this.v.position();
        v = v.mark();
        return this;
    }

    public final FloatBuffer avm_reset() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_reset);
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final FloatBuffer avm_clear() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_clear);
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final FloatBuffer avm_flip() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_flip);
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final FloatBuffer avm_rewind() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_rewind);
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public FloatBuffer avm_compact(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_compact);
        lazyLoad();
        this.v = this.v.compact();
        return this;
    }

    public boolean avm_isDirect() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_isDirect);
        lazyLoad();
        return v.isDirect();
    }

    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_hashCode);
        lazyLoad();
        return v.hashCode();
    }

    public boolean avm_equals(IObject ob) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_equals + Math.max(avm_limit() - avm_position(), 0));
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof FloatBuffer)) {
            return false;
        }
        FloatBuffer that = (FloatBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(FloatBuffer that) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_compareTo + Math.max(avm_limit() - avm_position(), 0));
        lazyLoad();
        return this.v.compareTo(that.v);
    }

    public String avm_toString(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_toString);
        lazyLoad();
        return new String(v.toString());
    }

    public final ByteOrder avm_order(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_order);
        lazyLoad();
        return ByteOrder.lookupForConstant(this.v.order());
    }

    public boolean avm_isReadOnly(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.FloatBuffer_avm_isReadOnly);
        lazyLoad();
        return v.isReadOnly();
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private FloatArray floatArray;
    private ByteArray byteArray;
    private ByteOrder byteArrayOrder;
    private int lastMark;
    FloatBuffer(java.nio.FloatBuffer underlying, FloatArray floatArray, ByteArray byteArray, ByteOrder byteArrayOrder) {
        super(java.nio.FloatBuffer.class, underlying);
        this.floatArray = floatArray;
        this.byteArray = byteArray;
        this.byteArrayOrder = byteArrayOrder;
        this.lastMark = -1;
    }

    // Deserializer support.
    public FloatBuffer(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(FloatBuffer.class, deserializer);
        this.forCasting = java.nio.FloatBuffer.class;
        
        // Deserialize both arrays to figure out how to construct this buffer.
        this.floatArray = (FloatArray)deserializer.readStub();
        this.byteArray = (ByteArray)deserializer.readStub();
        this.byteArrayOrder = (ByteOrder)deserializer.readStub();
        ByteBuffer byteBuffer = null;
        if (null != this.byteArray) {
            byteBuffer = ByteBuffer.avm_wrap(this.byteArray);
            byteBuffer.avm_order(this.byteArrayOrder);
        }
        // TODO:  We need to verify exactly which parts of state are copied when doing asFloatBuffer on a ByteBuffer to make sure we don't need more state here.
        java.nio.FloatBuffer buffer = null;
        if (null != this.floatArray) {
            buffer = java.nio.FloatBuffer.wrap(this.floatArray.getUnderlying());
        } else {
            buffer = byteBuffer.getUnderlying().asFloatBuffer();
        }
        
        // Then, we deserialize the data we need to configure the underlying instance state.
        int position = deserializer.readInt();
        int limit = deserializer.readInt();
        int mark = deserializer.readInt();
        boolean isReadOnly = 0x0 != deserializer.readByte();
        
        // Configure and store the buffer.
        if (-1 != mark) {
            buffer.position(mark);
            buffer.mark();
        }
        this.lastMark = mark;
        buffer.limit(limit);
        buffer.position(position);
        if (isReadOnly) {
            buffer.asReadOnlyBuffer();
        }
        this.v = buffer;
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(FloatBuffer.class, serializer);
        
        // First we serialize the data we were storing as instance variables.
        serializer.writeStub(this.floatArray);
        serializer.writeStub(this.byteArray);
        serializer.writeStub(this.byteArrayOrder);
        
        // Then, we serialize the data we need to configure the underlying instance state.
        serializer.writeInt(this.v.position());
        serializer.writeInt(this.v.limit());
        serializer.writeInt(this.lastMark);
        serializer.writeByte(this.v.isReadOnly() ? (byte)0x1 : (byte)0x0);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

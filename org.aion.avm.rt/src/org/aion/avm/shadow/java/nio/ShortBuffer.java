package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class ShortBuffer extends Buffer<java.nio.ShortBuffer> implements Comparable<ShortBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static ShortBuffer avm_allocate(int capacity) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_allocate);
        ShortArray array = ShortArray.initArray(capacity);
        java.nio.ShortBuffer buffer = java.nio.ShortBuffer.wrap(array.getUnderlying());
        return new ShortBuffer(buffer, array, null, null);
    }

    public static ShortBuffer avm_wrap(ShortArray array, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_wrap);
        java.nio.ShortBuffer buffer = java.nio.ShortBuffer.wrap(array.getUnderlying(), offset, length);
        return new ShortBuffer(buffer, array, null, null);
    }

    public static ShortBuffer avm_wrap(ShortArray array){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_wrap_1);
        java.nio.ShortBuffer buffer = java.nio.ShortBuffer.wrap(array.getUnderlying());
        return new ShortBuffer(buffer, array, null, null);
    }

    public ShortBuffer avm_slice(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_slice);
        lazyLoad();
        return new ShortBuffer(v.slice(), this.shortArray, this.byteArray, this.byteArrayOrder);
    }

    public ShortBuffer avm_duplicate(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_duplicate);
        lazyLoad();
        return new ShortBuffer(v.duplicate(), this.shortArray, this.byteArray, this.byteArrayOrder);
    }

    public ShortBuffer avm_asReadOnlyBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_asReadOnlyBuffer);
        lazyLoad();
        return new ShortBuffer(this.v.asReadOnlyBuffer(), this.shortArray, this.byteArray, this.byteArrayOrder);
    }

    public short avm_get(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_get);
        lazyLoad();
        return this.v.get();
    };

    public ShortBuffer avm_put(short b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_put);
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public short avm_get(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_get_1);
        lazyLoad();
        return this.v.get(index);
    }

    public ShortBuffer avm_put(int index, short b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_put_1);
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public ShortBuffer avm_get(ShortArray dst, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_get_2 + 5 * length);
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public ShortBuffer avm_get(ShortArray dst){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_get_3 + 5 * dst.length());
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public ShortBuffer avm_put(ShortBuffer src) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_put_2 + 5 * src.avm_remaining());
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public ShortBuffer avm_put(ShortArray dst, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_put_3 + 5 * length);
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public ShortBuffer avm_put(ShortArray dst){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_put_4 + 5 * dst.length());
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_hasArray);
        lazyLoad();
        return v.hasArray();
    }

    public ShortArray avm_array(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_array);
        lazyLoad();
        // If we can make the underlying call, return the array wrapper we already have (otherwise, it will throw).
        this.v.array();
        return this.shortArray;
    }

    public int avm_arrayOffset(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_arrayOffset);
        lazyLoad();
        return v.arrayOffset();
    }

    public final ShortBuffer avm_position(int newPosition) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_position);
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final ShortBuffer avm_limit(int newLimit) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_limit);
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final ShortBuffer avm_mark() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_mark);
        lazyLoad();
        this.lastMark = this.v.position();
        v = v.mark();
        return this;
    }

    public final ShortBuffer avm_reset() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_reset);
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final ShortBuffer avm_clear() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_clear);
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final ShortBuffer avm_flip() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_flip);
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final ShortBuffer avm_rewind() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_rewind);
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public ShortBuffer avm_compact(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_compact);
        lazyLoad();
        this.v = this.v.compact();
        return this;
    }

    public boolean avm_isDirect() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_isDirect);
        lazyLoad();
        return v.isDirect();
    }

    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_hashCode);
        lazyLoad();
        return v.hashCode();
    }

    public boolean avm_equals(IObject ob) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_equals + Math.max(avm_limit() - avm_position(), 0));
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof ShortBuffer)) {
            return false;
        }
        ShortBuffer that = (ShortBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(ShortBuffer that) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_compareTo + Math.max(avm_limit() - avm_position(), 0));
        lazyLoad();
        that.lazyLoad();
        return this.v.compareTo(that.v);
    }

    public String avm_toString(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_toString);
        return new String(v.toString());
    }

    public final ByteOrder avm_order(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_order);
        return ByteOrder.lookupForConstant(this.v.order());
    }

    public boolean avm_isReadOnly(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ShortBuffer_avm_isReadOnly);
        return v.isReadOnly();
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private ShortArray shortArray;
    private ByteArray byteArray;
    private ByteOrder byteArrayOrder;
    private int lastMark;
    ShortBuffer(java.nio.ShortBuffer underlying, ShortArray shortArray, ByteArray byteArray, ByteOrder byteArrayOrder) {
        super(java.nio.ShortBuffer.class, underlying);
        this.shortArray = shortArray;
        this.byteArray = byteArray;
        this.byteArrayOrder = byteArrayOrder;
        this.lastMark = -1;
    }

    // Deserializer support.
    public ShortBuffer(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(ShortBuffer.class, deserializer);
        this.forCasting = java.nio.ShortBuffer.class;
        
        // Deserialize both arrays to figure out how to construct this buffer.
        this.shortArray = (ShortArray)deserializer.readStub();
        this.byteArray = (ByteArray)deserializer.readStub();
        this.byteArrayOrder = (ByteOrder)deserializer.readStub();
        ByteBuffer byteBuffer = null;
        if (null != this.byteArray) {
            byteBuffer = ByteBuffer.avm_wrap(this.byteArray);
            byteBuffer.avm_order(this.byteArrayOrder);
        }
        // TODO:  We need to verify exactly which parts of state are copied when doing asShortBuffer on a ByteBuffer to make sure we don't need more state here.
        java.nio.ShortBuffer buffer = null;
        if (null != this.shortArray) {
            buffer = java.nio.ShortBuffer.wrap(this.shortArray.getUnderlying());
        } else {
            buffer = byteBuffer.getUnderlying().asShortBuffer();
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
        super.serializeSelf(ShortBuffer.class, serializer);
        
        // First we serialize the data we were storing as instance variables.
        serializer.writeStub(this.shortArray);
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

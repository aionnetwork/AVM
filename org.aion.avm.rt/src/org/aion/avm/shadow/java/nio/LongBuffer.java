package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;


public class LongBuffer extends Buffer<java.nio.LongBuffer> implements Comparable<LongBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static LongBuffer avm_allocate(int capacity) {
        LongArray array = LongArray.initArray(capacity);
        java.nio.LongBuffer buffer = java.nio.LongBuffer.wrap(array.getUnderlying());
        return new LongBuffer(buffer, array, null, null);
    }

    public static LongBuffer avm_wrap(LongArray array, int offset, int length){
        java.nio.LongBuffer buffer = java.nio.LongBuffer.wrap(array.getUnderlying(), offset, length);
        return new LongBuffer(buffer, array, null, null);
    }

    public static LongBuffer avm_wrap(LongArray array){
        java.nio.LongBuffer buffer = java.nio.LongBuffer.wrap(array.getUnderlying());
        return new LongBuffer(buffer, array, null, null);
    }

    public LongBuffer avm_slice(){
        lazyLoad();
        return new LongBuffer(v.slice(), this.longArray, this.byteArray, this.byteArrayOrder);
    }

    public LongBuffer avm_duplicate(){
        lazyLoad();
        return new LongBuffer(v.duplicate(), this.longArray, this.byteArray, this.byteArrayOrder);
    }

    public LongBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new LongBuffer(this.v.asReadOnlyBuffer(), this.longArray, this.byteArray, this.byteArrayOrder);
    }

    public long avm_get(){
        lazyLoad();
        return this.v.get();
    };

    public LongBuffer avm_put(long b){
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public long avm_get(int index){
        lazyLoad();
        return this.v.get(index);
    }

    public LongBuffer avm_put(int index, long b){
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public LongBuffer avm_get(LongArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public LongBuffer avm_get(LongArray dst){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public LongBuffer avm_put(LongBuffer src) {
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public LongBuffer avm_put(LongArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public LongBuffer avm_put(LongArray dst){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        lazyLoad();
        return v.hasArray();
    }

    public LongArray avm_array(){
        lazyLoad();
        // If we can make the underlying call, return the array wrapper we already have (otherwise, it will throw).
        this.v.array();
        return this.longArray;
    }

    public int avm_arrayOffset(){
        lazyLoad();
        return v.arrayOffset();
    }

    public final LongBuffer avm_position(int newPosition) {
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final LongBuffer avm_limit(int newLimit) {
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final LongBuffer avm_mark() {
        lazyLoad();
        this.lastMark = this.v.position();
        v = v.mark();
        return this;
    }

    public final LongBuffer avm_reset() {
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final LongBuffer avm_clear() {
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final LongBuffer avm_flip() {
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final LongBuffer avm_rewind() {
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public LongBuffer avm_compact(){
        lazyLoad();
        this.v = this.v.compact();
        return this;
    }

    public boolean avm_isDirect() {
        lazyLoad();
        return v.isDirect();
    }

    public int avm_hashCode() {
        lazyLoad();
        return v.hashCode();
    }

    public boolean avm_equals(IObject ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof LongBuffer)) {
            return false;
        }
        LongBuffer that = (LongBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(LongBuffer that) {
        lazyLoad();
        that.lazyLoad();
        return this.v.compareTo(that.v);
    }

    public String avm_toString(){
        lazyLoad();
        return new String(v.toString());
    }

    public final ByteOrder avm_order(){
        lazyLoad();
        return ByteOrder.lookupForConstant(this.v.order());
    }

    public boolean avm_isReadOnly(){
        lazyLoad();
        return v.isReadOnly();
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private LongArray longArray;
    private ByteArray byteArray;
    private ByteOrder byteArrayOrder;
    private int lastMark;
    LongBuffer(java.nio.LongBuffer underlying, LongArray longArray, ByteArray byteArray, ByteOrder byteArrayOrder) {
        super(java.nio.LongBuffer.class, underlying);
        this.longArray = longArray;
        this.byteArray = byteArray;
        this.byteArrayOrder = byteArrayOrder;
        this.lastMark = -1;
    }

    // Deserializer support.
    public LongBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(LongBuffer.class, deserializer);
        this.forCasting = java.nio.LongBuffer.class;
        
        // Deserialize both arrays to figure out how to construct this buffer.
        LongArray longArray = (LongArray)deserializer.readStub();
        ByteArray byteArray = (ByteArray)deserializer.readStub();
        ByteOrder byteArrayOrder = (ByteOrder)deserializer.readStub();
        ByteBuffer byteBuffer = null;
        if (null != byteArray) {
            byteBuffer = ByteBuffer.avm_wrap(byteArray);
            byteBuffer.avm_order(byteArrayOrder);
        }
        // TODO:  We need to verify exactly which parts of state are copied when doing asLongBuffer on a ByteBuffer to make sure we don't need more state here.
        java.nio.LongBuffer buffer = null;
        if (null != longArray) {
            buffer = java.nio.LongBuffer.wrap(longArray.getUnderlying());
        } else {
            buffer = byteBuffer.getUnderlying().asLongBuffer();
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
        super.serializeSelf(LongBuffer.class, serializer);
        
        // First we serialize the data we were storing as instance variables.
        serializer.writeStub(this.longArray);
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

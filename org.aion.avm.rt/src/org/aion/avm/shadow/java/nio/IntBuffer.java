package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;


public class IntBuffer extends Buffer<java.nio.IntBuffer> implements Comparable<IntBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static IntBuffer avm_allocate(int capacity) {
        IntArray array = IntArray.initArray(capacity);
        java.nio.IntBuffer buffer = java.nio.IntBuffer.wrap(array.getUnderlying());
        return new IntBuffer(buffer, array, null, null);
    }

    public static IntBuffer avm_wrap(IntArray array, int offset, int length){
        java.nio.IntBuffer buffer = java.nio.IntBuffer.wrap(array.getUnderlying(), offset, length);
        return new IntBuffer(buffer, array, null, null);
    }

    public static IntBuffer avm_wrap(IntArray array){
        java.nio.IntBuffer buffer = java.nio.IntBuffer.wrap(array.getUnderlying());
        return new IntBuffer(buffer, array, null, null);
    }

    public IntBuffer avm_slice(){
        lazyLoad();
        return new IntBuffer(v.slice(), this.intArray, this.byteArray, this.byteArrayOrder);
    }

    public IntBuffer avm_duplicate(){
        lazyLoad();
        return new IntBuffer(v.duplicate(), this.intArray, this.byteArray, this.byteArrayOrder);
    }

    public IntBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new IntBuffer(this.v.asReadOnlyBuffer(), this.intArray, this.byteArray, this.byteArrayOrder);
    }

    public int avm_get(){
        lazyLoad();
        return this.v.get();
    };

    public IntBuffer avm_put(int b){
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public int avm_get(int index){
        lazyLoad();
        return this.v.get(index);
    }

    public IntBuffer avm_put(int index, int b){
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public IntBuffer avm_get(IntArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public IntBuffer avm_get(IntArray dst){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public IntBuffer avm_put(IntBuffer src) {
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public IntBuffer avm_put(IntArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public IntBuffer avm_put(IntArray dst){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        lazyLoad();
        return v.hasArray();
    }

    public IntArray avm_array(){
        lazyLoad();
        // If we can make the underlying call, return the array wrapper we already have (otherwise, it will throw).
        this.v.array();
        return this.intArray;
    }

    public int avm_arrayOffset(){
        lazyLoad();
        return v.arrayOffset();
    }

    public final IntBuffer avm_position(int newPosition) {
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final IntBuffer avm_limit(int newLimit) {
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final IntBuffer avm_mark() {
        lazyLoad();
        this.lastMark = this.v.position();
        v = v.mark();
        return this;
    }

    public final IntBuffer avm_reset() {
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final IntBuffer avm_clear() {
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final IntBuffer avm_flip() {
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final IntBuffer avm_rewind() {
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public IntBuffer avm_compact(){
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
        if (!(ob instanceof IntBuffer)) {
            return false;
        }
        IntBuffer that = (IntBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(IntBuffer that) {
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

    private IntArray intArray;
    private ByteArray byteArray;
    private ByteOrder byteArrayOrder;
    private int lastMark;
    IntBuffer(java.nio.IntBuffer underlying, IntArray intArray, ByteArray byteArray, ByteOrder byteArrayOrder) {
        super(java.nio.IntBuffer.class, underlying);
        this.intArray = intArray;
        this.byteArray = byteArray;
        this.byteArrayOrder = byteArrayOrder;
        this.lastMark = -1;
    }

    // Deserializer support.
    public IntBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(IntBuffer.class, deserializer);
        this.forCasting = java.nio.IntBuffer.class;
        
        // Deserialize both arrays to figure out how to construct this buffer.
        IntArray intArray = (IntArray)deserializer.readStub();
        ByteArray byteArray = (ByteArray)deserializer.readStub();
        ByteOrder byteArrayOrder = (ByteOrder)deserializer.readStub();
        ByteBuffer byteBuffer = null;
        if (null != byteArray) {
            byteBuffer = ByteBuffer.avm_wrap(byteArray);
            byteBuffer.avm_order(byteArrayOrder);
        }
        // TODO:  We need to verify exactly which parts of state are copied when doing asIntBuffer on a ByteBuffer to make sure we don't need more state here.
        java.nio.IntBuffer buffer = null;
        if (null != intArray) {
            buffer = java.nio.IntBuffer.wrap(intArray.getUnderlying());
        } else {
            buffer = byteBuffer.getUnderlying().asIntBuffer();
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
        super.serializeSelf(IntBuffer.class, serializer);
        
        // First we serialize the data we were storing as instance variables.
        serializer.writeStub(this.intArray);
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

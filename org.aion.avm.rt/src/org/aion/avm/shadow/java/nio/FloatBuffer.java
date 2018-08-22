package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;


public class FloatBuffer extends Buffer<java.nio.FloatBuffer> implements Comparable<FloatBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static FloatBuffer avm_allocate(int capacity) {
        FloatArray array = FloatArray.initArray(capacity);
        java.nio.FloatBuffer buffer = java.nio.FloatBuffer.wrap(array.getUnderlying());
        return new FloatBuffer(buffer, array, null, null);
    }

    public static FloatBuffer avm_wrap(FloatArray array, int offset, int length){
        java.nio.FloatBuffer buffer = java.nio.FloatBuffer.wrap(array.getUnderlying(), offset, length);
        return new FloatBuffer(buffer, array, null, null);
    }

    public static FloatBuffer avm_wrap(FloatArray array){
        java.nio.FloatBuffer buffer = java.nio.FloatBuffer.wrap(array.getUnderlying());
        return new FloatBuffer(buffer, array, null, null);
    }

    public FloatBuffer avm_slice(){
        lazyLoad();
        return new FloatBuffer(v.slice(), this.floatArray, this.byteArray, this.byteArrayOrder);
    }

    public FloatBuffer avm_duplicate(){
        lazyLoad();
        return new FloatBuffer(v.duplicate(), this.floatArray, this.byteArray, this.byteArrayOrder);
    }

    public FloatBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new FloatBuffer(this.v.asReadOnlyBuffer(), this.floatArray, this.byteArray, this.byteArrayOrder);
    }

    public double avm_get(){
        lazyLoad();
        return this.v.get();
    };

    public FloatBuffer avm_put(float b){
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public double avm_get(int index){
        lazyLoad();
        return this.v.get(index);
    }

    public FloatBuffer avm_put(int index, float b){
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public FloatBuffer avm_get(FloatArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_get(FloatArray dst){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public FloatBuffer avm_put(FloatBuffer src) {
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        lazyLoad();
        return v.hasArray();
    }

    public FloatArray avm_array(){
        lazyLoad();
        // If we can make the underlying call, return the array wrapper we already have (otherwise, it will throw).
        this.v.array();
        return this.floatArray;
    }

    public int avm_arrayOffset(){
        lazyLoad();
        return v.arrayOffset();
    }

    public final FloatBuffer avm_position(int newPosition) {
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final FloatBuffer avm_limit(int newLimit) {
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final FloatBuffer avm_mark() {
        lazyLoad();
        this.lastMark = this.v.position();
        v = v.mark();
        return this;
    }

    public final FloatBuffer avm_reset() {
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final FloatBuffer avm_clear() {
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final FloatBuffer avm_flip() {
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final FloatBuffer avm_rewind() {
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public FloatBuffer avm_compact(){
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
        if (!(ob instanceof FloatBuffer)) {
            return false;
        }
        FloatBuffer that = (FloatBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(FloatBuffer that) {
        lazyLoad();
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
    public FloatBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(FloatBuffer.class, deserializer);
        this.forCasting = java.nio.FloatBuffer.class;
        
        // Deserialize both arrays to figure out how to construct this buffer.
        FloatArray floatArray = (FloatArray)deserializer.readStub();
        ByteArray byteArray = (ByteArray)deserializer.readStub();
        ByteOrder byteArrayOrder = (ByteOrder)deserializer.readStub();
        ByteBuffer byteBuffer = null;
        if (null != byteArray) {
            byteBuffer = ByteBuffer.avm_wrap(byteArray);
            byteBuffer.avm_order(byteArrayOrder);
        }
        // TODO:  We need to verify exactly which parts of state are copied when doing asFloatBuffer on a ByteBuffer to make sure we don't need more state here.
        java.nio.FloatBuffer buffer = null;
        if (null != floatArray) {
            buffer = java.nio.FloatBuffer.wrap(floatArray.getUnderlying());
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

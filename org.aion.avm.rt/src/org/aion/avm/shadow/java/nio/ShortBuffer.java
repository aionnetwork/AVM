package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;


public class ShortBuffer extends Buffer<java.nio.ShortBuffer> implements Comparable<ShortBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static ShortBuffer avm_allocate(int capacity) {
        ShortArray array = ShortArray.initArray(capacity);
        java.nio.ShortBuffer buffer = java.nio.ShortBuffer.wrap(array.getUnderlying());
        return new ShortBuffer(buffer, array, null, null);
    }

    public static ShortBuffer avm_wrap(ShortArray array, int offset, int length){
        java.nio.ShortBuffer buffer = java.nio.ShortBuffer.wrap(array.getUnderlying(), offset, length);
        return new ShortBuffer(buffer, array, null, null);
    }

    public static ShortBuffer avm_wrap(ShortArray array){
        java.nio.ShortBuffer buffer = java.nio.ShortBuffer.wrap(array.getUnderlying());
        return new ShortBuffer(buffer, array, null, null);
    }

    public ShortBuffer avm_slice(){
        lazyLoad();
        return new ShortBuffer(v.slice(), this.shortArray, this.byteArray, this.byteArrayOrder);
    }

    public ShortBuffer avm_duplicate(){
        lazyLoad();
        return new ShortBuffer(v.duplicate(), this.shortArray, this.byteArray, this.byteArrayOrder);
    }

    public ShortBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new ShortBuffer(this.v.asReadOnlyBuffer(), this.shortArray, this.byteArray, this.byteArrayOrder);
    }

    public short avm_get(){
        lazyLoad();
        return this.v.get();
    };

    public ShortBuffer avm_put(short b){
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public short avm_get(int index){
        lazyLoad();
        return this.v.get(index);
    }

    public ShortBuffer avm_put(int index, short b){
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public ShortBuffer avm_get(ShortArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public ShortBuffer avm_get(ShortArray dst){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public ShortBuffer avm_put(ShortBuffer src) {
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public ShortBuffer avm_put(ShortArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public ShortBuffer avm_put(ShortArray dst){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        lazyLoad();
        return v.hasArray();
    }

    public ShortArray avm_array(){
        lazyLoad();
        // If we can make the underlying call, return the array wrapper we already have (otherwise, it will throw).
        this.v.array();
        return this.shortArray;
    }

    public int avm_arrayOffset(){
        lazyLoad();
        return v.arrayOffset();
    }

    public final ShortBuffer avm_position(int newPosition) {
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final ShortBuffer avm_limit(int newLimit) {
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final ShortBuffer avm_mark() {
        lazyLoad();
        this.lastMark = this.v.position();
        v = v.mark();
        return this;
    }

    public final ShortBuffer avm_reset() {
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final ShortBuffer avm_clear() {
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final ShortBuffer avm_flip() {
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final ShortBuffer avm_rewind() {
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public ShortBuffer avm_compact(){
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
        if (!(ob instanceof ShortBuffer)) {
            return false;
        }
        ShortBuffer that = (ShortBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(ShortBuffer that) {
        lazyLoad();
        that.lazyLoad();
        return this.v.compareTo(that.v);
    }

    public String avm_toString(){
        return new String(v.toString());
    }

    public final ByteOrder avm_order(){
        return ByteOrder.lookupForConstant(this.v.order());
    }

    public boolean avm_isReadOnly(){
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
    public ShortBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(ShortBuffer.class, deserializer);
        this.forCasting = java.nio.ShortBuffer.class;
        
        // Deserialize both arrays to figure out how to construct this buffer.
        ShortArray shortArray = (ShortArray)deserializer.readStub();
        ByteArray byteArray = (ByteArray)deserializer.readStub();
        ByteOrder byteArrayOrder = (ByteOrder)deserializer.readStub();
        ByteBuffer byteBuffer = null;
        if (null != byteArray) {
            byteBuffer = ByteBuffer.avm_wrap(byteArray);
            byteBuffer.avm_order(byteArrayOrder);
        }
        // TODO:  We need to verify exactly which parts of state are copied when doing asShortBuffer on a ByteBuffer to make sure we don't need more state here.
        java.nio.ShortBuffer buffer = null;
        if (null != shortArray) {
            buffer = java.nio.ShortBuffer.wrap(shortArray.getUnderlying());
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

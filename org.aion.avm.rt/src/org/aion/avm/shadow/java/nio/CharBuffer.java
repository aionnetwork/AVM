package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.shadow.java.lang.CharSequence;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;
import org.aion.avm.shadow.java.lang.Appendable;
import org.aion.avm.shadow.java.lang.Readable;

import java.io.IOException;


public class CharBuffer extends Buffer<java.nio.CharBuffer> implements Comparable<CharBuffer>, Appendable, CharSequence, Readable{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static CharBuffer avm_allocate(int capacity) {
        CharArray array = CharArray.initArray(capacity);
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(array.getUnderlying());
        return new CharBuffer(buffer, array, null, null, null);
    }

    public static CharBuffer avm_wrap(CharArray array, int offset, int length){
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(array.getUnderlying(), offset, length);
        return new CharBuffer(buffer, array, null, null, null);
    }

    public static CharBuffer avm_wrap(CharArray array){
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(array.getUnderlying());
        return new CharBuffer(buffer, array, null, null, null);
    }

    public int avm_read(CharBuffer target) throws IOException {
        return this.v.read(target.v);
    }

    // TODO:  Can we even use this CharSequence implementation, given that we force it through a String conversion?
    public static CharBuffer avm_wrap(CharSequence csq, int start, int end) {
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(csq.avm_toString().getUnderlying(), start, end);
        return new CharBuffer(buffer, null, null, null, csq);
    }

    public static CharBuffer avm_wrap(CharSequence csq) {
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(csq.avm_toString().getUnderlying());
        return new CharBuffer(buffer, null, null, null, csq);
    }

    public CharBuffer avm_slice(){
        lazyLoad();
        return new CharBuffer(this.v.slice(), this.charArray, this.byteArray, this.byteArrayOrder, this.sequence);
    }

    public CharBuffer avm_duplicate(){
        lazyLoad();
        return new CharBuffer(this.v.duplicate(), this.charArray, this.byteArray, this.byteArrayOrder, this.sequence);
    }

    public CharBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new CharBuffer(this.v.asReadOnlyBuffer(), this.charArray, this.byteArray, this.byteArrayOrder, this.sequence);
    }

    public char avm_get(){
        lazyLoad();
        return this.v.get();
    }

    public CharBuffer avm_put(char c){
        lazyLoad();
        this.v = this.v.put(c);
        return this;
    }

    public CharBuffer avm_put(int index, char c){
        lazyLoad();
        this.v = this.v.put(index, c);
        return this;
    }

    public CharBuffer avm_get(CharArray dst, int offset, int length) {
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public CharBuffer avm_get(CharArray dst) {
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public CharBuffer avm_put(CharBuffer src) {
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public CharBuffer avm_put(CharArray dst, int offset, int length) {
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public CharBuffer avm_put(CharArray dst) {
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public CharBuffer avm_put(String src, int start, int end) {
        lazyLoad();
        this.v = this.v.put(src.getUnderlying(), start, end);
        return this;
    }

    public CharBuffer avm_put(String src) {
        lazyLoad();
        this.v = this.v.put(src.getUnderlying());
        return this;
    }

    public final boolean avm_hasArray() {
        lazyLoad();
        return v.hasArray();
    }

    public final CharArray avm_array() {
        lazyLoad();
        // If we can make the underlying call, return the array wrapper we already have (otherwise, it will throw).
        this.v.array();
        return this.charArray;
    }

    public final int avm_arrayOffset() {
        lazyLoad();
        return v.arrayOffset();
    }

    public final CharBuffer avm_position(int newPosition) {
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final CharBuffer avm_limit(int newLimit) {
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final CharBuffer avm_mark() {
        lazyLoad();
        this.lastMark = this.v.position();
        v = v.mark();
        return this;
    }

    public final CharBuffer avm_reset() {
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final CharBuffer avm_clear() {
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final CharBuffer avm_flip() {
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final CharBuffer avm_rewind() {
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public CharBuffer avm_compact() {
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
        if (!(ob instanceof CharBuffer)) {
            return false;
        }
        CharBuffer that = (CharBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(CharBuffer that) {
        lazyLoad();
        that.lazyLoad();
        return this.v.compareTo(that.v);
    }

    public String avm_toString(){
        lazyLoad();
        return new String(v.toString());
    }

    public final int avm_length(){
        lazyLoad();
        return this.v.length();
    }

    public final char avm_charAt(int index){
        lazyLoad();
        return this.v.charAt(index);
    }

    public CharBuffer avm_subSequence(int start, int end){
        lazyLoad();
        return new CharBuffer(this.v.subSequence(start, end), this.charArray, this.byteArray, this.byteArrayOrder, this.sequence);
    }

    public CharBuffer avm_append(CharSequence csq){
        lazyLoad();
        this.v = this.v.append(csq.avm_toString().getUnderlying());
        return this;
    }

    public CharBuffer avm_append(CharSequence csq, int start, int end){
        lazyLoad();
        this.v = this.v.append(csq.avm_toString().getUnderlying(), start, end);
        return this;
    }

    public CharBuffer avm_append(char c){
        lazyLoad();
        this.v = this.v.append(c);
        return this;
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

    // We could handle this, much like the underlying JCL does, by creating multiple implementations which handle these cases differently but, for
    // now, we will treat all these cases together (mostly because they are just used to rebuild the object graph, consistently, after
    // deserialization):
    // -char[]
    // -byte[]
    // -CharSequence
    private CharArray charArray;
    private ByteArray byteArray;
    private ByteOrder byteArrayOrder;
    private CharSequence sequence;
    private int lastMark;
    CharBuffer(java.nio.CharBuffer underlying, CharArray charArray, ByteArray byteArray, ByteOrder byteArrayOrder, CharSequence sequence) {
        super(java.nio.CharBuffer.class, underlying);
        this.charArray = charArray;
        this.byteArray = byteArray;
        this.byteArrayOrder = byteArrayOrder;
        this.sequence = sequence;
        this.lastMark = -1;
    }

    // Deserializer support.
    public CharBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(CharBuffer.class, deserializer);
        this.forCasting = java.nio.CharBuffer.class;
        
        // Deserialize both arrays to figure out how to construct this buffer.
        this.charArray = (CharArray)deserializer.readStub();
        this.byteArray = (ByteArray)deserializer.readStub();
        this.byteArrayOrder = (ByteOrder)deserializer.readStub();
        this.sequence = (CharSequence)deserializer.readStub();
        ByteBuffer byteBuffer = null;
        if (null != this.byteArray) {
            byteBuffer = ByteBuffer.avm_wrap(this.byteArray);
            byteBuffer.avm_order(this.byteArrayOrder);
        }
        // TODO:  We need to verify exactly which parts of state are copied when doing asCharBuffer on a ByteBuffer to make sure we don't need more state here.
        java.nio.CharBuffer buffer = null;
        if (null != this.charArray) {
            buffer = java.nio.CharBuffer.wrap(this.charArray.getUnderlying());
        } else if (null != this.byteArray) {
            buffer = byteBuffer.getUnderlying().asCharBuffer();
        } else {
            buffer = java.nio.CharBuffer.wrap(this.sequence.avm_toString().getUnderlying());
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
        // (if this was build from a CharSequence, it is defined as read-only so this would only cause us to duplicate and discard the instance).
        if (isReadOnly && (null == this.sequence)) {
            buffer.asReadOnlyBuffer();
        }
        this.v = buffer;
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(CharBuffer.class, serializer);
        
        // First we serialize the data we were storing as instance variables.
        serializer.writeStub(this.charArray);
        serializer.writeStub(this.byteArray);
        serializer.writeStub(this.byteArrayOrder);
        serializer.writeStub((org.aion.avm.shadow.java.lang.Object)this.sequence);
        
        // Then, we serialize the data we need to configure the underlying instance state.
        serializer.writeInt(this.v.position());
        serializer.writeInt(this.v.limit());
        serializer.writeInt(this.lastMark);
        serializer.writeByte(this.v.isReadOnly() ? (byte)0x1 : (byte)0x0);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public IntStream chars()
}

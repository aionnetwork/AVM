package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.shadow.java.lang.CharSequence;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;
import org.aion.avm.shadow.java.lang.Appendable;
import org.aion.avm.shadow.java.lang.Readable;

import java.io.IOException;
import org.aion.avm.RuntimeMethodFeeSchedule;


public class CharBuffer extends Buffer<java.nio.CharBuffer> implements Comparable<CharBuffer>, Appendable, CharSequence, Readable{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static CharBuffer avm_allocate(int capacity) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_allocate);
        CharArray array = CharArray.initArray(capacity);
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(array.getUnderlying());
        return new CharBuffer(buffer, array, null, null, null);
    }

    public static CharBuffer avm_wrap(CharArray array, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_wrap);
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(array.getUnderlying(), offset, length);
        return new CharBuffer(buffer, array, null, null, null);
    }

    public static CharBuffer avm_wrap(CharArray array){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_wrap_1);
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(array.getUnderlying());
        return new CharBuffer(buffer, array, null, null, null);
    }

    public int avm_read(CharBuffer target) throws IOException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_read + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * Math.min(avm_remaining(), target.avm_remaining()));
        return this.v.read(target.v);
    }

    // TODO:  Can we even use this CharSequence implementation, given that we force it through a String conversion?
    public static CharBuffer avm_wrap(CharSequence csq, int start, int end) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_wrap_2);
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(csq.avm_toString().getUnderlying(), start, end);
        return new CharBuffer(buffer, null, null, null, csq);
    }

    public static CharBuffer avm_wrap(CharSequence csq) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_wrap_3);
        java.nio.CharBuffer buffer = java.nio.CharBuffer.wrap(csq.avm_toString().getUnderlying());
        return new CharBuffer(buffer, null, null, null, csq);
    }

    public CharBuffer avm_slice(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_slice);
        lazyLoad();
        return new CharBuffer(this.v.slice(), this.charArray, this.byteArray, this.byteArrayOrder, this.sequence);
    }

    public CharBuffer avm_duplicate(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_duplicate);
        lazyLoad();
        return new CharBuffer(this.v.duplicate(), this.charArray, this.byteArray, this.byteArrayOrder, this.sequence);
    }

    public CharBuffer avm_asReadOnlyBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_asReadOnlyBuffer);
        lazyLoad();
        return new CharBuffer(this.v.asReadOnlyBuffer(), this.charArray, this.byteArray, this.byteArrayOrder, this.sequence);
    }

    public char avm_get(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_get);
        lazyLoad();
        return this.v.get();
    }

    public CharBuffer avm_put(char c){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_put);
        lazyLoad();
        this.v = this.v.put(c);
        return this;
    }

    public CharBuffer avm_put(int index, char c){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_put_1);
        lazyLoad();
        this.v = this.v.put(index, c);
        return this;
    }

    public CharBuffer avm_get(CharArray dst, int offset, int length) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_get_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * length);
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public CharBuffer avm_get(CharArray dst) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_get_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * dst.length());
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public CharBuffer avm_put(CharBuffer src) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_put_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * src.avm_remaining());
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public CharBuffer avm_put(CharArray dst, int offset, int length) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_put_3 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * length);
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public CharBuffer avm_put(CharArray dst) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_put_4 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * dst.length());
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public CharBuffer avm_put(String src, int start, int end) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_put_5 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * Math.max(end - start, 0));
        lazyLoad();
        this.v = this.v.put(src.getUnderlying(), start, end);
        return this;
    }

    public CharBuffer avm_put(String src) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_put_6 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * src.avm_length());
        lazyLoad();
        this.v = this.v.put(src.getUnderlying());
        return this;
    }

    public final boolean avm_hasArray() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_hasArray);
        lazyLoad();
        return v.hasArray();
    }

    public final CharArray avm_array() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_array);
        lazyLoad();
        // If we can make the underlying call, return the array wrapper we already have (otherwise, it will throw).
        this.v.array();
        return this.charArray;
    }

    public final int avm_arrayOffset() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_arrayOffset);
        lazyLoad();
        return v.arrayOffset();
    }

    public final CharBuffer avm_position(int newPosition) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_position);
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final CharBuffer avm_limit(int newLimit) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_limit);
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final CharBuffer avm_mark() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_mark);
        lazyLoad();
        this.lastMark = this.v.position();
        v = v.mark();
        return this;
    }

    public final CharBuffer avm_reset() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_reset);
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final CharBuffer avm_clear() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_clear);
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final CharBuffer avm_flip() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_flip);
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final CharBuffer avm_rewind() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_rewind);
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public CharBuffer avm_compact() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_compact);
        lazyLoad();
        this.v = this.v.compact();
        return this;
    }

    public boolean avm_isDirect() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_isDirect);
        lazyLoad();
        return v.isDirect();
    }

    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_hashCode + Math.max(avm_limit() - avm_position(), 0));
        lazyLoad();
        return v.hashCode();
    }

    public boolean avm_equals(IObject ob) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_equals + Math.max(avm_limit() - avm_position(), 0));
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
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_compareTo + Math.max(avm_limit() - avm_position(), 0));
        lazyLoad();
        that.lazyLoad();
        return this.v.compareTo(that.v);
    }

    public String avm_toString(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_toString);
        lazyLoad();
        return new String(v.toString());
    }

    public final int avm_length(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_length);
        lazyLoad();
        return this.v.length();
    }

    public final char avm_charAt(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_charAt);
        lazyLoad();
        return this.v.charAt(index);
    }

    public CharBuffer avm_subSequence(int start, int end){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_subSequence + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * Math.max(end - start, 0));
        lazyLoad();
        return new CharBuffer(this.v.subSequence(start, end), this.charArray, this.byteArray, this.byteArrayOrder, this.sequence);
    }

    public CharBuffer avm_append(CharSequence csq){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_append + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * csq.avm_length());
        lazyLoad();
        this.v = this.v.append(csq.avm_toString().getUnderlying());
        return this;
    }

    public CharBuffer avm_append(CharSequence csq, int start, int end){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_append_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * Math.max(end - start, 0));
        lazyLoad();
        this.v = this.v.append(csq.avm_toString().getUnderlying(), start, end);
        return this;
    }

    public CharBuffer avm_append(char c){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_append_2);
        lazyLoad();
        this.v = this.v.append(c);
        return this;
    }

    public final ByteOrder avm_order(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_order);
        lazyLoad();
        return ByteOrder.lookupForConstant(this.v.order());
    }

    public boolean avm_isReadOnly(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.CharBuffer_avm_isReadOnly);
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
    public CharBuffer(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
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

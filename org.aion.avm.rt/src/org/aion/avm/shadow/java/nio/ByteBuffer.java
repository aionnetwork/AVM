package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.RuntimeMethodFeeSchedule;


public class ByteBuffer extends Buffer<java.nio.ByteBuffer> implements org.aion.avm.shadow.java.lang.Comparable<ByteBuffer>{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static ByteBuffer avm_allocate(int capacity) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_allocate);
        ByteArray array = ByteArray.initArray(capacity);
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(array.getUnderlying());
        return new ByteBuffer(buffer, array);
    }

    public static ByteBuffer avm_wrap(ByteArray array, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_wrap);
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(array.getUnderlying(), offset, length);
        return new ByteBuffer(buffer, array);
    }

    public static ByteBuffer avm_wrap(ByteArray array){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_wrap_1);
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(array.getUnderlying());
        return new ByteBuffer(buffer, array);
    }

    public ByteBuffer avm_slice(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_slice);
        lazyLoad();
        return new ByteBuffer(this.v.slice(), this.array);
    }

    public ByteBuffer avm_duplicate(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_duplicate);
        lazyLoad();
        return new ByteBuffer(this.v.duplicate(), this.array);
    }

    public ByteBuffer avm_asReadOnlyBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_asReadOnlyBuffer);
        lazyLoad();
        return new ByteBuffer(this.v.asReadOnlyBuffer(), this.array);
    }

    public byte avm_get(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_get);
        lazyLoad();
        return this.v.get();
    };

    public ByteBuffer avm_put(byte b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_put);
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public byte avm_get(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_get_1);
        lazyLoad();
        return this.v.get(index);
    }

    public ByteBuffer avm_put(int index, byte b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_put_1);
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public ByteBuffer avm_get(ByteArray dst, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_get_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * length);
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public ByteBuffer avm_get(ByteArray dst){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_get_3 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * dst.length());
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public ByteBuffer avm_put(ByteBuffer src) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_put_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * src.avm_remaining());
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public ByteBuffer avm_put(ByteArray dst, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_put_3 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * length);
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public ByteBuffer avm_put(ByteArray dst){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_put_4 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * dst.length());
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_hasArray);
        lazyLoad();
        return v.hasArray();
    }

    public org.aion.avm.arraywrapper.ByteArray avm_array(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_array);
        lazyLoad();
        // If we can make the underlying call, return the array wrapper we already have (otherwise, it will throw).
        this.v.array();
        return this.array;
    }

    public int avm_arrayOffset(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_arrayOffset);
        lazyLoad();
        return v.arrayOffset();
    }

    public final ByteBuffer avm_position(int newPosition) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_position);
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final ByteBuffer avm_limit(int newLimit) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_limit);
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final ByteBuffer avm_mark() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_mark);
        lazyLoad();
        this.lastMark = this.v.position();
        v = v.mark();
        return this;
    }

    public final ByteBuffer avm_reset() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_reset);
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final ByteBuffer avm_clear() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_clear);
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final ByteBuffer avm_flip() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_flip);
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final ByteBuffer avm_rewind() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_rewind);
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public ByteBuffer avm_compact(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_compact);
        lazyLoad();
        this.v = this.v.compact();
        return this;
    }

    public boolean avm_isDirect(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_isDirect);
        lazyLoad();
        return v.isDirect();
    }

    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_toString);
        lazyLoad();
        return new String(v.toString());
    }

    public int avm_hashCode(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_hashCode + Math.max(avm_limit() - avm_position(), 0));
        lazyLoad();
        return v.hashCode();
    }

    public boolean avm_equals(IObject ob) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_equals + Math.max(avm_limit() - avm_position(), 0));
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof ByteBuffer)) {
            return false;
        }
        ByteBuffer that = (ByteBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(ByteBuffer that) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_compareTo + Math.max(avm_limit() - avm_position(), 0));
        lazyLoad();
        that.lazyLoad();
        return this.v.compareTo(that.v);
    }

    public final ByteOrder avm_order(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_order);
        lazyLoad();
        return ByteOrder.lookupForConstant(this.v.order());
    }

    public final ByteBuffer avm_order(ByteOrder bo){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_order_1);
        lazyLoad();
        this.v = this.v.order(bo.getV());
        return this;
    }

    public final int avm_alignmentOffset(int index, int unitSize) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_alignmentOffset);
        lazyLoad();
        return this.v.alignmentOffset(index, unitSize);
    }

    public final ByteBuffer avm_alignedSlice(int unitSize) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_alignedSlice);
        lazyLoad();
        return new ByteBuffer(this.v.alignedSlice(unitSize), this.array);
    }

    public char avm_getChar(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getChar);
        lazyLoad();
        return this.v.getChar();
    }

    public char avm_getChar(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getChar_1);
        lazyLoad();
        return this.v.getChar(index);
    }

    public ByteBuffer avm_putChar(char value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putChar);
        lazyLoad();
        this.v = this.v.putChar(value);
        return this;
    }

    public ByteBuffer avm_putChar(int index, char value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putChar_1);
        lazyLoad();
        this.v = this.v.putChar(index, value);
        return this;
    }

    public short avm_getShort(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getShort);
        lazyLoad();
        return this.v.getShort();
    }

    public short avm_getShort(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getShort_1);
        lazyLoad();
        return this.v.getShort(index);
    }

    public ByteBuffer avm_putShort(short value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putShort);
        lazyLoad();
        this.v = this.v.putShort(value);
        return this;
    }

    public ByteBuffer avm_putShort(int index, short value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putShort_1);
        lazyLoad();
        this.v = this.v.putShort(index, value);
        return this;
    }

    public int avm_getInt(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getInt);
        lazyLoad();
        return this.v.getInt();
    }

    public int avm_getInt(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getInt_1);
        lazyLoad();
        return this.v.getInt(index);
    }

    public ByteBuffer avm_putInt(int value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putInt);
        lazyLoad();
        this.v = this.v.putInt(value);
        return this;
    }

    public ByteBuffer avm_putInt(int index, int value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putInt_1);
        lazyLoad();
        this.v = this.v.putInt(index, value);
        return this;
    }

    public long avm_getLong(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getLong);
        lazyLoad();
        return this.v.getLong();
    }

    public long avm_getLong(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getLong_1);
        lazyLoad();
        return this.v.getLong(index);
    }

    public ByteBuffer avm_putLong(long value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putLong);
        lazyLoad();
        this.v = this.v.putLong(value);
        return this;
    }

    public ByteBuffer avm_putLong(int index, long value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putLong_1);
        lazyLoad();
        this.v = this.v.putLong(index, value);
        return this;
    }

    public float avm_getFloat(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getFloat);
        lazyLoad();
        return this.v.getFloat();
    }

    public float avm_getFloat(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getFloat_1);
        lazyLoad();
        return this.v.getFloat(index);
    }

    public ByteBuffer avm_putFloat(float value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putFloat);
        lazyLoad();
        this.v = this.v.putFloat(value);
        return this;
    }

    public ByteBuffer avm_putFloat(int index, float value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putFloat_1);
        lazyLoad();
        this.v = this.v.putFloat(index, value);
        return this;
    }

    public double avm_getDouble(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getDouble);
        lazyLoad();
        return this.v.getDouble();
    }

    public double avm_getDouble(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_getDouble_1);
        lazyLoad();
        return this.v.getDouble(index);
    }

    public ByteBuffer avm_putDouble(double value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putDouble);
        lazyLoad();
        this.v = this.v.putDouble(value);
        return this;
    }

    public ByteBuffer avm_putDouble(int index, double value){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_putDouble_1);
        lazyLoad();
        this.v = this.v.putDouble(index, value);
        return this;
    }

    public CharBuffer avm_asCharBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_asCharBuffer);
        lazyLoad();
        return new CharBuffer(this.v.asCharBuffer(), null, this.array, avm_order(), null);
    }

    public ShortBuffer avm_asShortBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_asShortBuffer);
        lazyLoad();
        return new ShortBuffer(this.v.asShortBuffer(), null, this.array, avm_order());
    }

    public IntBuffer avm_asIntBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_asIntBuffer);
        lazyLoad();
        return new IntBuffer(this.v.asIntBuffer(), null, this.array, avm_order());
    }

    public LongBuffer avm_asLongBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_asLongBuffer);
        lazyLoad();
        return new LongBuffer(this.v.asLongBuffer(), null, this.array, avm_order());
    }

    public FloatBuffer avm_asFloatBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_asFloatBuffer);
        lazyLoad();
        return new FloatBuffer(this.v.asFloatBuffer(), null, this.array, avm_order());
    }

    public DoubleBuffer avm_asDoubleBuffer(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_asDoubleBuffer);
        lazyLoad();
        return new DoubleBuffer(this.v.asDoubleBuffer(), null, this.array, avm_order());
    }

    public boolean avm_isReadOnly(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.ByteBuffer_avm_isReadOnly);
        lazyLoad();
        return v.isReadOnly();
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private ByteArray array;
    private int lastMark;
    private ByteBuffer(java.nio.ByteBuffer buffer, ByteArray array) {
        super(java.nio.ByteBuffer.class, buffer);
        this.array = array;
        this.lastMark = -1;
    }

    public java.nio.ByteBuffer getUnderlying() {
        lazyLoad();
        return this.v;
    }

    // Deserializer support.
    public ByteBuffer(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(ByteBuffer.class, deserializer);
        this.forCasting = java.nio.ByteBuffer.class;
        
        // First we deserialize the data we were storing as instance variables.
        this.array = (ByteArray)deserializer.readStub();
        
        // Deserializing this will, unfortunately, force the deserialization of the underlying ByteArray, as we need to pass the underlying
        // byte[] into the JCL.
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.wrap(this.array.getUnderlying());
        
        // Then, we deserialize the data we need to configure the underlying instance state.
        int position = deserializer.readInt();
        int limit = deserializer.readInt();
        int mark = deserializer.readInt();
        ByteOrder byteOrder = (ByteOrder)deserializer.readStub();
        boolean isReadOnly = 0x0 != deserializer.readByte();
        
        // Configure and store the buffer.
        if (-1 != mark) {
            buffer.position(mark);
            buffer.mark();
        }
        this.lastMark = mark;
        buffer.limit(limit);
        buffer.position(position);
        buffer.order(byteOrder.getV());
        if (isReadOnly) {
            buffer.asReadOnlyBuffer();
        }
        this.v = buffer;
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(ByteBuffer.class, serializer);
        
        // First we serialize the data we were storing as instance variables.
        serializer.writeStub(this.array);
        
        // Then, we serialize the data we need to configure the underlying instance state.
        serializer.writeInt(this.v.position());
        serializer.writeInt(this.v.limit());
        serializer.writeInt(this.lastMark);
        serializer.writeStub(ByteOrder.lookupForConstant(this.v.order()));
        serializer.writeByte(this.v.isReadOnly() ? (byte)0x1 : (byte)0x0);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static ByteBuffer allocateDirect(int capacity)
}

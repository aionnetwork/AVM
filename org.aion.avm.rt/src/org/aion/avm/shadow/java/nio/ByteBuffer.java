package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.String;


public class ByteBuffer extends Buffer<java.nio.ByteBuffer> implements org.aion.avm.shadow.java.lang.Comparable<ByteBuffer>{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static ByteBuffer avm_allocate(int capacity) {
        return new ByteBuffer(java.nio.ByteBuffer.allocate(capacity));
    }

    public static ByteBuffer avm_wrap(ByteArray array, int offset, int length){
        return new ByteBuffer(java.nio.ByteBuffer.wrap(array.getUnderlying(), offset, length));
    }

    public static ByteBuffer avm_wrap(ByteArray array){
        return new ByteBuffer(java.nio.ByteBuffer.wrap(array.getUnderlying()));
    }

    public ByteBuffer avm_slice(){
        lazyLoad();
        return new ByteBuffer(v.slice());
    }

    public ByteBuffer avm_duplicate(){
        lazyLoad();
        return new ByteBuffer(v.duplicate());
    }

    public ByteBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new ByteBuffer(this.v.asReadOnlyBuffer());
    }

    public byte avm_get(){
        lazyLoad();
        return this.v.get();
    };

    public ByteBuffer avm_put(byte b){
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public byte avm_get(int index){
        lazyLoad();
        return this.v.get(index);
    }

    public ByteBuffer avm_put(int index, byte b){
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public ByteBuffer avm_get(ByteArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public ByteBuffer avm_get(ByteArray dst){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public ByteBuffer avm_put(ByteBuffer src) {
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public ByteBuffer avm_put(ByteArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public ByteBuffer avm_put(ByteArray dst){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        lazyLoad();
        return v.hasArray();
    }

    public org.aion.avm.arraywrapper.ByteArray avm_array(){
        lazyLoad();
        return new ByteArray((byte[])v.array());
    }

    public int avm_arrayOffset(){
        lazyLoad();
        return v.arrayOffset();
    }

    public final ByteBuffer avm_position(int newPosition) {
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final ByteBuffer avm_limit(int newLimit) {
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final ByteBuffer avm_mark() {
        lazyLoad();
        v = v.mark();
        return this;
    }

    public final ByteBuffer avm_reset() {
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final ByteBuffer avm_clear() {
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final ByteBuffer avm_flip() {
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final ByteBuffer avm_rewind() {
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public ByteBuffer avm_compact(){
        lazyLoad();
        this.v = this.v.compact();
        return this;
    }

    public boolean avm_isDirect(){
        lazyLoad();
        return v.isDirect();
    }

    public String avm_toString() {
        lazyLoad();
        return new String(v.toString());
    }

    public int avm_hashCode(){
        lazyLoad();
        return v.hashCode();
    }

    public boolean avm_equals(IObject ob) {
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
        lazyLoad();
        that.lazyLoad();
        return this.v.compareTo(that.v);
    }

    public final ByteOrder avm_order(){
        lazyLoad();
        return ByteOrder.lookupForConstant(this.v.order());
    }

    public final ByteBuffer avm_order(ByteOrder bo){
        lazyLoad();
        this.v = this.v.order(bo.getV());
        return this;
    }

    public final int avm_alignmentOffset(int index, int unitSize) {
        lazyLoad();
        return this.v.alignmentOffset(index, unitSize);
    }

    public final ByteBuffer avm_alignedSlice(int unitSize) {
        lazyLoad();
        return new ByteBuffer(this.v.alignedSlice(unitSize));
    }

    public char avm_getChar(){
        lazyLoad();
        return this.v.getChar();
    }

    public char avm_getChar(int index){
        lazyLoad();
        return this.v.getChar(index);
    }

    public ByteBuffer avm_putChar(char value){
        lazyLoad();
        this.v = this.v.putChar(value);
        return this;
    }

    public ByteBuffer avm_putChar(int index, char value){
        lazyLoad();
        this.v = this.v.putChar(index, value);
        return this;
    }

    public short avm_getShort(){
        lazyLoad();
        return this.v.getShort();
    }

    public short avm_getShort(int index){
        lazyLoad();
        return this.v.getShort(index);
    }

    public ByteBuffer avm_putShort(short value){
        lazyLoad();
        this.v = this.v.putShort(value);
        return this;
    }

    public ByteBuffer avm_putShort(int index, short value){
        lazyLoad();
        this.v = this.v.putShort(index, value);
        return this;
    }

    public int avm_getInt(){
        lazyLoad();
        return this.v.getInt();
    }

    public int avm_getInt(int index){
        lazyLoad();
        return this.v.getInt(index);
    }

    public ByteBuffer avm_putInt(int value){
        lazyLoad();
        this.v = this.v.putInt(value);
        return this;
    }

    public ByteBuffer avm_putInt(int index, int value){
        lazyLoad();
        this.v = this.v.putInt(index, value);
        return this;
    }

    public long avm_getLong(){
        lazyLoad();
        return this.v.getLong();
    }

    public long avm_getLong(int index){
        lazyLoad();
        return this.v.getLong(index);
    }

    public ByteBuffer avm_putLong(long value){
        lazyLoad();
        this.v = this.v.putLong(value);
        return this;
    }

    public ByteBuffer avm_putLong(int index, long value){
        lazyLoad();
        this.v = this.v.putLong(index, value);
        return this;
    }

    public float avm_getFloat(){
        lazyLoad();
        return this.v.getFloat();
    }

    public float avm_getFloat(int index){
        lazyLoad();
        return this.v.getFloat(index);
    }

    public ByteBuffer avm_putFloat(float value){
        lazyLoad();
        this.v = this.v.putFloat(value);
        return this;
    }

    public ByteBuffer avm_putFloat(int index, float value){
        lazyLoad();
        this.v = this.v.putFloat(index, value);
        return this;
    }

    public double avm_getDouble(){
        lazyLoad();
        return this.v.getDouble();
    }

    public double avm_getDouble(int index){
        lazyLoad();
        return this.v.getDouble(index);
    }

    public ByteBuffer avm_putDouble(double value){
        lazyLoad();
        this.v = this.v.putDouble(value);
        return this;
    }

    public ByteBuffer avm_putDouble(int index, double value){
        lazyLoad();
        this.v = this.v.putDouble(index, value);
        return this;
    }

    public CharBuffer avm_asCharBuffer(){
        lazyLoad();
        return new CharBuffer(this.v.asCharBuffer());
    }

    public ShortBuffer avm_asShortBuffer(){
        lazyLoad();
        return new ShortBuffer(this.v.asShortBuffer());
    }

    public IntBuffer avm_asIntBuffer(){
        lazyLoad();
        return new IntBuffer(this.v.asIntBuffer());
    }

    public LongBuffer avm_asLongBuffer(){
        lazyLoad();
        return new LongBuffer(this.v.asLongBuffer());
    }

    public FloatBuffer avm_asFloatBuffer(){
        lazyLoad();
        return new FloatBuffer(this.v.asFloatBuffer());
    }

    public DoubleBuffer avm_asDoubleBuffer(){
        lazyLoad();
        return new DoubleBuffer(this.v.asDoubleBuffer());
    }

    public boolean avm_isReadOnly(){
        lazyLoad();
        return v.isReadOnly();
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    ByteBuffer(java.nio.ByteBuffer underlying){
        super(java.nio.ByteBuffer.class, underlying);
    }

    // Deserializer support.
    public ByteBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(ByteBuffer.class, deserializer);
        this.forCasting = java.nio.ByteBuffer.class;
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(ByteBuffer.class, serializer);
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static ByteBuffer allocateDirect(int capacity)
}

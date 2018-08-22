package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
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
        return new ByteBuffer(v.slice());
    }

    public ByteBuffer avm_duplicate(){
        return new ByteBuffer(v.duplicate());
    }

    public ByteBuffer avm_asReadOnlyBuffer(){
        return new ByteBuffer(this.v.asReadOnlyBuffer());
    }

    public byte avm_get(){
        return this.v.get();
    };

    public ByteBuffer avm_put(byte b){
        return new ByteBuffer(this.v.put(b));
    }

    public byte avm_get(int index){
        return this.v.get(index);
    }

    public ByteBuffer avm_put(int index, byte b){
        return new ByteBuffer(this.v.put(index, b));
    }

    public ByteBuffer avm_get(ByteArray dst, int offset, int length){
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public ByteBuffer avm_get(ByteArray dst){
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public ByteBuffer avm_put(ByteBuffer src) {
        this.v = this.v.put(src.v);
        return this;
    }

    public ByteBuffer avm_put(ByteArray dst, int offset, int length){
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public ByteBuffer avm_put(ByteArray dst){
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        return v.hasArray();
    }

    public org.aion.avm.arraywrapper.ByteArray avm_array(){
        return new ByteArray((byte[])v.array());
    }

    public int avm_arrayOffset(){
        return v.arrayOffset();
    }

    public final ByteBuffer avm_position(int newPosition) {
        v = v.position(newPosition);
        return this;
    }

    public final ByteBuffer avm_limit(int newLimit) {
        v = v.limit(newLimit);
        return this;
    }

    public final ByteBuffer avm_mark() {
        v = v.mark();
        return this;
    }

    public final ByteBuffer avm_reset() {
        v = v.reset();
        return this;
    }

    public final ByteBuffer avm_clear() {
        v = v.clear();
        return this;
    }

    public final ByteBuffer avm_flip() {
        v = v.flip();
        return this;
    }

    public final ByteBuffer avm_rewind() {
        v = v.rewind();
        return this;
    }

    public ByteBuffer avm_compact(){
        this.v = this.v.compact();
        return this;
    }

    public boolean avm_isDirect(){
        return v.isDirect();
    }

    public String avm_toString() {
        return new String(v.toString());
    }

    public int avm_hashCode(){
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
        return this.v.equals(that.v);
    }

    public int avm_compareTo(ByteBuffer that) {
        return this.v.compareTo(that.v);
    }

    public final ByteOrder avm_order(){
        return ByteOrder.lookupForConstant(this.v.order());
    }

    public final ByteBuffer avm_order(ByteOrder bo){
        this.v = this.v.order(bo.getV());
        return this;
    }

    public final int avm_alignmentOffset(int index, int unitSize) {
        return this.v.alignmentOffset(index, unitSize);
    }

    public final ByteBuffer avm_alignedSlice(int unitSize) {
        return new ByteBuffer(this.v.alignedSlice(unitSize));
    }

    public char avm_getChar(){
        return this.v.getChar();
    }

    public char avm_getChar(int index){
        return this.v.getChar(index);
    }

    public ByteBuffer avm_putChar(char value){
        this.v = this.v.putChar(value);
        return this;
    }

    public ByteBuffer avm_putChar(int index, char value){
        this.v = this.v.putChar(index, value);
        return this;
    }

    public short avm_getShort(){
        return this.v.getShort();
    }

    public short avm_getShort(int index){
        return this.v.getShort(index);
    }

    public ByteBuffer avm_putShort(short value){
        this.v = this.v.putShort(value);
        return this;
    }

    public ByteBuffer avm_putShort(int index, short value){
        this.v = this.v.putShort(index, value);
        return this;
    }

    public int avm_getInt(){
        return this.v.getInt();
    }

    public int avm_getInt(int index){
        return this.v.getInt(index);
    }

    public ByteBuffer avm_putInt(int value){
        this.v = this.v.putInt(value);
        return this;
    }

    public ByteBuffer avm_putInt(int index, int value){
        this.v = this.v.putInt(index, value);
        return this;
    }

    public long avm_getLong(){
        return this.v.getLong();
    }

    public long avm_getLong(int index){
        return this.v.getLong(index);
    }

    public ByteBuffer avm_putLong(long value){
        this.v = this.v.putLong(value);
        return this;
    }

    public ByteBuffer avm_putLong(int index, long value){
        this.v = this.v.putLong(index, value);
        return this;
    }

    public float avm_getFloat(){
        return this.v.getFloat();
    }

    public float avm_getFloat(int index){
        return this.v.getFloat(index);
    }

    public ByteBuffer avm_putFloat(float value){
        this.v = this.v.putFloat(value);
        return this;
    }

    public ByteBuffer avm_putFloat(int index, float value){
        this.v = this.v.putFloat(index, value);
        return this;
    }

    public double avm_getDouble(){
        return this.v.getDouble();
    }

    public double avm_getDouble(int index){
        return this.v.getDouble(index);
    }

    public ByteBuffer avm_putDouble(double value){
        this.v = this.v.putDouble(value);
        return this;
    }

    public ByteBuffer avm_putDouble(int index, double value){
        this.v = this.v.putDouble(index, value);
        return this;
    }

    public CharBuffer avm_asCharBuffer(){
        return new CharBuffer(this.v.asCharBuffer());
    }

    public ShortBuffer avm_asShortBuffer(){
        return new ShortBuffer(this.v.asShortBuffer());
    }

    public IntBuffer avm_asIntBuffer(){
        return new IntBuffer(this.v.asIntBuffer());
    }

    public LongBuffer avm_asLongBuffer(){
        return new LongBuffer(this.v.asLongBuffer());
    }

    public FloatBuffer avm_asFloatBuffer(){
        return new FloatBuffer(this.v.asFloatBuffer());
    }

    public DoubleBuffer avm_asDoubleBuffer(){
        return new DoubleBuffer(this.v.asDoubleBuffer());
    }

    public boolean avm_isReadOnly(){
        return v.isReadOnly();
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    ByteBuffer(java.nio.ByteBuffer underlying){
        super(java.nio.ByteBuffer.class, underlying);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static ByteBuffer allocateDirect(int capacity)
}

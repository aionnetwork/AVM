package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.String;

public class ByteBuffer extends Buffer implements org.aion.avm.shadow.java.lang.Comparable<ByteBuffer>{

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
        return new ByteBuffer(((java.nio.ByteBuffer)v).asReadOnlyBuffer());
    }

    public byte avm_get(){
        return ((java.nio.ByteBuffer)v).get();
    };

    public ByteBuffer avm_put(byte b){
        return new ByteBuffer(((java.nio.ByteBuffer)v).put(b));
    }

    public byte avm_get(int index){
        return ((java.nio.ByteBuffer)v).get(index);
    }

    public ByteBuffer avm_put(int index, byte b){
        return new ByteBuffer(((java.nio.ByteBuffer)v).put(index, b));
    }

    public ByteBuffer avm_get(ByteArray dst, int offset, int length){
        v = ((java.nio.ByteBuffer)v).get(dst.getUnderlying(), offset, length);
        return this;
    }

    public ByteBuffer avm_get(ByteArray dst){
        v = ((java.nio.ByteBuffer)v).get(dst.getUnderlying());
        return this;
    }

    public ByteBuffer avm_put(ByteBuffer src) {
        v = ((java.nio.ByteBuffer)v).put((java.nio.ByteBuffer)src.v);
        return this;
    }

    public ByteBuffer avm_put(ByteArray dst, int offset, int length){
        v = ((java.nio.ByteBuffer)v).put(dst.getUnderlying(), offset, length);
        return this;
    }

    public ByteBuffer avm_put(ByteArray dst){
        v = ((java.nio.ByteBuffer)v).put(dst.getUnderlying());
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

    public ByteBuffer avm_compact(){
        v = ((java.nio.ByteBuffer)v).compact();
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
        if (this == ob)
            return true;
        if (!(ob instanceof ByteBuffer))
            return false;
        ByteBuffer that = (ByteBuffer)ob;
        return this.v.equals(that.v);
    }

    public int avm_compareTo(ByteBuffer that) {
        return ((java.nio.ByteBuffer)v).compareTo((java.nio.ByteBuffer)that.v);
    }

    public final ByteOrder avm_order(){
        return new ByteOrder(((java.nio.ByteBuffer)v).order());
    }

    public final ByteBuffer avm_order(ByteOrder bo){
        v = ((java.nio.ByteBuffer)v).order(bo.getV());
        return this;
    }

    public final int avm_alignmentOffset(int index, int unitSize) {
        return ((java.nio.ByteBuffer)v).alignmentOffset(index, unitSize);
    }

    public final ByteBuffer avm_alignedSlice(int unitSize) {
        return new ByteBuffer(((java.nio.ByteBuffer)v).alignedSlice(unitSize));
    }

    public char avm_getChar(){
        return ((java.nio.ByteBuffer)v).getChar();
    }

    public char avm_getChar(int index){
        return ((java.nio.ByteBuffer)v).getChar(index);
    }

    public ByteBuffer avm_putChar(char value){
        v = ((java.nio.ByteBuffer)v).putChar(value);
        return this;
    }

    public ByteBuffer avm_putChar(int index, char value){
        v = ((java.nio.ByteBuffer)v).putChar(index, value);
        return this;
    }

    public short avm_getShort(){
        return ((java.nio.ByteBuffer)v).getShort();
    }

    public short avm_getShort(int index){
        return ((java.nio.ByteBuffer)v).getShort(index);
    }

    public ByteBuffer avm_putShort(short value){
        v = ((java.nio.ByteBuffer)v).putShort(value);
        return this;
    }

    public ByteBuffer avm_putShort(int index, short value){
        v = ((java.nio.ByteBuffer)v).putShort(index, value);
        return this;
    }

    public int avm_getInt(){
        return ((java.nio.ByteBuffer)v).getInt();
    }

    public int avm_getInt(int index){
        return ((java.nio.ByteBuffer)v).getInt(index);
    }

    public ByteBuffer avm_putInt(int value){
        v = ((java.nio.ByteBuffer)v).putInt(value);
        return this;
    }

    public ByteBuffer avm_putInt(int index, int value){
        v = ((java.nio.ByteBuffer)v).putInt(index, value);
        return this;
    }

    public long avm_getLong(){
        return ((java.nio.ByteBuffer)v).getLong();
    }

    public long avm_getLong(int index){
        return ((java.nio.ByteBuffer)v).getLong(index);
    }

    public ByteBuffer avm_putLong(long value){
        v = ((java.nio.ByteBuffer)v).putLong(value);
        return this;
    }

    public ByteBuffer avm_putLong(int index, long value){
        v = ((java.nio.ByteBuffer)v).putLong(index, value);
        return this;
    }

    public float avm_getFloat(){
        return ((java.nio.ByteBuffer)v).getFloat();
    }

    public float avm_getFloat(int index){
        return ((java.nio.ByteBuffer)v).getFloat(index);
    }

    public ByteBuffer avm_putFloat(float value){
        v = ((java.nio.ByteBuffer)v).putFloat(value);
        return this;
    }

    public ByteBuffer avm_putFloat(int index, float value){
        v = ((java.nio.ByteBuffer)v).putFloat(index, value);
        return this;
    }

    public double avm_getDouble(){
        return ((java.nio.ByteBuffer)v).getDouble();
    }

    public double avm_getDouble(int index){
        return ((java.nio.ByteBuffer)v).getDouble(index);
    }

    public ByteBuffer avm_putDouble(double value){
        v = ((java.nio.ByteBuffer)v).putDouble(value);
        return this;
    }

    public ByteBuffer avm_putDouble(int index, double value){
        v = ((java.nio.ByteBuffer)v).putDouble(index, value);
        return this;
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    ByteBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    public boolean avm_isReadOnly(){
        return v.isReadOnly();
    }


    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static ByteBuffer allocateDirect(int capacity)

    //public abstract CharBuffer avm_asCharBuffer(){}

    //public abstract ShortBuffer asShortBuffer(){}

    //public abstract IntBuffer asIntBuffer();

    //public abstract LongBuffer asLongBuffer();

    //public abstract FloatBuffer asFloatBuffer();

    //public abstract DoubleBuffer asDoubleBuffer();
}

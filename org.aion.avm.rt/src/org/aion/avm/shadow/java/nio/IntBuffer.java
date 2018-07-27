package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;

public class IntBuffer extends Buffer implements Comparable<IntBuffer> {

    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static IntBuffer avm_allocate(int capacity) {
        return new IntBuffer(java.nio.IntBuffer.allocate(capacity));
    }

    public static IntBuffer avm_wrap(IntArray array, int offset, int length){
        return new IntBuffer(java.nio.IntBuffer.wrap(array.getUnderlying(), offset, length));
    }

    public static IntBuffer avm_wrap(IntArray array){
        return new IntBuffer(java.nio.IntBuffer.wrap(array.getUnderlying()));
    }

    public IntBuffer avm_slice(){
        return new IntBuffer(v.slice());
    }

    public IntBuffer avm_duplicate(){
        return new IntBuffer(v.duplicate());
    }

    public IntBuffer avm_asReadOnlyBuffer(){
        return new IntBuffer(((java.nio.IntBuffer)v).asReadOnlyBuffer());
    }

    public int avm_get(){
        return ((java.nio.IntBuffer)v).get();
    };

    public IntBuffer avm_put(int b){
        return new IntBuffer(((java.nio.IntBuffer)v).put(b));
    }

    public int avm_get(int index){
        return ((java.nio.IntBuffer)v).get(index);
    }

    public IntBuffer avm_put(int index, int b){
        return new IntBuffer(((java.nio.IntBuffer)v).put(index, b));
    }

    public IntBuffer avm_get(IntArray dst, int offset, int length){
        v = ((java.nio.IntBuffer)v).get(dst.getUnderlying(), offset, length);
        return this;
    }

    public IntBuffer avm_get(IntArray dst){
        v = ((java.nio.IntBuffer)v).get(dst.getUnderlying());
        return this;
    }

    public IntBuffer avm_put(IntBuffer src) {
        v = ((java.nio.IntBuffer)v).put((java.nio.IntBuffer)src.v);
        return this;
    }

    public IntBuffer avm_put(IntArray dst, int offset, int length){
        v = ((java.nio.IntBuffer)v).put(dst.getUnderlying(), offset, length);
        return this;
    }

    public IntBuffer avm_put(IntArray dst){
        v = ((java.nio.IntBuffer)v).put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        return v.hasArray();
    }

    public IntArray avm_array(){
        return new IntArray((int[])v.array());
    }

    public int avm_arrayOffset(){
        return v.arrayOffset();
    }

    public final IntBuffer avm_position(int newPosition) {
        v = v.position(newPosition);
        return this;
    }

    public final IntBuffer avm_limit(int newLimit) {
        v = v.limit(newLimit);
        return this;
    }

    public final IntBuffer avm_mark() {
        v = v.mark();
        return this;
    }

    public final IntBuffer avm_reset() {
        v = v.reset();
        return this;
    }

    public final IntBuffer avm_clear() {
        v = v.clear();
        return this;
    }

    public final IntBuffer avm_flip() {
        v = v.flip();
        return this;
    }

    public final IntBuffer avm_rewind() {
        v = v.rewind();
        return this;
    }

    public IntBuffer avm_compact(){
        v = ((java.nio.IntBuffer)v).compact();
        return this;
    }

    public boolean avm_isDirect() {
        return v.isDirect();
    }

    public int avm_hashCode() {
        return v.hashCode();
    }

    public boolean avm_equals(IObject ob) {
        if (this == ob)
            return true;
        if (!(ob instanceof IntBuffer))
            return false;
        IntBuffer that = (IntBuffer)ob;
        return this.v.equals(that.v);
    }

    public int avm_compareTo(IntBuffer that) {
        return ((java.nio.IntBuffer)v).compareTo((java.nio.IntBuffer)that.v);
    }

    public String avm_toString(){
        return new String(v.toString());
    }

    public final ByteOrder avm_order(){
        return new ByteOrder(((java.nio.IntBuffer)v).order());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    IntBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    public boolean avm_isReadOnly(){
        return v.isReadOnly();
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================
}

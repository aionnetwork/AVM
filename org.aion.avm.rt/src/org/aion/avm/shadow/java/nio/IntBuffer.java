package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;


public class IntBuffer extends Buffer<java.nio.IntBuffer> implements Comparable<IntBuffer> {
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
        lazyLoad();
        return new IntBuffer(v.slice());
    }

    public IntBuffer avm_duplicate(){
        lazyLoad();
        return new IntBuffer(v.duplicate());
    }

    public IntBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new IntBuffer(this.v.asReadOnlyBuffer());
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
        return new IntArray((int[])v.array());
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

    IntBuffer(java.nio.IntBuffer underlying){
        super(java.nio.IntBuffer.class, underlying);
    }

    // Deserializer support.
    public IntBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(IntBuffer.class, deserializer);
        this.forCasting = java.nio.IntBuffer.class;
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(IntBuffer.class, serializer);
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================
}

package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;


public class LongBuffer extends Buffer<java.nio.LongBuffer> implements Comparable<LongBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static LongBuffer avm_allocate(int capacity) {
        return new LongBuffer(java.nio.LongBuffer.allocate(capacity));
    }

    public static LongBuffer avm_wrap(LongArray array, int offset, int length){
        return new LongBuffer(java.nio.LongBuffer.wrap(array.getUnderlying(), offset, length));
    }

    public static LongBuffer avm_wrap(LongArray array){
        return new LongBuffer(java.nio.LongBuffer.wrap(array.getUnderlying()));
    }

    public LongBuffer avm_slice(){
        lazyLoad();
        return new LongBuffer(v.slice());
    }

    public LongBuffer avm_duplicate(){
        lazyLoad();
        return new LongBuffer(v.duplicate());
    }

    public LongBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new LongBuffer(this.v.asReadOnlyBuffer());
    }

    public long avm_get(){
        lazyLoad();
        return this.v.get();
    };

    public LongBuffer avm_put(long b){
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public long avm_get(int index){
        lazyLoad();
        return this.v.get(index);
    }

    public LongBuffer avm_put(int index, long b){
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public LongBuffer avm_get(LongArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public LongBuffer avm_get(LongArray dst){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public LongBuffer avm_put(LongBuffer src) {
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public LongBuffer avm_put(LongArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public LongBuffer avm_put(LongArray dst){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        lazyLoad();
        return v.hasArray();
    }

    public LongArray avm_array(){
        lazyLoad();
        return new LongArray((long[])v.array());
    }

    public int avm_arrayOffset(){
        lazyLoad();
        return v.arrayOffset();
    }

    public final LongBuffer avm_position(int newPosition) {
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final LongBuffer avm_limit(int newLimit) {
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final LongBuffer avm_mark() {
        lazyLoad();
        v = v.mark();
        return this;
    }

    public final LongBuffer avm_reset() {
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final LongBuffer avm_clear() {
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final LongBuffer avm_flip() {
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final LongBuffer avm_rewind() {
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public LongBuffer avm_compact(){
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
        if (!(ob instanceof LongBuffer)) {
            return false;
        }
        LongBuffer that = (LongBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(LongBuffer that) {
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

    LongBuffer(java.nio.LongBuffer underlying){
        super(java.nio.LongBuffer.class, underlying);
    }

    // Deserializer support.
    public LongBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(LongBuffer.class, deserializer);
        this.forCasting = java.nio.LongBuffer.class;
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(LongBuffer.class, serializer);
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

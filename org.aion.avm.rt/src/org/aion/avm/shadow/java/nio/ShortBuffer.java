package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;


public class ShortBuffer extends Buffer<java.nio.ShortBuffer> implements Comparable<ShortBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static ShortBuffer avm_allocate(int capacity) {
        return new ShortBuffer(java.nio.ShortBuffer.allocate(capacity));
    }

    public static ShortBuffer avm_wrap(ShortArray array, int offset, int length){
        return new ShortBuffer(java.nio.ShortBuffer.wrap(array.getUnderlying(), offset, length));
    }

    public static ShortBuffer avm_wrap(ShortArray array){
        return new ShortBuffer(java.nio.ShortBuffer.wrap(array.getUnderlying()));
    }

    public ShortBuffer avm_slice(){
        lazyLoad();
        return new ShortBuffer(v.slice());
    }

    public ShortBuffer avm_duplicate(){
        lazyLoad();
        return new ShortBuffer(v.duplicate());
    }

    public ShortBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new ShortBuffer(this.v.asReadOnlyBuffer());
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
        return new ShortArray((short[])v.array());
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

    ShortBuffer(java.nio.ShortBuffer underlying){
        super(java.nio.ShortBuffer.class, underlying);
    }

    // Deserializer support.
    public ShortBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(ShortBuffer.class, deserializer);
        this.forCasting = java.nio.ShortBuffer.class;
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(ShortBuffer.class, serializer);
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================
}

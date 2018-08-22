package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;


public class DoubleBuffer extends Buffer<java.nio.DoubleBuffer> implements Comparable<DoubleBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static DoubleBuffer avm_allocate(int capacity) {
        return new DoubleBuffer(java.nio.DoubleBuffer.allocate(capacity));
    }

    public static DoubleBuffer avm_wrap(DoubleArray array, int offset, int length){
        return new DoubleBuffer(java.nio.DoubleBuffer.wrap(array.getUnderlying(), offset, length));
    }

    public static DoubleBuffer avm_wrap(DoubleArray array){
        return new DoubleBuffer(java.nio.DoubleBuffer.wrap(array.getUnderlying()));
    }

    public DoubleBuffer avm_slice(){
        lazyLoad();
        return new DoubleBuffer(v.slice());
    }

    public DoubleBuffer avm_duplicate(){
        lazyLoad();
        return new DoubleBuffer(v.duplicate());
    }

    public DoubleBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new DoubleBuffer(this.v.asReadOnlyBuffer());
    }

    public double avm_get(){
        lazyLoad();
        return this.v.get();
    };

    public DoubleBuffer avm_put(double b){
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public double avm_get(int index){
        lazyLoad();
        return this.v.get(index);
    }

    public DoubleBuffer avm_put(int index, double b){
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public DoubleBuffer avm_get(DoubleArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public DoubleBuffer avm_get(DoubleArray dst){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public DoubleBuffer avm_put(DoubleBuffer src) {
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public DoubleBuffer avm_put(DoubleArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public DoubleBuffer avm_put(DoubleArray dst){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        lazyLoad();
        return v.hasArray();
    }

    public DoubleArray avm_array(){
        lazyLoad();
        return new DoubleArray((double[])v.array());
    }

    public int avm_arrayOffset(){
        lazyLoad();
        return v.arrayOffset();
    }

    public final DoubleBuffer avm_position(int newPosition) {
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final DoubleBuffer avm_limit(int newLimit) {
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final DoubleBuffer avm_mark() {
        lazyLoad();
        v = v.mark();
        return this;
    }

    public final DoubleBuffer avm_reset() {
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final DoubleBuffer avm_clear() {
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final DoubleBuffer avm_flip() {
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final DoubleBuffer avm_rewind() {
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public DoubleBuffer avm_compact(){
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
        if (!(ob instanceof DoubleBuffer)) {
            return false;
        }
        DoubleBuffer that = (DoubleBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(DoubleBuffer that) {
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

    DoubleBuffer(java.nio.DoubleBuffer underlying){
        super(java.nio.DoubleBuffer.class, underlying);
    }

    // Deserializer support.
    public DoubleBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(DoubleBuffer.class, deserializer);
        this.forCasting = java.nio.DoubleBuffer.class;
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(DoubleBuffer.class, serializer);
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;


public class FloatBuffer extends Buffer<java.nio.FloatBuffer> implements Comparable<FloatBuffer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static FloatBuffer avm_allocate(int capacity) {
        return new FloatBuffer(java.nio.FloatBuffer.allocate(capacity));
    }

    public static FloatBuffer avm_wrap(FloatArray array, int offset, int length){
        return new FloatBuffer(java.nio.FloatBuffer.wrap(array.getUnderlying(), offset, length));
    }

    public static FloatBuffer avm_wrap(FloatArray array){
        return new FloatBuffer(java.nio.FloatBuffer.wrap(array.getUnderlying()));
    }

    public FloatBuffer avm_slice(){
        lazyLoad();
        return new FloatBuffer(v.slice());
    }

    public FloatBuffer avm_duplicate(){
        lazyLoad();
        return new FloatBuffer(v.duplicate());
    }

    public FloatBuffer avm_asReadOnlyBuffer(){
        lazyLoad();
        return new FloatBuffer(this.v.asReadOnlyBuffer());
    }

    public double avm_get(){
        lazyLoad();
        return this.v.get();
    };

    public FloatBuffer avm_put(float b){
        lazyLoad();
        this.v = this.v.put(b);
        return this;
    }

    public double avm_get(int index){
        lazyLoad();
        return this.v.get(index);
    }

    public FloatBuffer avm_put(int index, float b){
        lazyLoad();
        this.v = this.v.put(index, b);
        return this;
    }

    public FloatBuffer avm_get(FloatArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_get(FloatArray dst){
        lazyLoad();
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public FloatBuffer avm_put(FloatBuffer src) {
        lazyLoad();
        this.v = this.v.put(src.v);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst, int offset, int length){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst){
        lazyLoad();
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        lazyLoad();
        return v.hasArray();
    }

    public FloatArray avm_array(){
        lazyLoad();
        return new FloatArray((float[])v.array());
    }

    public int avm_arrayOffset(){
        lazyLoad();
        return v.arrayOffset();
    }

    public final FloatBuffer avm_position(int newPosition) {
        lazyLoad();
        v = v.position(newPosition);
        return this;
    }

    public final FloatBuffer avm_limit(int newLimit) {
        lazyLoad();
        v = v.limit(newLimit);
        return this;
    }

    public final FloatBuffer avm_mark() {
        lazyLoad();
        v = v.mark();
        return this;
    }

    public final FloatBuffer avm_reset() {
        lazyLoad();
        v = v.reset();
        return this;
    }

    public final FloatBuffer avm_clear() {
        lazyLoad();
        v = v.clear();
        return this;
    }

    public final FloatBuffer avm_flip() {
        lazyLoad();
        v = v.flip();
        return this;
    }

    public final FloatBuffer avm_rewind() {
        lazyLoad();
        v = v.rewind();
        return this;
    }

    public FloatBuffer avm_compact(){
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
        if (!(ob instanceof FloatBuffer)) {
            return false;
        }
        FloatBuffer that = (FloatBuffer)ob;
        lazyLoad();
        that.lazyLoad();
        return this.v.equals(that.v);
    }

    public int avm_compareTo(FloatBuffer that) {
        lazyLoad();
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

    FloatBuffer(java.nio.FloatBuffer underlying){
        super(java.nio.FloatBuffer.class, underlying);
    }

    // Deserializer support.
    public FloatBuffer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(FloatBuffer.class, deserializer);
        this.forCasting = java.nio.FloatBuffer.class;
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(FloatBuffer.class, serializer);
        
        // TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope).
        RuntimeAssertionError.unimplemented("TODO:  Implement (this isn't yet called - just carved from a later change to reduce scope)");
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
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
        return new FloatBuffer(v.slice());
    }

    public FloatBuffer avm_duplicate(){
        return new FloatBuffer(v.duplicate());
    }

    public FloatBuffer avm_asReadOnlyBuffer(){
        return new FloatBuffer(this.v.asReadOnlyBuffer());
    }

    public double avm_get(){
        return this.v.get();
    };

    public FloatBuffer avm_put(float b){
        return new FloatBuffer(this.v.put(b));
    }

    public double avm_get(int index){
        return this.v.get(index);
    }

    public FloatBuffer avm_put(int index, float b){
        return new FloatBuffer(this.v.put(index, b));
    }

    public FloatBuffer avm_get(FloatArray dst, int offset, int length){
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_get(FloatArray dst){
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public FloatBuffer avm_put(FloatBuffer src) {
        this.v = this.v.put(src.v);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst, int offset, int length){
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst){
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        return v.hasArray();
    }

    public FloatArray avm_array(){
        return new FloatArray((float[])v.array());
    }

    public int avm_arrayOffset(){
        return v.arrayOffset();
    }

    public final FloatBuffer avm_position(int newPosition) {
        v = v.position(newPosition);
        return this;
    }

    public final FloatBuffer avm_limit(int newLimit) {
        v = v.limit(newLimit);
        return this;
    }

    public final FloatBuffer avm_mark() {
        v = v.mark();
        return this;
    }

    public final FloatBuffer avm_reset() {
        v = v.reset();
        return this;
    }

    public final FloatBuffer avm_clear() {
        v = v.clear();
        return this;
    }

    public final FloatBuffer avm_flip() {
        v = v.flip();
        return this;
    }

    public final FloatBuffer avm_rewind() {
        v = v.rewind();
        return this;
    }

    public FloatBuffer avm_compact(){
        this.v = this.v.compact();
        return this;
    }

    public boolean avm_isDirect() {
        return v.isDirect();
    }

    public int avm_hashCode() {
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
        return this.v.equals(that.v);
    }

    public int avm_compareTo(FloatBuffer that) {
        return this.v.compareTo(that.v);
    }

    public String avm_toString(){
        return new String(v.toString());
    }

    public final ByteOrder avm_order(){
        return new ByteOrder(this.v.order());
    }

    public boolean avm_isReadOnly(){
        return v.isReadOnly();
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    FloatBuffer(java.nio.FloatBuffer underlying){
        super(java.nio.FloatBuffer.class, underlying);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

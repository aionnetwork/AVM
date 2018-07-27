package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.String;

public class FloatBuffer extends Buffer {

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
        return new FloatBuffer(((java.nio.FloatBuffer)v).asReadOnlyBuffer());
    }

    public double avm_get(){
        return ((java.nio.FloatBuffer)v).get();
    };

    public FloatBuffer avm_put(float b){
        return new FloatBuffer(((java.nio.FloatBuffer)v).put(b));
    }

    public double avm_get(int index){
        return ((java.nio.FloatBuffer)v).get(index);
    }

    public FloatBuffer avm_put(int index, float b){
        return new FloatBuffer(((java.nio.FloatBuffer)v).put(index, b));
    }

    public FloatBuffer avm_get(FloatArray dst, int offset, int length){
        v = ((java.nio.FloatBuffer)v).get(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_get(FloatArray dst){
        v = ((java.nio.FloatBuffer)v).get(dst.getUnderlying());
        return this;
    }

    public FloatBuffer avm_put(FloatBuffer src) {
        v = ((java.nio.FloatBuffer)v).put((java.nio.FloatBuffer)src.v);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst, int offset, int length){
        v = ((java.nio.FloatBuffer)v).put(dst.getUnderlying(), offset, length);
        return this;
    }

    public FloatBuffer avm_put(FloatArray dst){
        v = ((java.nio.FloatBuffer)v).put(dst.getUnderlying());
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
        v = ((java.nio.FloatBuffer)v).compact();
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
        if (!(ob instanceof FloatBuffer))
            return false;
        FloatBuffer that = (FloatBuffer)ob;
        return this.v.equals(that.v);
    }

    public int avm_compareTo(FloatBuffer that) {
        return ((java.nio.FloatBuffer)v).compareTo((java.nio.FloatBuffer)that.v);
    }

    public String avm_toString(){
        return new String(v.toString());
    }

    public final ByteOrder avm_order(){
        return new ByteOrder(((java.nio.FloatBuffer)v).order());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    FloatBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    public boolean avm_isReadOnly(){
        return v.isReadOnly();
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

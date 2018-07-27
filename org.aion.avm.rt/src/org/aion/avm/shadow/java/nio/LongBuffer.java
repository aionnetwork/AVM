package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.String;

public class LongBuffer extends Buffer {

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
        return new LongBuffer(v.slice());
    }

    public LongBuffer avm_duplicate(){
        return new LongBuffer(v.duplicate());
    }

    public LongBuffer avm_asReadOnlyBuffer(){
        return new LongBuffer(((java.nio.LongBuffer)v).asReadOnlyBuffer());
    }

    public long avm_get(){
        return ((java.nio.LongBuffer)v).get();
    };

    public LongBuffer avm_put(long b){
        return new LongBuffer(((java.nio.LongBuffer)v).put(b));
    }

    public long avm_get(int index){
        return ((java.nio.LongBuffer)v).get(index);
    }

    public LongBuffer avm_put(int index, long b){
        return new LongBuffer(((java.nio.LongBuffer)v).put(index, b));
    }

    public LongBuffer avm_get(LongArray dst, int offset, int length){
        v = ((java.nio.LongBuffer)v).get(dst.getUnderlying(), offset, length);
        return this;
    }

    public LongBuffer avm_get(LongArray dst){
        v = ((java.nio.LongBuffer)v).get(dst.getUnderlying());
        return this;
    }

    public LongBuffer avm_put(LongBuffer src) {
        v = ((java.nio.LongBuffer)v).put((java.nio.LongBuffer)src.v);
        return this;
    }

    public LongBuffer avm_put(LongArray dst, int offset, int length){
        v = ((java.nio.LongBuffer)v).put(dst.getUnderlying(), offset, length);
        return this;
    }

    public LongBuffer avm_put(LongArray dst){
        v = ((java.nio.LongBuffer)v).put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        return v.hasArray();
    }

    public LongArray avm_array(){
        return new LongArray((long[])v.array());
    }

    public int avm_arrayOffset(){
        return v.arrayOffset();
    }

    public final LongBuffer avm_position(int newPosition) {
        v = v.position(newPosition);
        return this;
    }

    public final LongBuffer avm_limit(int newLimit) {
        v = v.limit(newLimit);
        return this;
    }

    public final LongBuffer avm_mark() {
        v = v.mark();
        return this;
    }

    public final LongBuffer avm_reset() {
        v = v.reset();
        return this;
    }

    public final LongBuffer avm_clear() {
        v = v.clear();
        return this;
    }

    public final LongBuffer avm_flip() {
        v = v.flip();
        return this;
    }

    public final LongBuffer avm_rewind() {
        v = v.rewind();
        return this;
    }

    public LongBuffer avm_compact(){
        v = ((java.nio.LongBuffer)v).compact();
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
        if (!(ob instanceof LongBuffer))
            return false;
        LongBuffer that = (LongBuffer)ob;
        return this.v.equals(that.v);
    }

    public int avm_compareTo(LongBuffer that) {
        return ((java.nio.LongBuffer)v).compareTo((java.nio.LongBuffer)that.v);
    }

    public String avm_toString(){
        return new String(v.toString());
    }

    public final ByteOrder avm_order(){
        return new ByteOrder(((java.nio.LongBuffer)v).order());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    LongBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    public boolean avm_isReadOnly(){
        return v.isReadOnly();
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

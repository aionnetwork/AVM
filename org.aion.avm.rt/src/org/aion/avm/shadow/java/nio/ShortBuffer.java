package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
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
        return new ShortBuffer(v.slice());
    }

    public ShortBuffer avm_duplicate(){
        return new ShortBuffer(v.duplicate());
    }

    public ShortBuffer avm_asReadOnlyBuffer(){
        return new ShortBuffer(this.v.asReadOnlyBuffer());
    }

    public short avm_get(){
        return this.v.get();
    };

    public ShortBuffer avm_put(short b){
        return new ShortBuffer(this.v.put(b));
    }

    public short avm_get(int index){
        return this.v.get(index);
    }

    public ShortBuffer avm_put(int index, short b){
        return new ShortBuffer(this.v.put(index, b));
    }

    public ShortBuffer avm_get(ShortArray dst, int offset, int length){
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public ShortBuffer avm_get(ShortArray dst){
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public ShortBuffer avm_put(ShortBuffer src) {
        this.v = this.v.put(src.v);
        return this;
    }

    public ShortBuffer avm_put(ShortArray dst, int offset, int length){
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public ShortBuffer avm_put(ShortArray dst){
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        return v.hasArray();
    }

    public ShortArray avm_array(){
        return new ShortArray((short[])v.array());
    }

    public int avm_arrayOffset(){
        return v.arrayOffset();
    }

    public final ShortBuffer avm_position(int newPosition) {
        v = v.position(newPosition);
        return this;
    }

    public final ShortBuffer avm_limit(int newLimit) {
        v = v.limit(newLimit);
        return this;
    }

    public final ShortBuffer avm_mark() {
        v = v.mark();
        return this;
    }

    public final ShortBuffer avm_reset() {
        v = v.reset();
        return this;
    }

    public final ShortBuffer avm_clear() {
        v = v.clear();
        return this;
    }

    public final ShortBuffer avm_flip() {
        v = v.flip();
        return this;
    }

    public final ShortBuffer avm_rewind() {
        v = v.rewind();
        return this;
    }

    public ShortBuffer avm_compact(){
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
        if (!(ob instanceof ShortBuffer)) {
            return false;
        }
        ShortBuffer that = (ShortBuffer)ob;
        return this.v.equals(that.v);
    }

    public int avm_compareTo(ShortBuffer that) {
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

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================
}

package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
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
        return new DoubleBuffer(v.slice());
    }

    public DoubleBuffer avm_duplicate(){
        return new DoubleBuffer(v.duplicate());
    }

    public DoubleBuffer avm_asReadOnlyBuffer(){
        return new DoubleBuffer(this.v.asReadOnlyBuffer());
    }

    public double avm_get(){
        return this.v.get();
    };

    public DoubleBuffer avm_put(double b){
        this.v = this.v.put(b);
        return this;
    }

    public double avm_get(int index){
        return this.v.get(index);
    }

    public DoubleBuffer avm_put(int index, double b){
        this.v = this.v.put(index, b);
        return this;
    }

    public DoubleBuffer avm_get(DoubleArray dst, int offset, int length){
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public DoubleBuffer avm_get(DoubleArray dst){
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public DoubleBuffer avm_put(DoubleBuffer src) {
        this.v = this.v.put(src.v);
        return this;
    }

    public DoubleBuffer avm_put(DoubleArray dst, int offset, int length){
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public DoubleBuffer avm_put(DoubleArray dst){
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public boolean avm_hasArray(){
        return v.hasArray();
    }

    public DoubleArray avm_array(){
        return new DoubleArray((double[])v.array());
    }

    public int avm_arrayOffset(){
        return v.arrayOffset();
    }

    public final DoubleBuffer avm_position(int newPosition) {
        v = v.position(newPosition);
        return this;
    }

    public final DoubleBuffer avm_limit(int newLimit) {
        v = v.limit(newLimit);
        return this;
    }

    public final DoubleBuffer avm_mark() {
        v = v.mark();
        return this;
    }

    public final DoubleBuffer avm_reset() {
        v = v.reset();
        return this;
    }

    public final DoubleBuffer avm_clear() {
        v = v.clear();
        return this;
    }

    public final DoubleBuffer avm_flip() {
        v = v.flip();
        return this;
    }

    public final DoubleBuffer avm_rewind() {
        v = v.rewind();
        return this;
    }

    public DoubleBuffer avm_compact(){
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
        if (!(ob instanceof DoubleBuffer)) {
            return false;
        }
        DoubleBuffer that = (DoubleBuffer)ob;
        return this.v.equals(that.v);
    }

    public int avm_compareTo(DoubleBuffer that) {
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

    DoubleBuffer(java.nio.DoubleBuffer underlying){
        super(java.nio.DoubleBuffer.class, underlying);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

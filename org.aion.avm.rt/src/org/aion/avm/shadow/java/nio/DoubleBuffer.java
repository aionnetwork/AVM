package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;

public class DoubleBuffer extends Buffer implements Comparable<DoubleBuffer> {

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
        return new DoubleBuffer(((java.nio.DoubleBuffer)v).asReadOnlyBuffer());
    }

    public double avm_get(){
        return ((java.nio.DoubleBuffer)v).get();
    };

    public DoubleBuffer avm_put(double b){
        return new DoubleBuffer(((java.nio.DoubleBuffer)v).put(b));
    }

    public double avm_get(int index){
        return ((java.nio.DoubleBuffer)v).get(index);
    }

    public DoubleBuffer avm_put(int index, double b){
        return new DoubleBuffer(((java.nio.DoubleBuffer)v).put(index, b));
    }

    public DoubleBuffer avm_get(DoubleArray dst, int offset, int length){
        v = ((java.nio.DoubleBuffer)v).get(dst.getUnderlying(), offset, length);
        return this;
    }

    public DoubleBuffer avm_get(DoubleArray dst){
        v = ((java.nio.DoubleBuffer)v).get(dst.getUnderlying());
        return this;
    }

    public DoubleBuffer avm_put(DoubleBuffer src) {
        v = ((java.nio.DoubleBuffer)v).put((java.nio.DoubleBuffer)src.v);
        return this;
    }

    public DoubleBuffer avm_put(DoubleArray dst, int offset, int length){
        v = ((java.nio.DoubleBuffer)v).put(dst.getUnderlying(), offset, length);
        return this;
    }

    public DoubleBuffer avm_put(DoubleArray dst){
        v = ((java.nio.DoubleBuffer)v).put(dst.getUnderlying());
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
        v = ((java.nio.DoubleBuffer)v).compact();
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
        if (!(ob instanceof DoubleBuffer))
            return false;
        DoubleBuffer that = (DoubleBuffer)ob;
        return this.v.equals(that.v);
    }

    public int avm_compareTo(DoubleBuffer that) {
        return ((java.nio.DoubleBuffer)v).compareTo((java.nio.DoubleBuffer)that.v);
    }

    public String avm_toString(){
        return new String(v.toString());
    }

    public final ByteOrder avm_order(){
        return new ByteOrder(((java.nio.DoubleBuffer)v).order());
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    DoubleBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    public boolean avm_isReadOnly(){
        return v.isReadOnly();
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

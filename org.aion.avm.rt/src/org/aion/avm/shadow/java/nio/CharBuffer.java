package org.aion.avm.shadow.java.nio;

import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.CharSequence;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Comparable;
import org.aion.avm.shadow.java.lang.Appendable;
import org.aion.avm.shadow.java.lang.Readable;

import java.io.IOException;


public class CharBuffer extends Buffer<java.nio.CharBuffer> implements Comparable<CharBuffer>, Appendable, CharSequence, Readable{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static CharBuffer avm_allocate(int capacity) {
        return new CharBuffer(java.nio.CharBuffer.allocate(capacity));
    }

    public static CharBuffer avm_wrap(CharArray array, int offset, int length){
        return new CharBuffer(java.nio.CharBuffer.wrap(array.getUnderlying(), offset, length));
    }

    public static CharBuffer avm_wrap(CharArray array){
        return new CharBuffer(java.nio.CharBuffer.wrap(array.getUnderlying()));
    }

    public int avm_read(CharBuffer target) throws IOException {
        return this.v.read(target.v);
    }

    public static CharBuffer avm_wrap(CharSequence csq, int start, int end) {
        return new CharBuffer(java.nio.CharBuffer.wrap(csq.avm_toString().getUnderlying(), start, end));
    }

    public static CharBuffer avm_wrap(CharSequence csq) {
        return new CharBuffer(java.nio.CharBuffer.wrap(csq.avm_toString().getUnderlying()));
    }

    public CharBuffer avm_slice(){
        return new CharBuffer(v.slice());
    }

    public CharBuffer avm_duplicate(){
        return new CharBuffer(v.duplicate());
    }

    public CharBuffer avm_asReadOnlyBuffer(){
        return new CharBuffer(this.v.asReadOnlyBuffer());
    }

    public char avm_get(){
        return this.v.get();
    }

    public CharBuffer avm_put(char c){
        this.v = this.v.put(c);
        return this;
    }

    public CharBuffer avm_put(int index, char c){
        this.v = this.v.put(index, c);
        return this;
    }

    public CharBuffer avm_get(CharArray dst, int offset, int length) {
        this.v = this.v.get(dst.getUnderlying(), offset, length);
        return this;
    }

    public CharBuffer avm_get(CharArray dst) {
        this.v = this.v.get(dst.getUnderlying());
        return this;
    }

    public CharBuffer avm_put(CharBuffer src) {
        this.v = this.v.put(src.v);
        return this;
    }

    public CharBuffer avm_put(CharArray dst, int offset, int length) {
        this.v = this.v.put(dst.getUnderlying(), offset, length);
        return this;
    }

    public CharBuffer avm_put(CharArray dst) {
        this.v = this.v.put(dst.getUnderlying());
        return this;
    }

    public CharBuffer avm_put(String src, int start, int end) {
        this.v = this.v.put(src.getUnderlying(), start, end);
        return this;
    }

    public CharBuffer avm_put(String src) {
        this.v = this.v.put(src.getUnderlying());
        return this;
    }

    public final boolean avm_hasArray() {
        return v.hasArray();
    }

    public final CharArray avm_array() {
        return new CharArray((char[])v.array());
    }

    public final int avm_arrayOffset() {
        return v.arrayOffset();
    }

    public final CharBuffer avm_position(int newPosition) {
        v = v.position(newPosition);
        return this;
    }

    public final CharBuffer avm_limit(int newLimit) {
        v = v.limit(newLimit);
        return this;
    }

    public final CharBuffer avm_mark() {
        v = v.mark();
        return this;
    }

    public final CharBuffer avm_reset() {
        v = v.reset();
        return this;
    }

    public final CharBuffer avm_clear() {
        v = v.clear();
        return this;
    }

    public final CharBuffer avm_flip() {
        v = v.flip();
        return this;
    }

    public final CharBuffer avm_rewind() {
        v = v.rewind();
        return this;
    }

    public CharBuffer avm_compact() {
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
        if (!(ob instanceof CharBuffer)) {
            return false;
        }
        CharBuffer that = (CharBuffer)ob;
        return this.v.equals(that.v);
    }

    public int avm_compareTo(CharBuffer that) {
        return this.v.compareTo(that.v);
    }

    public String avm_toString(){
        return new String(v.toString());
    }

    public final int avm_length(){
        return this.v.length();
    }

    public final char avm_charAt(int index){
        return this.v.charAt(index);
    }

    public CharBuffer avm_subSequence(int start, int end){
        return new CharBuffer(this.v.subSequence(start, end));
    }

    public CharBuffer avm_append(CharSequence csq){
        this.v = this.v.append(csq.avm_toString().getUnderlying());
        return this;
    }

    public CharBuffer avm_append(CharSequence csq, int start, int end){
        this.v = this.v.append(csq.avm_toString().getUnderlying(), start, end);
        return this;
    }

    public CharBuffer avm_append(char c){
        this.v = this.v.append(c);
        return this;
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

    CharBuffer(java.nio.CharBuffer underlying){
        super(java.nio.CharBuffer.class, underlying);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public IntStream chars()
}

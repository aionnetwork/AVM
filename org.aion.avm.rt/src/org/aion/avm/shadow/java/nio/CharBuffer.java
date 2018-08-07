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

public class CharBuffer extends Buffer implements Comparable<CharBuffer>, Appendable, CharSequence, Readable{

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
        return ((java.nio.CharBuffer)v).read((java.nio.CharBuffer)target.v);
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
        return new CharBuffer(((java.nio.CharBuffer)v).asReadOnlyBuffer());
    }

    public char avm_get(){
        return ((java.nio.CharBuffer)v).get();
    }

    public CharBuffer avm_put(char c){
        v = ((java.nio.CharBuffer)v).put(c);
        return this;
    }

    public CharBuffer avm_put(int index, char c){
        v = ((java.nio.CharBuffer)v).put(index, c);
        return this;
    }

    public CharBuffer avm_get(CharArray dst, int offset, int length) {
        v = ((java.nio.CharBuffer)v).get(dst.getUnderlying(), offset, length);
        return this;
    }

    public CharBuffer avm_get(CharArray dst) {
        v = ((java.nio.CharBuffer)v).get(dst.getUnderlying());
        return this;
    }

    public CharBuffer avm_put(CharBuffer src) {
        v = ((java.nio.CharBuffer)v).put((java.nio.CharBuffer)src.v);
        return this;
    }

    public CharBuffer avm_put(CharArray dst, int offset, int length) {
        v = ((java.nio.CharBuffer)v).put(dst.getUnderlying(), offset, length);
        return this;
    }

    public CharBuffer avm_put(CharArray dst) {
        v = ((java.nio.CharBuffer)v).put(dst.getUnderlying());
        return this;
    }

    public CharBuffer avm_put(String src, int start, int end) {
        v = ((java.nio.CharBuffer)v).put(src.getUnderlying(), start, end);
        return this;
    }

    public CharBuffer avm_put(String src) {
        v = ((java.nio.CharBuffer)v).put(src.getUnderlying());
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
        v = ((java.nio.CharBuffer)v).compact();
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
        if (!(ob instanceof CharBuffer))
            return false;
        CharBuffer that = (CharBuffer)ob;
        return this.v.equals(that.v);
    }

    public int avm_compareTo(CharBuffer that) {
        return ((java.nio.CharBuffer)v).compareTo((java.nio.CharBuffer)that.v);
    }

    public String avm_toString(){
        return new String(v.toString());
    }

    public final int avm_length(){
        return ((java.nio.CharBuffer)v).length();
    }

    public final char avm_charAt(int index){
        return ((java.nio.CharBuffer)v).charAt(index);
    }

    public CharBuffer avm_subSequence(int start, int end){
        return new CharBuffer(((java.nio.CharBuffer)v).subSequence(start, end));
    }

    public CharBuffer avm_append(CharSequence csq){
        v = ((java.nio.CharBuffer)v).append(csq.avm_toString().getUnderlying());
        return this;
    }

    public CharBuffer avm_append(CharSequence csq, int start, int end){
        v = ((java.nio.CharBuffer)v).append(csq.avm_toString().getUnderlying(), start, end);
        return this;
    }

    public CharBuffer avm_append(char c){
        v = ((java.nio.CharBuffer)v).append(c);
        return this;
    }

    public final ByteOrder avm_order(){
        return new ByteOrder(((java.nio.CharBuffer)v).order());
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    CharBuffer(java.nio.Buffer underlying){
        super(underlying);
    }

    public boolean avm_isReadOnly(){
        return v.isReadOnly();
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public IntStream chars()
}

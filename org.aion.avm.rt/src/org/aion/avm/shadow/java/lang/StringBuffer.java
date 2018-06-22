package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.CharArray;

public class StringBuffer {

    public StringBuffer() {
        this.underlying = new java.lang.StringBuffer();
    }

    public StringBuffer(int capacity) {
        this.underlying = new java.lang.StringBuffer(capacity);
    }

    public StringBuffer(String str) {
        this.underlying = new java.lang.StringBuffer(str.getUnderlying());
    }

    public synchronized int avm_length() {
        return this.underlying.length();
    }

    public synchronized int avm_capacity() {
        return this.underlying.capacity();
    }

    public synchronized void avm_ensureCapacity(int minimumCapacity){
        this.underlying.ensureCapacity(minimumCapacity);
    }

    public synchronized void avm_trimToSize() {
        this.underlying.trimToSize();
    }

    public synchronized void avm_setLength(int newLength) {
        this.underlying.setLength(newLength);
    }

    public synchronized char avm_charAt(int index) {
        return this.underlying.charAt(index);
    }

    public synchronized int avm_codePointAt(int index) {
        return this.underlying.codePointAt(index);
    }

    public synchronized int avm_codePointBefore(int index) {
        return this.underlying.codePointBefore(index);
    }

    public synchronized int avm_codePointCount(int beginIndex, int endIndex) {
        return this.underlying.codePointCount(beginIndex, endIndex);
    }

    public synchronized int avm_offsetByCodePoints(int index, int codePointOffset) {
        return this.underlying.offsetByCodePoints(index, codePointOffset);
    }

    public synchronized void avm_getChars(int srcBegin, int srcEnd, char[] dst,
                                      int dstBegin)
    {
        this.underlying.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public synchronized void avm_setCharAt(int index, char ch) {
        this.underlying.setCharAt(index, ch);
    }

    //TODO: IOBJECT?
    public synchronized StringBuffer avm_append(Object obj) {
        this.underlying = this.underlying.append(obj);
        return this;
    }

    public synchronized StringBuffer avm_append(String str) {
        this.underlying = this.underlying.append(str);
        return this;
    }

    public synchronized StringBuffer avm_append(StringBuffer sb) {
        this.underlying = this.underlying.append(sb.underlying);
        return this;
    }

    public synchronized StringBuffer avm_append(CharArray str) {
        this.underlying = this.underlying.append(str.getUnderlying());
        return this;
    }

    public synchronized StringBuffer avm_append(CharArray str, int offset, int len) {
        this.underlying = this.underlying.append(str.getUnderlying(), offset, len);
        return this;
    }

    public synchronized StringBuffer avm_append(boolean b) {
        this.underlying = this.underlying.append(b);
        return this;
    }

    public synchronized StringBuffer avm_append(char c) {
        this.underlying = this.underlying.append(c);
        return this;
    }

    public synchronized StringBuffer avm_append(int i) {
        this.underlying = this.underlying.append(i);
        return this;
    }

    public synchronized StringBuffer avm_appendCodePoint(int codePoint) {
        this.underlying = this.underlying.appendCodePoint(codePoint);
        return this;
    }

    public synchronized StringBuffer avm_append(long lng) {
        this.underlying = this.underlying.append(lng);
        return this;
    }

    public synchronized StringBuffer avm_append(float f) {
        this.underlying = this.underlying.append(f);
        return this;
    }

    public synchronized StringBuffer avm_append(double d) {
        this.underlying = this.underlying.append(d);
        return this;
    }

    public synchronized StringBuffer avm_delete(int start, int end) {
        this.underlying = this.underlying.delete(start, end);
        return this;
    }

    public synchronized StringBuffer avm_deleteCharAt(int index) {
        this.underlying = this.underlying.deleteCharAt(index);
        return this;
    }

    public synchronized StringBuffer avm_replace(int start, int end, String str) {
        this.underlying = this.underlying.replace(start, end, str.getUnderlying());
        return this;
    }

    public synchronized String avm_substring(int start) {
        return new String(this.underlying.substring(start));
    }

    public synchronized String avm_substring(int start, int end) {
        return new String(this.underlying.substring(start, end));
    }

    public synchronized StringBuffer avm_insert(int index, CharArray str, int offset,
                                            int len)
    {
        this.underlying.insert(index, str.getUnderlying(), offset, len);
        return this;
    }

    //TODO: IOBJECT?
    public synchronized StringBuffer avm_insert(int offset, Object obj) {
        this.underlying.insert(offset, obj);
        return this;
    }

    public synchronized StringBuffer avm_insert(int offset, String str) {
        this.underlying.insert(offset, str.getUnderlying());
        return this;
    }

    public synchronized StringBuffer avm_insert(int offset, CharArray str) {
        this.underlying.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuffer avm_insert(int offset, boolean b) {
        this.underlying.insert(offset, b);
        return this;
    }

    public synchronized StringBuffer avm_insert(int offset, char c) {
        this.underlying.insert(offset, c);
        return this;
    }

    public StringBuffer avm_insert(int offset, int i) {
        this.underlying.insert(offset, i);
        return this;
    }

    public StringBuffer avm_insert(int offset, long l) {
        this.underlying.insert(offset, l);
        return this;
    }

    public StringBuffer avm_insert(int offset, float f) {
        this.underlying.insert(offset, f);
        return this;
    }

    public StringBuffer avm_insert(int offset, double d) {
        this.underlying.insert(offset, d);
        return this;
    }

    public int avm_indexOf(String str) {
        return this.underlying.indexOf(str.getUnderlying());
    }

    public synchronized int avm_indexOf(String str, int fromIndex) {
        return this.underlying.indexOf(str.getUnderlying(), fromIndex);
    }

    public int avm_lastIndexOf(String str) {
        return this.underlying.lastIndexOf(str.getUnderlying());
    }

    public synchronized int avm_lastIndexOf(String str, int fromIndex) {
        return this.underlying.lastIndexOf(str.getUnderlying(), fromIndex);
    }

    public synchronized StringBuffer avm_reverse() {
        this.underlying.reverse();
        return this;
    }

    public synchronized String avm_toString() {
        return new String(this);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================
    private java.lang.StringBuffer underlying;

    public java.lang.StringBuffer getUnderlying() {
        return underlying;
    }


    //========================================================
    // Methods below are deprecated, we don't shadow them
    //========================================================



    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public StringBuffer(CharSequence seq)

    //public synchronized StringBuffer append(CharSequence s)

    //public synchronized StringBuffer append(CharSequence s, int start, int end)

    //public synchronized CharSequence subSequence(int start, int end)

    //public StringBuffer insert(int dstOffset, CharSequence s)

    //public synchronized StringBuffer insert(int dstOffset, CharSequence s,
    //            int start, int end)
}

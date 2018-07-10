package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.CharArray;

public class StringBuilder extends Object implements CharSequence, Appendable{

    public StringBuilder() {
        this.underlying = new java.lang.StringBuilder();
    }

    public StringBuilder(int capacity) {
        this.underlying = new java.lang.StringBuilder(capacity);
    }

    public StringBuilder(String str) {
        this.underlying = new java.lang.StringBuilder(str.getV());
    }

    public StringBuilder(CharSequence seq){
        this.underlying = new java.lang.StringBuilder(seq.avm_toString().getV());
    }

    //TODO: IOBJECT?
    public StringBuilder avm_append(Object obj) {
        this.underlying.append(obj);
        return this;
    }

    public StringBuilder avm_append(String str) {
        this.underlying.append(str.getV());
        return this;
    }

    public StringBuilder avm_append(StringBuffer sb) {
        this.underlying.append(sb.getUnderlying());
        return this;
    }

    public StringBuilder avm_append(CharArray str) {
        this.underlying.append(str.getUnderlying());
        return this;
    }

    public StringBuilder avm_append(CharArray str, int offset, int len) {
        this.underlying.append(str.getUnderlying(), offset, len);
        return this;
    }

    public StringBuilder avm_append(CharSequence s){
        this.underlying.append(s.avm_toString());
        return this;
    }

    public StringBuilder avm_append(CharSequence s, int start, int end){
        this.underlying.append(s.avm_toString().getV(), start, end);
        return this;
    }

    public StringBuilder avm_append(boolean b) {
        this.underlying.append(b);
        return this;
    }

    public StringBuilder avm_append(char c) {
        this.underlying.append(c);
        return this;
    }

    public StringBuilder avm_append(int i) {
        this.underlying.append(i);
        return this;
    }

    public StringBuilder avm_append(long lng) {
        this.underlying.append(lng);
        return this;
    }

    public StringBuilder avm_append(float f) {
        this.underlying.append(f);
        return this;
    }

    public StringBuilder avm_append(double d) {
        this.underlying.append(d);
        return this;
    }

    public StringBuilder avm_appendCodePoint(int codePoint) {
        this.underlying.appendCodePoint(codePoint);
        return this;
    }

    public StringBuilder avm_delete(int start, int end) {
        this.underlying.delete(start, end);
        return this;
    }

    public StringBuilder avm_deleteCharAt(int index) {
        this.underlying.deleteCharAt(index);
        return this;
    }

    public StringBuilder avm_replace(int start, int end, String str) {
        this.underlying = this.underlying.replace(start, end, str.getV());
        return this;
    }

    public StringBuilder avm_insert(int index, CharArray str, int offset,
                                                int len)
    {
        this.underlying.insert(index, str.getUnderlying(), offset, len);
        return this;
    }

    //TODO: IOBJECT?
    public StringBuilder avm_insert(int offset, Object obj) {
        this.underlying.insert(offset, obj);
        return this;
    }

    public StringBuilder avm_insert(int offset, String str) {
        this.underlying.insert(offset, str.getV());
        return this;
    }

    public StringBuilder avm_insert(int offset, CharArray str) {
        this.underlying.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int dstOffset, CharSequence s) {
        this.underlying.insert(dstOffset, s.avm_toString().getV());
        return this;
    }

    public StringBuilder avm_insert(int dstOffset, CharSequence s, int start, int end) {
        this.underlying.insert(dstOffset, s.avm_subSequence(start, end).avm_toString().getV());
        return this;
    }

    public StringBuilder avm_insert(int offset, boolean b) {
        this.underlying.insert(offset, b);
        return this;
    }

    public StringBuilder avm_insert(int offset, char c) {
        this.underlying.insert(offset, c);
        return this;
    }

    public StringBuilder avm_insert(int offset, int i) {
        this.underlying.insert(offset, i);
        return this;
    }

    public StringBuilder avm_insert(int offset, long l) {
        this.underlying.insert(offset, l);
        return this;
    }

    public StringBuilder avm_insert(int offset, float f) {
        this.underlying.insert(offset, f);
        return this;
    }

    public StringBuilder avm_insert(int offset, double d) {
        this.underlying.insert(offset, d);
        return this;
    }

    public int avm_indexOf(String str) {
        return this.underlying.indexOf(str.getV());
    }

    public synchronized int avm_indexOf(String str, int fromIndex) {
        return this.underlying.indexOf(str.getV(), fromIndex);
    }

    public int avm_lastIndexOf(String str) {
        return this.underlying.lastIndexOf(str.getV());
    }

    public synchronized int avm_lastIndexOf(String str, int fromIndex) {
        return this.underlying.lastIndexOf(str.getV(), fromIndex);
    }

    public synchronized StringBuilder avm_reverse() {
        this.underlying.reverse();
        return this;
    }

    public synchronized String avm_toString() {
        return new String(this);
    }

    public char avm_charAt(int index){
        return this.underlying.charAt(index);
    }

    public CharSequence avm_subSequence(int start, int end) {
        return this.avm_toString().avm_subSequence(start, end);
    }

    public int avm_length(){
        return this.avm_toString().avm_length();
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.lang.StringBuilder underlying;

    public java.lang.StringBuilder getUnderlying() {
        return underlying;
    }

    //========================================================
    // Methods below are deprecated
    //========================================================


    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

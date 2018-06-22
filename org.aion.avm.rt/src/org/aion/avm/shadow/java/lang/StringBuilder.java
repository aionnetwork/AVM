package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.CharArray;

public class StringBuilder {

    public StringBuilder() {
        this.underlying = new java.lang.StringBuilder();
    }

    public StringBuilder(int capacity) {
        this.underlying = new java.lang.StringBuilder(capacity);
    }

    public StringBuilder(String str) {
        this.underlying = new java.lang.StringBuilder(str.getUnderlying());
    }

    //TODO: IOBJECT?
    public StringBuilder avm_append(Object obj) {
        this.underlying.append(obj);
        return this;
    }

    public StringBuilder avm_append(String str) {
        this.underlying.append(str.getUnderlying());
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
        this.underlying = this.underlying.replace(start, end, str.getUnderlying());
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
        this.underlying.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int offset, CharArray str) {
        this.underlying.insert(offset, str.getUnderlying());
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

    public synchronized StringBuilder avm_reverse() {
        this.underlying.reverse();
        return this;
    }

    public synchronized String avm_toString() {
        return new String(this);
    }






    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.lang.StringBuilder underlying;

    public java.lang.StringBuilder getUnderlying() {
        return underlying;
    }

    //========================================================
    // Methods below are deprecated, we don't shadow them
    //========================================================



    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public StringBuilder(CharSequence seq)

    //public StringBuilder append(CharSequence s)

    //public StringBuilder append(CharSequence s, int start, int end)

    //public StringBuilder insert(int dstOffset, CharSequence s)

    //public StringBuilder insert(int dstOffset, CharSequence s, int start, int end)

}

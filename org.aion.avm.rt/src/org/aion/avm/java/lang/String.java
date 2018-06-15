package org.aion.avm.java.lang;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.IObject;

import java.util.Arrays;

public class String extends Object implements CharSequence {

    private java.lang.String DEFAULT_CHARSET = "UTF-8";

    private java.lang.String underlying;

    // @Internal
    public String(java.lang.String underlying) {
        this.underlying = underlying;
    }

    public String(String str) {
        this.underlying = str.underlying;
    }

    public String(ByteArray bytes) {
        this(bytes, 0, bytes.getUnderlying().length);
    }

    public String(ByteArray bytes, int offset, int length) {
        byte[] b = bytes.getUnderlying();
        if (offset < 0 || offset >= b.length || length < 0 || length > b.length || offset + length > b.length) {
            throw new java.lang.StringIndexOutOfBoundsException();
        }
        try {
            this.underlying = new java.lang.String(Arrays.copyOfRange(b, offset, offset + length), DEFAULT_CHARSET);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new java.lang.RuntimeException();
        }
    }

    public char avm_charAt(int index) {
        return this.underlying.charAt(index);
    }

    public boolean avm_contains(CharSequence s) {
        return this.underlying.indexOf(s.avm_toString().underlying) >= 0;
    }

    @Override
    public boolean avm_equals(IObject obj) {
        return obj instanceof String && this.underlying.equals(((String) obj).underlying);
    }

    public ByteArray avm_getBytes() {
        return new ByteArray(underlying.getBytes());
    }

    @Override
    public int avm_hashCode() {
        // In the case of string, we want to use the actual hashcode.
        return this.underlying.hashCode();
    }

    public int avm_indexOf(int ch) {
        return underlying.indexOf(ch);
    }

    public int avm_indexOf(String str) {
        return underlying.indexOf(str.underlying);
    }

    public boolean avm_isEmpty() {
        return underlying.isEmpty();
    }

    public int avm_length() {
        return this.underlying.length();
    }

    public String avm_replace(char oldChar, char newChar) {
        java.lang.String str = this.underlying.replace(oldChar, newChar);
        return new String(str);
    }

    public boolean avm_startsWith(String prefix) {
        return this.underlying.startsWith(prefix.underlying);
    }

    public CharSequence avm_subSequence(int start, int end) {
        return avm_substring(start, end);
    }

    public String avm_substring(int start, int end) {
        // TODO: check range
        return new String(this.underlying.substring(start, end));
    }

    public CharArray avm_toCharArray() {
        return new CharArray(this.underlying.toCharArray());
    }

    public String avm_toLowerCase() {
        return new String(this.underlying.toLowerCase());
    }

    public String avm_toUpperCase() {
        return new String(this.underlying.toUpperCase());
    }

    public String avm_trim() {
        return new String(this.underlying.trim());
    }

    public String avm_toString() {
        return this;
    }

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    @Override
    public boolean equals(java.lang.Object anObject) {
        return anObject instanceof String && this.underlying.equals(((String) anObject).underlying);
    }

    @Override
    public int hashCode() {
        // We probably want a consistent hashCode answer, for strings, since they are data-defined.
        return this.underlying.hashCode();
    }

    // NOTE:  This toString() cannot be called by the contract code (it will call avm_toString()) but our runtime and test code can call this.
    @Override
    public java.lang.String toString() {
        return this.underlying.toString();
    }
}

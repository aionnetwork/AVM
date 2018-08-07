package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;


public class StringBuilder extends Object implements CharSequence, Appendable{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public StringBuilder() {
        this.v = new java.lang.StringBuilder();
    }

    public StringBuilder(int capacity) {
        this.v = new java.lang.StringBuilder(capacity);
    }

    public StringBuilder(String str) {
        this.v = new java.lang.StringBuilder(str.getUnderlying());
    }

    public StringBuilder(CharSequence seq){
        this.v = new java.lang.StringBuilder(seq.avm_toString().getUnderlying());
    }

    //TODO: IOBJECT?
    public StringBuilder avm_append(Object obj) {
        this.v.append(obj);
        return this;
    }

    public StringBuilder avm_append(String str) {
        this.v.append(str.getUnderlying());
        return this;
    }

    public StringBuilder avm_append(StringBuffer sb) {
        this.v.append(sb.getUnderlying());
        return this;
    }

    public StringBuilder avm_append(CharArray str) {
        this.v.append(str.getUnderlying());
        return this;
    }

    public StringBuilder avm_append(CharArray str, int offset, int len) {
        this.v.append(str.getUnderlying(), offset, len);
        return this;
    }

    public StringBuilder avm_append(CharSequence s){
        this.v.append(s.avm_toString());
        return this;
    }

    public StringBuilder avm_append(CharSequence s, int start, int end){
        this.v.append(s.avm_toString().getUnderlying(), start, end);
        return this;
    }

    public StringBuilder avm_append(boolean b) {
        this.v.append(b);
        return this;
    }

    public StringBuilder avm_append(char c) {
        this.v.append(c);
        return this;
    }

    public StringBuilder avm_append(int i) {
        this.v.append(i);
        return this;
    }

    public StringBuilder avm_append(long lng) {
        this.v.append(lng);
        return this;
    }

    public StringBuilder avm_append(float f) {
        this.v.append(f);
        return this;
    }

    public StringBuilder avm_append(double d) {
        this.v.append(d);
        return this;
    }

    public StringBuilder avm_appendCodePoint(int codePoint) {
        this.v.appendCodePoint(codePoint);
        return this;
    }

    public StringBuilder avm_delete(int start, int end) {
        this.v.delete(start, end);
        return this;
    }

    public StringBuilder avm_deleteCharAt(int index) {
        this.v.deleteCharAt(index);
        return this;
    }

    public StringBuilder avm_replace(int start, int end, String str) {
        this.v = this.v.replace(start, end, str.getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int index, CharArray str, int offset,
                                                int len)
    {
        this.v.insert(index, str.getUnderlying(), offset, len);
        return this;
    }

    //TODO: IOBJECT?
    public StringBuilder avm_insert(int offset, Object obj) {
        this.v.insert(offset, obj);
        return this;
    }

    public StringBuilder avm_insert(int offset, String str) {
        this.v.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int offset, CharArray str) {
        this.v.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int dstOffset, CharSequence s) {
        this.v.insert(dstOffset, s.avm_toString().getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int dstOffset, CharSequence s, int start, int end) {
        this.v.insert(dstOffset, s.avm_subSequence(start, end).avm_toString().getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int offset, boolean b) {
        this.v.insert(offset, b);
        return this;
    }

    public StringBuilder avm_insert(int offset, char c) {
        this.v.insert(offset, c);
        return this;
    }

    public StringBuilder avm_insert(int offset, int i) {
        this.v.insert(offset, i);
        return this;
    }

    public StringBuilder avm_insert(int offset, long l) {
        this.v.insert(offset, l);
        return this;
    }

    public StringBuilder avm_insert(int offset, float f) {
        this.v.insert(offset, f);
        return this;
    }

    public StringBuilder avm_insert(int offset, double d) {
        this.v.insert(offset, d);
        return this;
    }

    public int avm_indexOf(String str) {
        return this.v.indexOf(str.getUnderlying());
    }

    public int avm_indexOf(String str, int fromIndex) {
        return this.v.indexOf(str.getUnderlying(), fromIndex);
    }

    public int avm_lastIndexOf(String str) {
        return this.v.lastIndexOf(str.getUnderlying());
    }

    public int avm_lastIndexOf(String str, int fromIndex) {
        return this.v.lastIndexOf(str.getUnderlying(), fromIndex);
    }

    public StringBuilder avm_reverse() {
        this.v.reverse();
        return this;
    }

    public String avm_toString() {
        return new String(this);
    }

    public char avm_charAt(int index){
        return this.v.charAt(index);
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

    private java.lang.StringBuilder v;

    public java.lang.StringBuilder getUnderlying() {
        return v;
    }

    // Deserializer support.
    public StringBuilder(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    //========================================================
    // Methods below are deprecated
    //========================================================


    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

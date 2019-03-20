package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.*;
import org.aion.avm.RuntimeMethodFeeSchedule;

/**
 * TODO:  Ensure that none of the interface we have provided exposes underlying implementation details (slack buffer space, etc), since we would
 * otherwise need to take that into account with our serialization strategy.
 */
public class StringBuffer extends Object implements CharSequence, Appendable{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    public StringBuffer() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_constructor);
        this.v = new java.lang.StringBuffer();
    }

    public StringBuffer(int capacity) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_constructor_1);
        this.v = new java.lang.StringBuffer(capacity);
    }

    public StringBuffer(String str) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_constructor_2);
        this.v = new java.lang.StringBuffer(str.getUnderlying());
    }

    public StringBuffer(CharSequence seq) {
        this(seq.avm_length() + 16);
        avm_append(seq);
    }

    public int avm_length() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_length);
        return internalLength();
    }

    public int avm_capacity() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_capacity);
        return this.v.capacity();
    }

    public void avm_ensureCapacity(int minimumCapacity){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_ensureCapacity);
        this.v.ensureCapacity(minimumCapacity);
    }

    public void avm_trimToSize() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_trimToSize + internalLength());
        this.v.trimToSize();
    }

    public void avm_setLength(int newLength) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_setLength);
        this.v.setLength(newLength);
    }

    public char avm_charAt(int index) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_charAt);
        return this.v.charAt(index);
    }

    public int avm_codePointAt(int index) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_codePointAt);
        return this.v.codePointAt(index);
    }

    public int avm_codePointBefore(int index) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_codePointBefore);
        return this.v.codePointBefore(index);
    }

    public int avm_codePointCount(int beginIndex, int endIndex) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_codePointCount + java.lang.Math.max(endIndex - beginIndex, 0));
        return this.v.codePointCount(beginIndex, endIndex);
    }

    public int avm_offsetByCodePoints(int index, int codePointOffset) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_offsetByCodePoints + java.lang.Math.abs(codePointOffset));
        return this.v.offsetByCodePoints(index, codePointOffset);
    }

    public void avm_getChars(int srcBegin, int srcEnd, CharArray dst,
                             int dstBegin)
    {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_getChars + java.lang.Math.max(srcEnd - srcBegin, 0));
        this.v.getChars(srcBegin, srcEnd, dst.getUnderlying(), dstBegin);
    }

    public void avm_setCharAt(int index, char ch) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_setCharAt);
        this.v.setCharAt(index, ch);
    }

    public StringBuffer avm_append(IObject obj) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append);
        // using public method to stay consistent with jcl implementation
        this.v = this.v.append(String.internalValueOfObject(obj).getUnderlying());
        return this;
    }

    public StringBuffer avm_append(String str) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * str.internalLength());
        this.v = this.v.append(str);
        return this;
    }

    public StringBuffer avm_append(StringBuffer sb) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * sb.internalLength());
        this.v = this.v.append(sb.v);
        return this;
    }

    public StringBuffer avm_append(CharSequence s){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_3 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * s.avm_length());
        this.v = this.v.append(s.avm_toString());
        return this;
    }

    public StringBuffer avm_append(CharSequence s, int start, int end){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_4 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(end - start, 0));
        this.v = this.v.append(s.avm_toString().getUnderlying(), start, end);
        return this;
    }

    public StringBuffer avm_append(CharArray str) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_5 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * str.length());
        this.v = this.v.append(str.getUnderlying());
        return this;
    }

    public StringBuffer avm_append(CharArray str, int offset, int len) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_6 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * len);
        this.v = this.v.append(str.getUnderlying(), offset, len);
        return this;
    }

    public StringBuffer avm_append(boolean b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_7);
        this.v = this.v.append(b);
        return this;
    }

    public StringBuffer avm_append(char c) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_8);
        this.v = this.v.append(c);
        return this;
    }

    public StringBuffer avm_append(int i) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_9);
        this.v = this.v.append(i);
        return this;
    }

    public StringBuffer avm_appendCodePoint(int codePoint) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_appendCodePoint);
        this.v = this.v.appendCodePoint(codePoint);
        return this;
    }

    public StringBuffer avm_append(long lng) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_10);
        this.v = this.v.append(lng);
        return this;
    }

    public StringBuffer avm_append(float f) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_11);
        this.v = this.v.append(f);
        return this;
    }

    public StringBuffer avm_append(double d) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_12);
        this.v = this.v.append(d);
        return this;
    }

    public StringBuffer avm_delete(int start, int end) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_delete + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(internalLength() - start, 0));
        this.v = this.v.delete(start, end);
        return this;
    }

    public StringBuffer avm_deleteCharAt(int index) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_deleteCharAt + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(internalLength() - index, 0));
        this.v = this.v.deleteCharAt(index);
        return this;
    }

    public StringBuffer avm_replace(int start, int end, String str) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_replace + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(internalLength() - start, 0));
        this.v = this.v.replace(start, end, str.getUnderlying());
        return this;
    }

    public String avm_substring(int start) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_substring + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(internalLength() - start, 0));
        return new String(this.v.substring(start));
    }

    public CharSequence avm_subSequence(int start, int end){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_subSequence + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(end - start, 0));
        return new String(this.v.subSequence(start, end).toString());
    }

    public String avm_substring(int start, int end) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_substring_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(end - start, 0));
        return new String(this.v.substring(start, end));
    }

    public StringBuffer avm_insert(int index, CharArray str, int offset,
                                            int len)
    {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(internalLength() - index, 0));
        this.v.insert(index, str.getUnderlying(), offset, len);
        return this;
    }

    public StringBuffer avm_insert(int offset, IObject obj) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(internalLength() - offset, 0));
        this.v.insert(offset, obj);
        return this;
    }

    public StringBuffer avm_insert(int offset, String str) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * (str.internalLength() + java.lang.Math.max(internalLength() - offset, 0)));
        this.v.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuffer avm_insert(int offset, CharArray str) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_3 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * (str.length() + java.lang.Math.max(internalLength() - offset, 0)));
        this.v.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuffer avm_insert(int dstOffset, CharSequence s){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_4 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * (s.avm_length() + java.lang.Math.max(internalLength() - dstOffset, 0)));
        this.v.insert(dstOffset, s.avm_toString());
        return this;
    }

    public StringBuffer avm_insert(int dstOffset, CharSequence s, int start, int end) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_5 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * (java.lang.Math.max(end - start, 0) + java.lang.Math.max(internalLength() - dstOffset, 0)));
        this.v.insert(dstOffset, s.avm_subSequence(start, end));
        return this;
    }

    public StringBuffer avm_insert(int offset, boolean b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_6);
        this.v.insert(offset, b);
        return this;
    }

    public StringBuffer avm_insert(int offset, char c) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_7);
        this.v.insert(offset, c);
        return this;
    }

    public StringBuffer avm_insert(int offset, int i) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_8);
        this.v.insert(offset, i);
        return this;
    }

    public StringBuffer avm_insert(int offset, long l) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_9);
        this.v.insert(offset, l);
        return this;
    }

    public StringBuffer avm_insert(int offset, float f) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_10);
        this.v.insert(offset, f);
        return this;
    }

    public StringBuffer avm_insert(int offset, double d) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_11);
        this.v.insert(offset, d);
        return this;
    }

    public int avm_indexOf(String str) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_indexOf + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * internalLength());
        return this.v.indexOf(str.getUnderlying());
    }

    public int avm_indexOf(String str, int fromIndex) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_indexOf_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(internalLength() - fromIndex, 0));
        return this.v.indexOf(str.getUnderlying(), fromIndex);
    }

    public int avm_lastIndexOf(String str) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_lastIndexOf + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * internalLength());
        return this.v.lastIndexOf(str.getUnderlying());
    }

    public int avm_lastIndexOf(String str, int fromIndex) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_lastIndexOf_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(internalLength() - fromIndex, 0));
        return this.v.lastIndexOf(str.getUnderlying(), fromIndex);
    }

    public StringBuffer avm_reverse() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_reverse + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * internalLength());
        this.v.reverse();
        return this;
    }

    public String avm_toString() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_toString);
        return new String(this);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================
    private java.lang.StringBuffer v;

    public java.lang.StringBuffer getUnderlying() {
        return v;
    }

    // Deserializer support.
    public StringBuffer(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(String.class, deserializer);
        
        // We serialize this as a string.
        java.lang.String simpler = CodecIdioms.deserializeString(deserializer);
        this.v = new java.lang.StringBuffer(simpler);
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(String.class, serializer);
        
        // We serialize this as a string.
        CodecIdioms.serializeString(serializer, this.v.toString());
    }

    public int internalLength(){
        return this.v.length();
    }
    //========================================================
    // Methods below are deprecated
    //========================================================



    //========================================================
    // Methods below are excluded from shadowing
    //========================================================
}

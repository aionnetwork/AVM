package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.CodecIdioms;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.RuntimeMethodFeeSchedule;

/**
 * TODO:  Ensure that none of the interface we have provided exposes underlying implementation details (slack buffer space, etc), since we would
 * otherwise need to take that into account with our serialization strategy.
 */
public class StringBuffer extends Object implements CharSequence, Appendable{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public StringBuffer() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_constructor);
        this.v = new java.lang.StringBuffer();
    }

    public StringBuffer(int capacity) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_constructor_1);
        this.v = new java.lang.StringBuffer(capacity);
    }

    public StringBuffer(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_constructor_2);
        this.v = new java.lang.StringBuffer(str.getUnderlying());
    }

    public StringBuffer(CharSequence seq) {
        this(seq.avm_length() + 16);
        avm_append(seq);
    }

    public int avm_length() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_length);
        return this.v.length();
    }

    public int avm_capacity() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_capacity);
        return this.v.capacity();
    }

    public void avm_ensureCapacity(int minimumCapacity){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_ensureCapacity);
        this.v.ensureCapacity(minimumCapacity);
    }

    public void avm_trimToSize() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_trimToSize + avm_length());
        this.v.trimToSize();
    }

    public void avm_setLength(int newLength) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_setLength);
        this.v.setLength(newLength);
    }

    public char avm_charAt(int index) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_charAt);
        return this.v.charAt(index);
    }

    public int avm_codePointAt(int index) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_codePointAt);
        return this.v.codePointAt(index);
    }

    public int avm_codePointBefore(int index) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_codePointBefore);
        return this.v.codePointBefore(index);
    }

    public int avm_codePointCount(int beginIndex, int endIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_codePointCount + java.lang.Math.max(endIndex - beginIndex, 0));
        return this.v.codePointCount(beginIndex, endIndex);
    }

    public int avm_offsetByCodePoints(int index, int codePointOffset) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_offsetByCodePoints + java.lang.Math.abs(codePointOffset));
        return this.v.offsetByCodePoints(index, codePointOffset);
    }

    public void avm_getChars(int srcBegin, int srcEnd, char[] dst,
                                      int dstBegin)
    {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_getChars + java.lang.Math.max(srcEnd - srcBegin, 0));
        this.v.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public void avm_setCharAt(int index, char ch) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_setCharAt);
        this.v.setCharAt(index, ch);
    }

    //TODO: IOBJECT?
    public StringBuffer avm_append(Object obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append);
        this.v = this.v.append(obj);
        return this;
    }

    public StringBuffer avm_append(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * str.avm_length());
        this.v = this.v.append(str);
        return this;
    }

    public StringBuffer avm_append(StringBuffer sb) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * sb.avm_length());
        this.v = this.v.append(sb.v);
        return this;
    }

    public StringBuffer avm_append(CharSequence s){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_3 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * s.avm_length());
        this.v = this.v.append(s.avm_toString());
        return this;
    }

    public StringBuffer avm_append(CharSequence s, int start, int end){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_4 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(end - start, 0));
        this.v = this.v.append(s.avm_toString().getUnderlying(), start, end);
        return this;
    }

    public StringBuffer avm_append(CharArray str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_5 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * str.length());
        this.v = this.v.append(str.getUnderlying());
        return this;
    }

    public StringBuffer avm_append(CharArray str, int offset, int len) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_6 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * len);
        this.v = this.v.append(str.getUnderlying(), offset, len);
        return this;
    }

    public StringBuffer avm_append(boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_7);
        this.v = this.v.append(b);
        return this;
    }

    public StringBuffer avm_append(char c) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_8);
        this.v = this.v.append(c);
        return this;
    }

    public StringBuffer avm_append(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_9);
        this.v = this.v.append(i);
        return this;
    }

    public StringBuffer avm_appendCodePoint(int codePoint) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_appendCodePoint);
        this.v = this.v.appendCodePoint(codePoint);
        return this;
    }

    public StringBuffer avm_append(long lng) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_10);
        this.v = this.v.append(lng);
        return this;
    }

    public StringBuffer avm_append(float f) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_11);
        this.v = this.v.append(f);
        return this;
    }

    public StringBuffer avm_append(double d) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_append_12);
        this.v = this.v.append(d);
        return this;
    }

    public StringBuffer avm_delete(int start, int end) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_delete + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(avm_length() - start, 0));
        this.v = this.v.delete(start, end);
        return this;
    }

    public StringBuffer avm_deleteCharAt(int index) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_deleteCharAt + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(avm_length() - index, 0));
        this.v = this.v.deleteCharAt(index);
        return this;
    }

    public StringBuffer avm_replace(int start, int end, String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_replace + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(avm_length() - start, 0));
        this.v = this.v.replace(start, end, str.getUnderlying());
        return this;
    }

    public String avm_substring(int start) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_substring + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(avm_length() - start, 0));
        return new String(this.v.substring(start));
    }

    public CharSequence avm_subSequence(int start, int end){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_subSequence + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(end - start, 0));
        return new String(this.v.subSequence(start, end).toString());
    }

    public String avm_substring(int start, int end) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_substring_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(end - start, 0));
        return new String(this.v.substring(start, end));
    }

    public StringBuffer avm_insert(int index, CharArray str, int offset,
                                            int len)
    {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(avm_length() - index, 0));
        this.v.insert(index, str.getUnderlying(), offset, len);
        return this;
    }

    //TODO: IOBJECT?
    public StringBuffer avm_insert(int offset, Object obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(avm_length() - offset, 0));
        this.v.insert(offset, obj);
        return this;
    }

    public StringBuffer avm_insert(int offset, String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_2 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * (str.avm_length() + java.lang.Math.max(avm_length() - offset, 0)));
        this.v.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuffer avm_insert(int offset, CharArray str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_3 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * (str.length() + java.lang.Math.max(avm_length() - offset, 0)));
        this.v.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuffer avm_insert(int dstOffset, CharSequence s){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_4 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * (s.avm_length() + java.lang.Math.max(avm_length() - dstOffset, 0)));
        this.v.insert(dstOffset, s.avm_toString());
        return this;
    }

    public StringBuffer avm_insert(int dstOffset, CharSequence s, int start, int end) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_5 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * (java.lang.Math.max(end - start, 0) + java.lang.Math.max(avm_length() - dstOffset, 0)));
        this.v.insert(dstOffset, s.avm_subSequence(start, end));
        return this;
    }

    public StringBuffer avm_insert(int offset, boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_6);
        this.v.insert(offset, b);
        return this;
    }

    public StringBuffer avm_insert(int offset, char c) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_7);
        this.v.insert(offset, c);
        return this;
    }

    public StringBuffer avm_insert(int offset, int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_8);
        this.v.insert(offset, i);
        return this;
    }

    public StringBuffer avm_insert(int offset, long l) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_9);
        this.v.insert(offset, l);
        return this;
    }

    public StringBuffer avm_insert(int offset, float f) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_10);
        this.v.insert(offset, f);
        return this;
    }

    public StringBuffer avm_insert(int offset, double d) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_insert_11);
        this.v.insert(offset, d);
        return this;
    }

    public int avm_indexOf(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_indexOf + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * avm_length());
        return this.v.indexOf(str.getUnderlying());
    }

    public int avm_indexOf(String str, int fromIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_indexOf_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(avm_length() - fromIndex, 0));
        return this.v.indexOf(str.getUnderlying(), fromIndex);
    }

    public int avm_lastIndexOf(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_lastIndexOf + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * avm_length());
        return this.v.lastIndexOf(str.getUnderlying());
    }

    public int avm_lastIndexOf(String str, int fromIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_lastIndexOf_1 + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * java.lang.Math.max(avm_length() - fromIndex, 0));
        return this.v.lastIndexOf(str.getUnderlying(), fromIndex);
    }

    public StringBuffer avm_reverse() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_reverse + RuntimeMethodFeeSchedule.RT_METHOD_FEE_FACTOR * avm_length());
        this.v.reverse();
        return this;
    }

    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuffer_avm_toString);
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


    //========================================================
    // Methods below are deprecated
    //========================================================



    //========================================================
    // Methods below are excluded from shadowing
    //========================================================
}

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
public class StringBuilder extends Object implements CharSequence, Appendable{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public StringBuilder() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_constructor);
        this.v = new java.lang.StringBuilder();
    }

    public StringBuilder(int capacity) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_constructor_1);
        this.v = new java.lang.StringBuilder(capacity);
    }

    public StringBuilder(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_constructor_2);
        this.v = new java.lang.StringBuilder(str.getUnderlying());
    }

    public StringBuilder(CharSequence seq){
        this.v = new java.lang.StringBuilder(seq.avm_toString().getUnderlying());
    }

    //TODO: IOBJECT?
    public StringBuilder avm_append(Object obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append);
        this.v.append(obj);
        return this;
    }

    public StringBuilder avm_append(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_1 + 5 * str.avm_length());
        this.v.append(str.getUnderlying());
        return this;
    }

    public StringBuilder avm_append(StringBuffer sb) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_2 + 5 * sb.avm_length());
        this.v.append(sb.getUnderlying());
        return this;
    }

    public StringBuilder avm_append(CharArray str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_3 + 5 * str.length());
        this.v.append(str.getUnderlying());
        return this;
    }

    public StringBuilder avm_append(CharArray str, int offset, int len) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_4 + 5 * len);
        this.v.append(str.getUnderlying(), offset, len);
        return this;
    }

    public StringBuilder avm_append(CharSequence s){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_5 + 5 * s.avm_length());
        this.v.append(s.avm_toString());
        return this;
    }

    public StringBuilder avm_append(CharSequence s, int start, int end){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_6 + 5 * java.lang.Math.max(end - start, 0));
        this.v.append(s.avm_toString().getUnderlying(), start, end);
        return this;
    }

    public StringBuilder avm_append(boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_7);
        this.v.append(b);
        return this;
    }

    public StringBuilder avm_append(char c) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_8);
        this.v.append(c);
        return this;
    }

    public StringBuilder avm_append(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_9);
        this.v.append(i);
        return this;
    }

    public StringBuilder avm_append(long lng) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_10);
        this.v.append(lng);
        return this;
    }

    public StringBuilder avm_append(float f) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_11);
        this.v.append(f);
        return this;
    }

    public StringBuilder avm_append(double d) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_append_12);
        this.v.append(d);
        return this;
    }

    public StringBuilder avm_appendCodePoint(int codePoint) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_appendCodePoint);
        this.v.appendCodePoint(codePoint);
        return this;
    }

    public StringBuilder avm_delete(int start, int end) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_delete);
        this.v.delete(start, end);
        return this;
    }

    public StringBuilder avm_deleteCharAt(int index) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_deleteCharAt);
        this.v.deleteCharAt(index);
        return this;
    }

    public StringBuilder avm_replace(int start, int end, String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_replace + 5 * java.lang.Math.max(end - start, 0));
        this.v = this.v.replace(start, end, str.getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int index, CharArray str, int offset,
                                                int len)
    {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert + 5 * len);
        this.v.insert(index, str.getUnderlying(), offset, len);
        return this;
    }

    //TODO: IOBJECT?
    public StringBuilder avm_insert(int offset, Object obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_1 + 5 * java.lang.Math.max(avm_length() - offset, 0));
        this.v.insert(offset, obj);
        return this;
    }

    public StringBuilder avm_insert(int offset, String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_2 + 5 * (str.avm_length() + java.lang.Math.max(avm_length() - offset, 0)));
        this.v.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int offset, CharArray str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_3 + 5 * (str.length() + java.lang.Math.max(avm_length() - offset, 0)));
        this.v.insert(offset, str.getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int dstOffset, CharSequence s) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_4 + 5 * (s.avm_length() + java.lang.Math.max(avm_length() - dstOffset, 0)));
        this.v.insert(dstOffset, s.avm_toString().getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int dstOffset, CharSequence s, int start, int end) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_5 + java.lang.Math.max(end - start, 0) + java.lang.Math.max(avm_length() - dstOffset, 0));
        this.v.insert(dstOffset, s.avm_subSequence(start, end).avm_toString().getUnderlying());
        return this;
    }

    public StringBuilder avm_insert(int offset, boolean b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_6);
        this.v.insert(offset, b);
        return this;
    }

    public StringBuilder avm_insert(int offset, char c) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_7);
        this.v.insert(offset, c);
        return this;
    }

    public StringBuilder avm_insert(int offset, int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_8);
        this.v.insert(offset, i);
        return this;
    }

    public StringBuilder avm_insert(int offset, long l) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_9);
        this.v.insert(offset, l);
        return this;
    }

    public StringBuilder avm_insert(int offset, float f) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_10);
        this.v.insert(offset, f);
        return this;
    }

    public StringBuilder avm_insert(int offset, double d) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_insert_11);
        this.v.insert(offset, d);
        return this;
    }

    public int avm_indexOf(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_indexOf + 5 * avm_length());
        return this.v.indexOf(str.getUnderlying());
    }

    public int avm_indexOf(String str, int fromIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_indexOf_1 + 5 * java.lang.Math.max(avm_length() - fromIndex, 0));
        return this.v.indexOf(str.getUnderlying(), fromIndex);
    }

    public int avm_lastIndexOf(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_lastIndexOf + 5 * avm_length());
        return this.v.lastIndexOf(str.getUnderlying());
    }

    public int avm_lastIndexOf(String str, int fromIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_lastIndexOf_1 + 5 * java.lang.Math.max(avm_length() - fromIndex, 0));
        return this.v.lastIndexOf(str.getUnderlying(), fromIndex);
    }

    public StringBuilder avm_reverse() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_reverse + 5 * avm_length());
        this.v.reverse();
        return this;
    }

    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_toString);
        return new String(this);
    }

    public char avm_charAt(int index){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_charAt);
        return this.v.charAt(index);
    }

    public CharSequence avm_subSequence(int start, int end) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_subSequence + 5 * java.lang.Math.max(end - start, 0));
        return this.avm_toString().avm_subSequence(start, end);
    }

    public int avm_length(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StringBuilder_avm_length);
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
    public StringBuilder(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(String.class, deserializer);
        
        // We serialize this as a string.
        java.lang.String simpler = CodecIdioms.deserializeString(deserializer);
        this.v = new java.lang.StringBuilder(simpler);
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

package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.internal.CodecIdioms;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;

import java.io.UnsupportedEncodingException;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class String extends Object implements Comparable<String>, CharSequence {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public String() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor);
        this.v = new java.lang.String();
    }

    public String(String original) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_1);
        this.v = new java.lang.String(original.getUnderlying());
    }

    public String(CharArray value) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_2);
        this.v = new java.lang.String(value.getUnderlying());
    }

    public String(CharArray value, int offset, int count) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_3);
        this.v = new java.lang.String(value.getUnderlying(), offset, count);
    }

    public String(IntArray codePoints, int offset, int count) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_4);
        this.v = new java.lang.String(codePoints.getUnderlying(), offset, count);
    }

    public String(ByteArray bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_5);
        this.v = new java.lang.String(bytes.getUnderlying(), offset, length, charsetName.v);
    }

    public String(ByteArray bytes, String charsetName) throws UnsupportedEncodingException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_6);
        this.v = new java.lang.String(bytes.getUnderlying(), charsetName.v);
    }

    public String(ByteArray bytes, int offset, int length){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_7);
        this.v = new java.lang.String(bytes.getUnderlying(), offset, length);
    }

    public String(ByteArray bytes){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_8);
        this.v = new java.lang.String(bytes.getUnderlying());
    }

    public String(StringBuffer buffer){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_9);
        this.v = new java.lang.String(buffer.getUnderlying());
    }

    public String(StringBuilder builder) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_constructor_10);
        this.v = new java.lang.String(builder.getUnderlying());
    }

    public int avm_length(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_length);
        lazyLoad();
        return v.length();
    }

    public boolean avm_isEmpty() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_isEmpty);
        lazyLoad();
        return v.isEmpty();
    }

    public char avm_charAt(int index) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_charAt);
        lazyLoad();
        return this.v.charAt(index);
    }

    public int avm_codePointAt(int index) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_codePointAt);
        lazyLoad();
        return this.v.codePointAt(index);
    }

    public int avm_codePointBefore(int index) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_codePointBefore);
        lazyLoad();
        return this.v.codePointBefore(index);
    }

    public int avm_codePointCount(int beginIndex, int endIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_codePointCount + java.lang.Math.max(endIndex - beginIndex, 0));
        lazyLoad();
        return this.v.codePointCount(beginIndex, endIndex);
    }

    public int avm_offsetByCodePoints(int index, int codePointOffset){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_offsetByCodePoints);
        lazyLoad();
        return this.v.offsetByCodePoints(index, codePointOffset);
    }

    public void avm_getChars(int srcBegin, int srcEnd, CharArray dst, int dstBegin) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_getChars + 5 * java.lang.Math.max(srcEnd - srcBegin, 0));
        lazyLoad();
        this.v.getChars(srcBegin, srcEnd, dst.getUnderlying(), dstBegin);
    }

    public ByteArray avm_getBytes(String charsetName) throws UnsupportedEncodingException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_getBytes + 5 * avm_length());
        lazyLoad();
        return new ByteArray(this.v.getBytes(charsetName.v));
    }

    public ByteArray avm_getBytes(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_getBytes_1 + 5 * avm_length());
        lazyLoad();
        return new ByteArray(this.v.getBytes());
    }

    public boolean avm_equals(IObject anObject) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_equals + 5 * avm_length());
        if (!(anObject instanceof String)){
            return false;
        }

        String toComp = (String) anObject;
        toComp.lazyLoad();
        this.lazyLoad();

        return this.v.equals(toComp.v);
    }

    public boolean avm_contentEquals(StringBuffer sb) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_contentEquals + 5 * avm_length());
        lazyLoad();
        return this.v.contentEquals(sb.getUnderlying());
    }

    public boolean avm_contentEquals(CharSequence cs){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_contentEquals_1 + 5 * avm_length());
        lazyLoad();
        return this.v.contentEquals(cs.avm_toString().getUnderlying());
    }

    public boolean avm_equalsIgnoreCase(String anotherString) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_equalsIgnoreCase + 5 * avm_length());
        lazyLoad();
        return this.v.equalsIgnoreCase(anotherString.v);
    }

    public int avm_compareTo(String anotherString) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_compareTo + 5 * avm_length());
        lazyLoad();
        return this.v.compareTo(anotherString.getUnderlying());
    }

    public int avm_compareToIgnoreCase(String str){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_compareToIgnoreCase + 5 * avm_length());
        lazyLoad();
        return this.v.compareToIgnoreCase(str.v);
    }

    public boolean avm_regionMatches(int toffset, String other, int ooffset, int len) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_regionMatches + 5 * len);
        lazyLoad();
        return this.v.regionMatches(toffset, other.v, ooffset, len);
    }

    public boolean avm_regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_regionMatches_1 + 5 * len);
        lazyLoad();
        return this.v.regionMatches(ignoreCase, toffset, other.v, ooffset, len);
    }

    public boolean avm_startsWith(String prefix, int toffset) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_startsWith + 5 * prefix.avm_length());
        lazyLoad();
        return this.v.startsWith(prefix.v, toffset);
    }

    public boolean avm_startsWith(String prefix) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_startsWith_1 + 5 * prefix.avm_length());
        lazyLoad();
        return this.v.startsWith(prefix.v);
    }

    public boolean avm_endsWith(String prefix) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_endsWith + 5 * prefix.avm_length());
        lazyLoad();
        return this.v.endsWith(prefix.v);
    }

    @Override
    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_hashCode + 5 * avm_length());
        lazyLoad();
        return this.v.hashCode();
    }

    public int avm_indexOf(int ch) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_indexOf + 5 * avm_length());
        lazyLoad();
        return this.v.indexOf(ch);
    }

    public int avm_indexOf(int ch, int fromIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_indexOf_1 + 5 * java.lang.Math.max(avm_length() - fromIndex, 0));
        lazyLoad();
        return this.v.indexOf(ch, fromIndex);
    }

    public int avm_lastIndexOf(int ch) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_lastIndexOf + 5 * avm_length());
        lazyLoad();
        return this.v.lastIndexOf(ch);
    }

    public int avm_lastIndexOf(int ch, int fromIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_lastIndexOf_1 + 5 * java.lang.Math.max(avm_length() - fromIndex, 0));
        lazyLoad();
        return this.v.lastIndexOf(ch, fromIndex);
    }

    public int avm_indexOf(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_indexOf_2 + 5 * avm_length());
        lazyLoad();
        str.lazyLoad();
        return this.v.indexOf(str.v);
    }

    public int avm_lastIndexOf(String str) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_lastIndexOf_2 + 5 * avm_length());
        lazyLoad();
        str.lazyLoad();
        return this.v.lastIndexOf(str.v);
    }

    public int avm_lastIndexOf(String str, int fromIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_lastIndexOf_3 + 5 * java.lang.Math.max(avm_length() - fromIndex, 0));
        lazyLoad();
        str.lazyLoad();
        return this.v.lastIndexOf(str.v, fromIndex);
    }

    public String avm_substring(int beginIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_substring + 5 * java.lang.Math.max(avm_length() - beginIndex, 0));
        lazyLoad();
        return new String(this.v.substring(beginIndex));
    }

    public String avm_substring(int beginIndex, int endIndex) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_substring_1 + 5 * java.lang.Math.max(endIndex - beginIndex, 0));
        lazyLoad();
        return new String(this.v.substring(beginIndex, endIndex));
    }

    public CharSequence avm_subSequence(int beginIndex, int endIndex){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_subSequence + 5 * java.lang.Math.max(endIndex - beginIndex, 0));
        lazyLoad();
        return this.avm_substring(beginIndex, endIndex);
    }

    public String avm_concat(String str){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_concat + 5 * (str.avm_length() + avm_length()));
        lazyLoad();
        str.lazyLoad();
        return new String(this.v.concat(str.v));
    }

    public String avm_replace(char oldChar, char newChar) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_replace + 5 * avm_length());
        lazyLoad();
        return new String(this.v.replace(oldChar, newChar));
    }

    public boolean avm_matches(String regex){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_matches + 5 * avm_length());
        lazyLoad();
        regex.lazyLoad();
        return this.v.matches(regex.v);
    }

    public boolean avm_contains(CharSequence s){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_contains + 5 * avm_length());
        lazyLoad();
        ((Object)s).lazyLoad();
        return avm_indexOf(s.avm_toString()) >= 0;
    }

    public String avm_replaceFirst(String regex, String replacement){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_replaceFirst + 5 * avm_length());
        lazyLoad();
        regex.lazyLoad();
        replacement.lazyLoad();
        return new String(this.v.replaceFirst(regex.v, replacement.v));
    }

    public String avm_replaceAll(String regex, String replacement) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_replaceAll + 5 * avm_length());
        lazyLoad();
        regex.lazyLoad();
        replacement.lazyLoad();
        return new String(this.v.replaceAll(regex.v, replacement.v));
    }

    public String avm_replace(CharSequence target, CharSequence replacement){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_replace_1 + 5 * avm_length());
        lazyLoad();
        ((Object)target).lazyLoad();
        ((Object)replacement).lazyLoad();
        return new String(this.v.replace(target.avm_toString().getUnderlying(),
                replacement.avm_toString().getUnderlying()));
    }

    //public String[] split(String regex, int limit) {}

    //public String[] split(String regex){}

    public String avm_toLowerCase(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_toLowerCase + 5 * avm_length());
        lazyLoad();
        return new String(this.v.toLowerCase());
    }

    public String avm_toUpperCase(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_toUpperCase + 5 * avm_length());
        lazyLoad();
        return new String(this.v.toUpperCase());
    }

    public String avm_trim() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_trim + 5 * avm_length());
        lazyLoad();
        return new String(this.v.trim());
    }

    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_toString);
        return this;
    }

    public CharArray avm_toCharArray() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_toCharArray + 5 * avm_length());
        lazyLoad();
        return new CharArray(this.v.toCharArray());
    }


    //TODO: IOBJECT?
    public static String avm_valueOf(Object obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_valueOf);
        obj.lazyLoad();
        return new String(java.lang.String.valueOf(obj));
    }

    public static String avm_valueOf(CharArray a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_valueOf_1 + 5 * a.length());
        a.lazyLoad();
        return new String(java.lang.String.valueOf(a.getUnderlying()));
    }

    public static String avm_valueOf(CharArray data, int offset, int count){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_valueOf_2 + 5 * count);
        data.lazyLoad();
        return new String(java.lang.String.valueOf(data.getUnderlying(), offset, count));
    }

    public static String avm_copyValueOf(CharArray data, int offset, int count){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_copyValueOf + 5 * count);
        data.lazyLoad();
        return new String(java.lang.String.copyValueOf(data.getUnderlying(), offset, count));
    }

    public static String avm_copyValueOf(CharArray a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_copyValueOf_1 + 5 * a.length());
        a.lazyLoad();
        return new String(java.lang.String.copyValueOf(a.getUnderlying()));
    }

    public static String avm_valueOf(boolean b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_valueOf_3);
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(char b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_valueOf_4);
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(int b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_valueOf_5);
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(long b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_valueOf_6);
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(float b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_valueOf_7);
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(double b){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.String_avm_valueOf_8);
        return new String(java.lang.String.valueOf(b));
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.lang.String v;

    // @Internal
    public String(java.lang.String underlying) {
        this.v = underlying;
    }

    // Deserializer support.
    public String(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(String.class, deserializer);
        this.v = CodecIdioms.deserializeString(deserializer);
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(String.class, serializer);
        CodecIdioms.serializeString(serializer, this.v);
    }

    @Override
    public boolean equals(java.lang.Object anObject) {
        return anObject instanceof String && this.v.equals(((String) anObject).v);
    }

    @Override
    public int hashCode() {
        // We probably want a consistent hashCode answer, for strings, since they are data-defined.
        return this.v.hashCode();
    }

    // NOTE:  This toString() cannot be called by the contract code (it will call avm_toString()) but our runtime and test code can call this.
    @Override
    public java.lang.String toString() {
        lazyLoad();
        return this.v;
    }

    //internal
    public java.lang.String getUnderlying(){
        lazyLoad();
        return v;
    }

    //========================================================
    // Methods below are deprecated, we don't shadow them
    //========================================================

    //public String(byte ascii[], int hibyte, int offset, int count)

    //public String(byte ascii[], int hibyte)

    //public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin)


    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public String(byte bytes[], int offset, int length, Charset charset)

    //public String(byte bytes[], Charset charset)

    //public byte[] getBytes(Charset charset)

    //public static final Comparator<String> CASE_INSENSITIVE_ORDER

    //public static String join(CharSequence delimiter, CharSequence... elements)

    //public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements)

    //public String toLowerCase(Locale locale)

    //public String toUpperCase(Locale locale)

    //public IntStream chars()

    //public IntStream codePoints()

    //public static String format(Locale l, String format, Object... args) {

    //public String avm_intern()

    //public static String format(String format, Object... args)

}

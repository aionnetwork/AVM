package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;


public class String extends Object implements Comparable<String>, CharSequence {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    private Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;



    public String() {
        this.v = new java.lang.String();
    }

    // Deserializer support.
    public String(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(String.class, deserializer);
        
        // TODO:  We probably want faster array copies.
        int length = deserializer.readInt();
        byte[] data = new byte[length];
        for (int i = 0; i < length; ++i) {
            data[i] = deserializer.readByte();
        }
        this.v = new java.lang.String(data, DEFAULT_CHARSET);
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        super.serializeSelf(String.class, serializer, nextObjectQueue);
        
        // TODO:  We probably want faster array copies.
        byte[] data = this.v.getBytes(DEFAULT_CHARSET);
        serializer.writeInt(data.length);
        for (int i = 0; i < data.length; ++i) {
            serializer.writeByte(data[i]);
        }
    }

    public String(String original) {
        this.v = new java.lang.String(original.getV());
    }

    public String(CharArray value) {
        this.v = new java.lang.String(value.getUnderlying());
    }

    public String(CharArray value, int offset, int count) {
        this.v = new java.lang.String(value.getUnderlying(), offset, count);
    }

    public String(IntArray codePoints, int offset, int count) {
        this.v = new java.lang.String(codePoints.getUnderlying(), offset, count);
    }

    public String(ByteArray bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException {
        this.v = new java.lang.String(bytes.getUnderlying(), offset, length, charsetName.getV());
    }

    public String(ByteArray bytes, String charsetName) throws UnsupportedEncodingException {
        this.v = new java.lang.String(bytes.getUnderlying(), charsetName.getV());
    }

    public String(ByteArray bytes, int offset, int length){
        this.v = new java.lang.String(bytes.getUnderlying(), offset, length);
    }

    public String(ByteArray bytes){
        this.v = new java.lang.String(bytes.getUnderlying());
    }

    public String(StringBuffer buffer){
        this.v = new java.lang.String(buffer.getUnderlying());
    }

    public String(StringBuilder builder) {
        this.v = new java.lang.String(builder.getUnderlying());
    }

    public int avm_length(){
        lazyLoad();
        return v.length();
    }

    public boolean avm_isEmpty() {
        lazyLoad();
        return v.isEmpty();
    }

    public char avm_charAt(int index) {
        lazyLoad();
        return this.v.charAt(index);
    }

    public int avm_codePointAt(int index) {
        lazyLoad();
        return this.v.codePointAt(index);
    }

    public int avm_codePointBefore(int index) {
        lazyLoad();
        return this.v.codePointBefore(index);
    }

    public int avm_codePointCount(int beginIndex, int endIndex) {
        lazyLoad();
        return this.v.codePointCount(beginIndex, endIndex);
    }

    public int avm_offsetByCodePoints(int index, int codePointOffset){
        lazyLoad();
        return this.v.offsetByCodePoints(index, codePointOffset);
    }

    public void avm_getChars(int srcBegin, int srcEnd, CharArray dst, int dstBegin) {
        lazyLoad();
        this.v.getChars(srcBegin, srcEnd, dst.getUnderlying(), dstBegin);
    }

    public ByteArray avm_getBytes(String charsetName) throws UnsupportedEncodingException {
        lazyLoad();
        return new ByteArray(this.v.getBytes(charsetName.getV()));
    }

    public ByteArray avm_getBytes(){
        lazyLoad();
        return new ByteArray(this.v.getBytes());
    }

    public boolean avm_equals(IObject anObject) {
        lazyLoad();
        return anObject instanceof String && this.v.equals(((String) anObject).v);
    }

    public boolean avm_contentEquals(StringBuffer sb) {
        lazyLoad();
        return this.v.contentEquals(sb.getUnderlying());
    }

    public boolean avm_contentEquals(CharSequence cs){
        lazyLoad();
        return this.v.contentEquals(cs.avm_toString().getV());
    }

    public boolean avm_equalsIgnoreCase(String anotherString) {
        lazyLoad();
        return this.v.equalsIgnoreCase(anotherString.getV());
    }

    public int avm_compareTo(String anotherString) {
        lazyLoad();
        return this.v.compareTo(anotherString.getV());
    }

    public int avm_compareToIgnoreCase(String str){
        lazyLoad();
        return this.v.compareToIgnoreCase(str.getV());
    }

    public boolean avm_regionMatches(int toffset, String other, int ooffset, int len) {
        lazyLoad();
        return this.v.regionMatches(toffset, other.getV(), ooffset, len);
    }

    public boolean avm_regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        lazyLoad();
        return this.v.regionMatches(ignoreCase, toffset, other.getV(), ooffset, len);
    }

    public boolean avm_startsWith(String prefix, int toffset) {
        lazyLoad();
        return this.v.startsWith(prefix.getV(), toffset);
    }

    public boolean avm_startsWith(String prefix) {
        lazyLoad();
        return this.v.startsWith(prefix.getV());
    }

    public boolean avm_endsWith(String prefix) {
        lazyLoad();
        return this.v.endsWith(prefix.getV());
    }

    @Override
    public int avm_hashCode() {
        lazyLoad();
        return this.v.hashCode();
    }

    public int avm_indexOf(int ch) {
        lazyLoad();
        return this.v.indexOf(ch);
    }

    public int avm_indexOf(int ch, int fromIndex) {
        lazyLoad();
        return this.v.indexOf(ch, fromIndex);
    }

    public int avm_lastIndexOf(int ch) {
        lazyLoad();
        return this.v.lastIndexOf(ch);
    }

    public int avm_lastIndexOf(int ch, int fromIndex) {
        lazyLoad();
        return this.v.lastIndexOf(ch, fromIndex);
    }

    public int avm_indexOf(String str) {
        lazyLoad();
        str.lazyLoad();
        return this.v.indexOf(str.v);
    }

    public int avm_lastIndexOf(String str) {
        lazyLoad();
        str.lazyLoad();
        return this.v.lastIndexOf(str.v);
    }

    public int avm_lastIndexOf(String str, int fromIndex) {
        lazyLoad();
        str.lazyLoad();
        return this.v.lastIndexOf(str.v, fromIndex);
    }

    public String avm_substring(int beginIndex) {
        lazyLoad();
        return new String(this.v.substring(beginIndex));
    }

    public String avm_substring(int beginIndex, int endIndex) {
        lazyLoad();
        return new String(this.v.substring(beginIndex, endIndex));
    }

    public CharSequence avm_subSequence(int beginIndex, int endIndex){
        lazyLoad();
        return this.avm_substring(beginIndex, endIndex);
    }

    public String avm_concat(String str){
        lazyLoad();
        str.lazyLoad();
        return new String(this.v.concat(str.getV()));
    }

    public String avm_replace(char oldChar, char newChar) {
        lazyLoad();
        return new String(this.v.replace(oldChar, newChar));
    }

    public boolean avm_matches(String regex){
        lazyLoad();
        regex.lazyLoad();
        return this.v.matches(regex.v);
    }

    public boolean avm_contains(CharSequence s){
        lazyLoad();
        ((Object)s).lazyLoad();
        return avm_indexOf(s.avm_toString()) >= 0;
    }

    public String avm_replaceFirst(String regex, String replacement){
        lazyLoad();
        regex.lazyLoad();
        replacement.lazyLoad();
        return new String(this.v.replaceFirst(regex.getV(), replacement.getV()));
    }

    public String avm_replaceAll(String regex, String replacement) {
        lazyLoad();
        regex.lazyLoad();
        replacement.lazyLoad();
        return new String(this.v.replaceAll(regex.getV(), replacement.getV()));
    }

    public String avm_replace(CharSequence target, CharSequence replacement){
        lazyLoad();
        ((Object)target).lazyLoad();
        ((Object)replacement).lazyLoad();
        return new String(this.v.replace(target.avm_toString().getV(),
                replacement.avm_toString().getV()));
    }

    //public String[] split(String regex, int limit) {}

    //public String[] split(String regex){}

    public String avm_toLowerCase(){
        lazyLoad();
        return new String(this.v.toLowerCase());
    }

    public String avm_toUpperCase(){
        lazyLoad();
        return new String(this.v.toUpperCase());
    }

    public String avm_trim() {
        lazyLoad();
        return new String(this.v.trim());
    }

    public String avm_toString() {
        return this;
    }

    public CharArray avm_toCharArray() {
        lazyLoad();
        return new CharArray(this.v.toCharArray());
    }

//    public static String avm_format(String format, Object... args) {
//        return new String(java.lang.String.format(format.getV(), args));
//    }

    //TODO: IOBJECT?
    public static String avm_valueOf(Object obj) {
        obj.lazyLoad();
        return new String(java.lang.String.valueOf(obj));
    }

    public static String avm_valueOf(CharArray a){
        a.lazyLoad();
        return new String(java.lang.String.valueOf(a.getUnderlying()));
    }

    public static String avm_valueOf(CharArray data, int offset, int count){
        data.lazyLoad();
        return new String(java.lang.String.valueOf(data.getUnderlying(), offset, count));
    }

    public static String avm_copyValueOf(CharArray data, int offset, int count){
        data.lazyLoad();
        return new String(java.lang.String.copyValueOf(data.getUnderlying(), offset, count));
    }

    public static String avm_copyValueOf(CharArray a){
        a.lazyLoad();
        return new String(java.lang.String.copyValueOf(a.getUnderlying()));
    }

    public static String avm_valueOf(boolean b){
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(char b){
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(int b){
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(long b){
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(float b){
        return new String(java.lang.String.valueOf(b));
    }

    public static String avm_valueOf(double b){
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
    public java.lang.String getV(){
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

}

package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.internal.IObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class String extends Object implements Comparable<String>, CharSequence {

    private Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private java.lang.String v;

    public String() {
        this.v = new java.lang.String();
    }

    public String(String original) {
        this.v = new java.lang.String(original.getUnderlying());
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
        this.v = new java.lang.String(bytes.getUnderlying(), offset, length, charsetName.getUnderlying());
    }

    public String(ByteArray bytes, String charsetName) throws UnsupportedEncodingException {
        this.v = new java.lang.String(bytes.getUnderlying(), charsetName.getUnderlying());
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
        return v.length();
    }

    public boolean avm_isEmpty() {
        return v.isEmpty();
    }

    public char avm_charAt(int index) {
        return this.v.charAt(index);
    }

    public int avm_codePointAt(int index) {
        return this.v.codePointAt(index);
    }

    public int avm_codePointBefore(int index) {
        return this.v.codePointBefore(index);
    }

    public int avm_codePointCount(int beginIndex, int endIndex) {
        return this.v.codePointCount(beginIndex, endIndex);
    }

    public int avm_offsetByCodePoints(int index, int codePointOffset){
        return this.v.offsetByCodePoints(index, codePointOffset);
    }

    public void avm_getChars(int srcBegin, int srcEnd, CharArray dst, int dstBegin) {
        this.v.getChars(srcBegin, srcEnd, dst.getUnderlying(), dstBegin);
    }

    public ByteArray avm_getBytes(String charsetName) throws UnsupportedEncodingException {
        return new ByteArray(this.v.getBytes(charsetName.getUnderlying()));
    }

    public ByteArray avm_getBytes(){
        return new ByteArray(this.v.getBytes());
    }

    //TODO
    public boolean avm_equals(IObject anObject) {
        return anObject instanceof String && this.v.equals(((String) anObject).v);
    }

    public boolean avm_contentEquals(StringBuffer sb) {
        return this.v.contentEquals(sb.getUnderlying());
    }

    public boolean contentEquals(CharSequence cs){
        return this.v.contentEquals(cs.avm_toString().getUnderlying());
    }

    public boolean avm_equalsIgnoreCase(String anotherString) {
        return this.v.equalsIgnoreCase(anotherString.getUnderlying());
    }

    public int avm_compareTo(String anotherString) {
        return this.v.compareTo(anotherString.getUnderlying());
    }

    public int avm_compareToIgnoreCase(String str){
        return this.v.compareToIgnoreCase(str.getUnderlying());
    }

    public boolean avm_regionMatches(int toffset, String other, int ooffset, int len) {
        return this.v.regionMatches(toffset, other.getUnderlying(), ooffset, len);
    }

    public boolean avm_regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        return this.v.regionMatches(ignoreCase, toffset, other.getUnderlying(), ooffset, len);
    }

    public boolean avm_startsWith(String prefix, int toffset) {
        return this.v.startsWith(prefix.getUnderlying(), toffset);
    }

    public boolean avm_startsWith(String prefix) {
        return this.v.startsWith(prefix.getUnderlying());
    }

    public boolean avm_endsWith(String prefix) {
        return this.v.endsWith(prefix.getUnderlying());
    }

    @Override
    public int avm_hashCode() {
        return this.v.hashCode();
    }

    public int avm_indexOf(int ch) {
        return this.v.indexOf(ch);
    }

    public int avm_indexOf(int ch, int fromIndex) {
        return this.v.indexOf(ch, fromIndex);
    }

    public int avm_lastIndexOf(int ch) {
        return this.v.lastIndexOf(ch);
    }

    public int avm_lastIndexOf(int ch, int fromIndex) {
        return this.v.lastIndexOf(ch, fromIndex);
    }

    public int avm_indexOf(String str) {
        return this.v.indexOf(str.v);
    }

    public int avm_lastIndexOf(String str) {
        return this.v.lastIndexOf(str.v);
    }

    public int avm_lastIndexOf(String str, int fromIndex) {
        return this.v.lastIndexOf(str.v, fromIndex);
    }

    public String avm_substring(int beginIndex) {
        return new String(this.v.substring(beginIndex));
    }

    public String avm_substring(int beginIndex, int endIndex) {
        return new String(this.v.substring(beginIndex, endIndex));
    }

    public CharSequence avm_subSequence(int beginIndex, int endIndex){
        return this.avm_substring(beginIndex, endIndex);
    }

    public String avm_concat(String str){
        return new String(this.v.concat(str.getUnderlying()));
    }

    public String avm_replace(char oldChar, char newChar) {
        return new String(this.v.replace(oldChar, newChar));
    }

    public boolean avm_matches(String regex){
        return this.v.matches(regex.v);
    }

    public boolean avm_contains(CharSequence s){
        return avm_indexOf(s.avm_toString()) >= 0;
    }

    public String avm_replaceFirst(String regex, String replacement){
        return new String(this.v.replaceFirst(regex.getUnderlying(), replacement.getUnderlying()));
    }

    public String avm_replaceAll(String regex, String replacement) {
        return new String(this.v.replaceAll(regex.getUnderlying(), replacement.getUnderlying()));
    }

    public String avm_replace(CharSequence target, CharSequence replacement){
        return new String(this.v.replace(target.avm_toString().getUnderlying(),
                replacement.avm_toString().getUnderlying()));
    }

    //public String[] split(String regex, int limit) {}

    //public String[] split(String regex){}

    public String avm_toLowerCase(){
        return new String(this.v.toLowerCase());
    }

    public String avm_toUpperCase(){
        return new String(this.v.toUpperCase());
    }

    public String avm_trim() {
        return new String(this.v.trim());
    }

    public String avm_toString() {
        return this;
    }

    public CharArray avm_toCharArray() {
        return new CharArray(this.v.toCharArray());
    }

//    public static String avm_format(String format, Object... args) {
//        return new String(java.lang.String.format(format.getUnderlying(), args));
//    }

    //TODO: IOBJECT?
    public static String avm_valueOf(Object obj) {
        return new String(java.lang.String.valueOf(obj));
    }

    public static String avm_valueOf(CharArray a){
        return new String(java.lang.String.valueOf(a.getUnderlying()));
    }

    public static String avm_valueOf(CharArray data, int offset, int count){
        return new String(java.lang.String.valueOf(data.getUnderlying(), offset, count));
    }

    public static String avm_copyValueOf(CharArray data, int offset, int count){
        return new String(java.lang.String.copyValueOf(data.getUnderlying(), offset, count));
    }

    public static String avm_copyValueOf(CharArray a){
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

    public String avm_intern(){
        return new String(this.v.intern());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

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
        return this.v;
    }

    //internal
    public java.lang.String getUnderlying(){
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

}

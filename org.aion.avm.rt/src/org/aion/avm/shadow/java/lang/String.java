package org.aion.avm.shadow.java.lang;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.internal.IObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class String extends Object implements Comparable<String> {

    private Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private java.lang.String underlying;

    public String() {
        this.underlying = new java.lang.String();
    }

    public String(String original) {
        this.underlying = new java.lang.String(original.getUnderlying());
    }

    public String(CharArray value) {
        this.underlying = new java.lang.String(value.getUnderlying());
    }

    public String(CharArray value, int offset, int count) {
        this.underlying = new java.lang.String(value.getUnderlying(), offset, count);
    }

    public String(IntArray codePoints, int offset, int count) {
        this.underlying = new java.lang.String(codePoints.getUnderlying(), offset, count);
    }

    public String(ByteArray bytes, int offset, int length, String charsetName) throws UnsupportedEncodingException {
        this.underlying = new java.lang.String(bytes.getUnderlying(), offset, length, charsetName.getUnderlying());
    }

    public String(ByteArray bytes, String charsetName) throws UnsupportedEncodingException {
        this.underlying = new java.lang.String(bytes.getUnderlying(), charsetName.getUnderlying());
    }

    public String(ByteArray bytes, int offset, int length){
        this.underlying = new java.lang.String(bytes.getUnderlying(), offset, length);
    }

    public String(ByteArray bytes){
        this.underlying = new java.lang.String(bytes.getUnderlying());
    }

    public String(StringBuffer buffer){
        this.underlying = new java.lang.String(buffer.getUnderlying());
    }

    public String(StringBuilder builder) {
        this.underlying = new java.lang.String(builder.getUnderlying());
    }

    public int avm_length(){
        return underlying.length();
    }

    public boolean avm_isEmpty() {
        return underlying.isEmpty();
    }

    public char avm_charAt(int index) {
        return this.underlying.charAt(index);
    }

    public int avm_codePointAt(int index) {
        return this.underlying.codePointAt(index);
    }

    public int avm_codePointBefore(int index) {
        return this.underlying.codePointBefore(index);
    }

    public int avm_codePointCount(int beginIndex, int endIndex) {
        return this.underlying.codePointCount(beginIndex, endIndex);
    }

    public int avm_offsetByCodePoints(int index, int codePointOffset){
        return this.underlying.offsetByCodePoints(index, codePointOffset);
    }

    public void avm_getChars(int srcBegin, int srcEnd, CharArray dst, int dstBegin) {
        this.underlying.getChars(srcBegin, srcEnd, dst.getUnderlying(), dstBegin);
    }

    public ByteArray avm_getBytes(String charsetName) throws UnsupportedEncodingException {
        return new ByteArray(this.underlying.getBytes(charsetName.getUnderlying()));
    }

    public ByteArray avm_getBytes(){
        return new ByteArray(this.underlying.getBytes());
    }

    //TODO
    public boolean avm_equals(IObject anObject) {
        return anObject instanceof String && this.underlying.equals(((String) anObject).underlying);
    }

    public boolean avm_contentEquals(StringBuffer sb) {
        return this.underlying.contentEquals(sb.getUnderlying());
    }

    public boolean avm_equalsIgnoreCase(String anotherString) {
        return this.underlying.equalsIgnoreCase(anotherString.getUnderlying());
    }

    public int avm_compareTo(String anotherString) {
        return this.underlying.compareTo(anotherString.getUnderlying());
    }

    public int avm_compareToIgnoreCase(String str){
        return this.underlying.compareToIgnoreCase(str.getUnderlying());
    }

    public boolean avm_regionMatches(int toffset, String other, int ooffset, int len) {
        return this.underlying.regionMatches(toffset, other.getUnderlying(), ooffset, len);
    }

    public boolean avm_regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        return this.underlying.regionMatches(ignoreCase, toffset, other.getUnderlying(), ooffset, len);
    }

    public boolean avm_startsWith(String prefix, int toffset) {
        return this.underlying.startsWith(prefix.getUnderlying(), toffset);
    }

    public boolean avm_startsWith(String prefix) {
        return this.underlying.startsWith(prefix.getUnderlying());
    }

    public boolean avm_endsWith(String prefix) {
        return this.underlying.endsWith(prefix.getUnderlying());
    }

    @Override
    public int avm_hashCode() {
        return this.underlying.hashCode();
    }

    public int avm_indexOf(int ch) {
        return this.underlying.indexOf(ch);
    }

    public int avm_indexOf(int ch, int fromIndex) {
        return this.underlying.indexOf(ch, fromIndex);
    }

    public int avm_lastIndexOf(int ch) {
        return this.underlying.lastIndexOf(ch);
    }

    public int avm_lastIndexOf(int ch, int fromIndex) {
        return this.underlying.lastIndexOf(ch, fromIndex);
    }

    public int avm_indexOf(String str) {
        return this.underlying.indexOf(str.underlying);
    }

    public int avm_lastIndexOf(String str) {
        return this.underlying.lastIndexOf(str.underlying);
    }

    public int avm_lastIndexOf(String str, int fromIndex) {
        return this.underlying.lastIndexOf(str.underlying, fromIndex);
    }

    public String avm_substring(int beginIndex) {
        return new String(this.underlying.substring(beginIndex));
    }

    public String avm_substring(int beginIndex, int endIndex) {
        return new String(this.underlying.substring(beginIndex, endIndex));
    }

    public String avm_concat(String str){
        return new String(this.underlying.concat(str.getUnderlying()));
    }

    public String avm_replace(char oldChar, char newChar) {
        return new String(this.underlying.replace(oldChar, newChar));
    }

    public boolean avm_matches(String regex){
        return this.underlying.matches(regex.underlying);
    }

    public String avm_replaceFirst(String regex, String replacement){
        return new String(this.underlying.replaceFirst(regex.getUnderlying(), replacement.getUnderlying()));
    }

    public String avm_replaceAll(String regex, String replacement) {
        return new String(this.underlying.replaceAll(regex.getUnderlying(), replacement.getUnderlying()));
    }

    //public String[] split(String regex, int limit) {}

    //public String[] split(String regex){}

    public String avm_toLowerCase(){
        return new String(this.underlying.toLowerCase());
    }

    public String avm_toUpperCase(){
        return new String(this.underlying.toUpperCase());
    }

    public String avm_trim() {
        return new String(this.underlying.trim());
    }

    public String avm_toString() {
        return this;
    }

    public CharArray avm_toCharArray() {
        return new CharArray(this.underlying.toCharArray());
    }

    public static String avm_format(String format, Object... args) {
        return new String(java.lang.String.format(format.getUnderlying(), args));
    }

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
        return new String(this.underlying.intern());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    // @Internal
    public String(java.lang.String underlying) {
        this.underlying = underlying;
    }

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

    //internal
    java.lang.String getUnderlying(){
        return underlying;
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

    //public boolean contentEquals(CharSequence cs)

    //public static final Comparator<String> CASE_INSENSITIVE_ORDER

    //public CharSequence subSequence(int beginIndex, int endIndex)

    //public boolean contains(CharSequence s)

    //public String replace(CharSequence target, CharSequence replacement)

    //public static String join(CharSequence delimiter, CharSequence... elements)

    //public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements)

    //public String toLowerCase(Locale locale)

    //public String toUpperCase(Locale locale)

    //public IntStream chars()

    //public IntStream codePoints()

    //public static String format(Locale l, String format, Object... args) {

}

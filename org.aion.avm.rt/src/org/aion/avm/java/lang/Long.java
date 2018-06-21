package org.aion.avm.java.lang;

import org.aion.avm.internal.IObject;

public class Long extends Number implements Comparable<Long>{

    public static final long avm_MIN_VALUE = 0x8000000000000000L;

    public static final long avm_MAX_VALUE = 0x7fffffffffffffffL;

    public static final Class<Long> avm_TYPE = new Class(java.lang.Long.TYPE);

    public static String avm_toString(long i, int radix) {
        return new String(java.lang.Long.toString(i, radix));
    }

    public static String avm_toUnsignedString(long i, int radix){
        return new String(java.lang.Long.toUnsignedString(i, radix));
    }

    public static String avm_toHexString(long i) {
        return new String(java.lang.Long.toHexString(i));
    }

    public static String avm_toOctalString(long i) {
        return new String(java.lang.Long.toOctalString(i));
    }

    public static String avm_toBinaryString(long i) {
        return new String(java.lang.Long.toBinaryString(i));
    }

    public static String avm_toString(long i) {
        return new String(java.lang.Long.toString(i));
    }

    public static String avm_toUnsignedString(long i){
        return new String(java.lang.Long.toUnsignedString(i));
    }

    public static long avm_parseLong(String s, int radix) throws NumberFormatException{
        return java.lang.Long.parseLong(s.getUnderlying(), radix);
    }

    public static long avm_parseLong(String s) throws NumberFormatException {
        return java.lang.Long.parseLong(s.getUnderlying(), 10);
    }

    public static long avm_parseUnsignedLong(String s, int radix) throws NumberFormatException {
        return java.lang.Long.parseUnsignedLong(s.getUnderlying(), radix);
    }



    public static long avm_parseUnsignedLong(String s) throws NumberFormatException {
        return java.lang.Long.parseUnsignedLong(s.getUnderlying(), 10);
    }

    public static Long avm_valueOf(String s, int radix) throws NumberFormatException {
        return avm_valueOf(avm_parseLong(s, radix));
    }

    public static Long avm_valueOf(String s) throws NumberFormatException {
        return avm_valueOf(avm_parseLong(s, 10));
    }

    public static Long avm_valueOf(long l) {
        return new Long(l);
    }

    public static Long avm_decode(String nm) throws NumberFormatException {
        return new Long(java.lang.Long.decode(nm.getUnderlying()).longValue());
    }

    private final long value;

    public Long(long value) {
        this.value = value;
    }

    public Long(String s) throws NumberFormatException {
        this.value = avm_parseLong(s, 10);
    }

    public byte avm_byteValue() {
        return (byte)value;
    }

    public short avm_shortValue() {
        return (short)value;
    }

    public int avm_intValue() {
        return (int)value;
    }

    public long avm_longValue() {
        return value;
    }

    public float avm_floatValue() {
        return (float)value;
    }

    public double avm_doubleValue() {
        return (double)value;
    }

    public String avm_toString() {
        return avm_toString(value);
    }

    public int avm_hashCode() {
        return avm_hashCode(value);
    }

    public static int avm_hashCode(long value) {
        return (int)(value ^ (value >>> 32));
    }

    public boolean avm_equals(IObject obj) {
        if (obj instanceof Long) {
            return value == ((Long)obj).avm_longValue();
        }
        return false;
    }

    public int avm_compareTo(Long anotherLong) {
        return avm_compare(this.value, anotherLong.value);
    }

    public static int avm_compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public static int avm_compareUnsigned(long x, long y) {
        return avm_compare(x + avm_MIN_VALUE, y + avm_MIN_VALUE);
    }

    public static long avm_divideUnsigned(long dividend, long divisor){
        return java.lang.Long.divideUnsigned(dividend, divisor);
    }

    public static long avm_remainderUnsigned(long dividend, long divisor){
        return java.lang.Long.remainderUnsigned(dividend, divisor);
    }

    public static final int avm_SIZE = java.lang.Long.SIZE;

    public static final int avm_BYTES = avm_SIZE / Byte.avm_SIZE;

    public static long avm_highestOneBit(long i) {
        return java.lang.Long.highestOneBit(i);
    }

    public static long avm_lowestOneBit(long i) {
        return java.lang.Long.lowestOneBit(i);
    }

    public static int avm_numberOfLeadingZeros(long i) {
        return java.lang.Long.numberOfLeadingZeros(i);
    }

    public static int avm_numberOfTrailingZeros(long i) {
        return java.lang.Long.numberOfTrailingZeros(i);
    }

    public static int avm_bitCount(long i) {
        return java.lang.Long.bitCount(i);
    }

    public static long avm_rotateLeft(long i, int distance) {
        return (i << distance) | (i >>> -distance);
    }

    public static long avm_rotateRight(long i, int distance) {
        return (i >>> distance) | (i << -distance);
    }

    public static long avm_reverse(long i) {
        return java.lang.Long.reverse(i);
    }

    public static int avm_signum(long i) {
        return (int) ((i >> 63) | (-i >>> 63));
    }

    public static long avm_reverseBytes(long i) {
        return java.lang.Long.reverseBytes(i);
    }

    public static long avm_sum(long a, long b) {
        return a + b;
    }

    public static long avm_max(long a, long b) {
        return Math.avm_max(a, b);
    }

    public static long avm_min(long a, long b) {
        return Math.avm_min(a, b);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    // public static long avm_parseLong(CharSequence s, int beginIndex, int endIndex, int radix)
    //      throws NumberFormatException {}


    // public static long avm_parseUnsignedLong(CharSequence s, int beginIndex, int endIndex, int radix)
    //      throws NumberFormatException {}

    // public static Long avm_getLong(String nm) {}

    // public static Long avm_getLong(String nm, long val) {}

    // public static Long avm_getLong(String nm, Long val) {}

}

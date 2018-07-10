package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IObject;

public class Integer extends Number implements Comparable<Integer> {

    public static final int avm_MAX_VALUE = java.lang.Integer.MAX_VALUE;

    public static final int avm_MIN_VALUE = java.lang.Integer.MIN_VALUE;

    public static final int avm_SIZE = java.lang.Integer.SIZE;

    public static final int avm_BYTES = java.lang.Integer.SIZE;

    public static final Class<Integer> avm_TYPE = new Class(java.lang.Integer.TYPE);

    public static String avm_toString(int i, int radix) {
        return new String(java.lang.Integer.toString(i, radix));
    }

    public static String avm_toUnsignedString(int i, int radix) {
        return new String(java.lang.Integer.toUnsignedString(i, radix));
    }

    public static String avm_toHexString(int i) {
        return new String(java.lang.Integer.toHexString(i));
    }

    public static String avm_toOctalString(int i) {
        return new String(java.lang.Integer.toOctalString(i));
    }

    public static String avm_toBinaryString(int i) {
        return new String(java.lang.Integer.toBinaryString(i));
    }

    public static String avm_toString(int i) {
        return new String(java.lang.Integer.toString(i));
    }

    public static String avm_toUnsignedString(int i) {
        return new String(java.lang.Integer.toUnsignedString(i));
    }

    public static int avm_parseInt(String s, int radix) throws NumberFormatException {
        return java.lang.Integer.parseInt(s.getV(), radix);
    }

    public static int avm_parseInt(String s) throws NumberFormatException {
        return java.lang.Integer.parseInt(s.getV());
    }

    public static int avm_parseInt(CharSequence s, int beginIndex, int endIndex, int radix)
            throws NumberFormatException {
        return java.lang.Integer.parseInt(s.avm_toString().getV(), beginIndex, endIndex, radix);
    }

    public static int avm_parseUnsignedInt(String s, int radix){
        return java.lang.Integer.parseUnsignedInt(s.getV(), radix);
    }

    public static int avm_parseUnsignedInt(String s){
        return java.lang.Integer.parseUnsignedInt(s.getV());
    }

    public static int avm_parseUnsignedInt(CharSequence s, int beginIndex, int endIndex, int radix)
            throws NumberFormatException {
        return java.lang.Integer.parseUnsignedInt(s.avm_toString().getV(), beginIndex, endIndex, radix);
    }

    public static Integer avm_valueOf(String s, int radix) throws NumberFormatException {
        return Integer.avm_valueOf(avm_parseInt(s, radix));
    }

    public static Integer avm_valueOf(String s) throws NumberFormatException {
        return Integer.avm_valueOf(avm_parseInt(s, 10));
    }

    public static Integer avm_valueOf(int i) {
        return new Integer(i);
    }

    public Integer(int v) {
        this.v = v;
    }

    public Integer(String s) throws NumberFormatException {
        this.v = avm_parseInt(s, 10);
    }

    public byte avm_byteValue() {
        return (byte) v;
    }

    public short avm_shortValue() {
        return (short) v;
    }

    public int avm_intValue() {
        return v;
    }

    public long avm_longValue() {
        return (long) v;
    }

    public float avm_floatValue() {
        return (float) v;
    }

    public double avm_doubleValue() {
        return (double) v;
    }

    public String avm_toString() {
        return avm_toString(v);
    }

    public boolean avm_equals(IObject obj) {
        if (obj instanceof Integer) {
            return v == ((Integer)obj).avm_intValue();
        }
        return false;
    }

    public static Integer avm_decode(String nm) throws NumberFormatException {
        return new Integer(java.lang.Integer.decode(nm.getV()).intValue());
    }

    public int avm_compareTo(Integer anotherInteger) {
        return avm_compare(this.v, anotherInteger.v);
    }

    public static int avm_compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public static int avm_compareUnsigned(int x, int y) {
        return avm_compare(x + avm_MIN_VALUE, y + avm_MIN_VALUE);
    }

    public static long avm_toUnsignedLong(int x) {
        return ((long) x) & 0xffffffffL;
    }

    public static int avm_divideUnsigned(int dividend, int divisor) {
        // In lieu of tricky code, for now just use long arithmetic.
        return (int)(avm_toUnsignedLong(dividend) / avm_toUnsignedLong(divisor));
    }

    public static int avm_remainderUnsigned(int dividend, int divisor) {
        // In lieu of tricky code, for now just use long arithmetic.
        return (int)(avm_toUnsignedLong(dividend) % avm_toUnsignedLong(divisor));
    }

    public static int avm_highestOneBit(int i) {
        return java.lang.Integer.highestOneBit(i);
    }

    public static int avm_lowestOneBit(int i) {
        return java.lang.Integer.lowestOneBit(i);
    }

    public static int avm_numberOfLeadingZeros(int i) {
        return java.lang.Integer.numberOfLeadingZeros(i);
    }

    public static int avm_numberOfTrailingZeros(int i) {
        return java.lang.Integer.numberOfTrailingZeros(i);
    }

    public static int avm_bitCount(int i) {
        return java.lang.Integer.bitCount(i);
    }

    public static long avm_reverse(int i) {
        return java.lang.Integer.reverse(i);
    }

    public static int avm_signum(int i) {
        return (i >> 31) | (-i >>> 31);
    }

    public static int avm_reverseBytes(int i) {
        return java.lang.Integer.reverseBytes(i);
    }

    public static int avm_sum(int a, int b) {
        return a + b;
    }

    public static int avm_max(int a, int b) {
        return Math.avm_max(a, b);
    }

    public static int avm_min(int a, int b) {
        return Math.avm_min(a, b);
    }

    //=======================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private final int v;

    @Override
    public boolean equals(java.lang.Object obj) {
        return obj instanceof Integer && this.v == ((Integer) obj).v;
    }

    @Override
    public java.lang.String toString() {
        return java.lang.Integer.toString(this.v);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    // public static Integer avm_getInteger(String nm){}

    // public static Integer avm_getInteger(String nm, int val) {}

    // public static Integer avm_getInteger(String nm, Integer val) {}


}

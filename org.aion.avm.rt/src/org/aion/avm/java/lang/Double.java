package org.aion.avm.java.lang;

public class Double extends Number {
    private double value;

    public Double(double d){
        value = d;
    }

    public static final int avm_BYTES = java.lang.Double.BYTES;

    public static final int avm_MAX_EXPONENT = java.lang.Double.MAX_EXPONENT;

    public static final int avm_MIN_EXPONENT = java.lang.Double.MIN_EXPONENT;

    public static final double avm_MAX_VALUE = java.lang.Double.MAX_VALUE;

    public static final double avm_MIN_VALUE = java.lang.Double.MIN_VALUE;

    public static final double avm_MIN_NORMAL = java.lang.Double.MIN_NORMAL;

    public static final double avm_POSITIVE_INFINITY = java.lang.Double.POSITIVE_INFINITY;

    public static final double avm_NEGATIVE_INFINITY = java.lang.Double.NEGATIVE_INFINITY;

    public static final double avm_NaN = java.lang.Double.NaN;

    public static final int avm_SIZE = java.lang.Double.SIZE;

    // TODO
    public static final Class avm_TYPE = null;


    public static String avm_toHexString(double a)
    {
        return new String(java.lang.Double.toHexString(a));
    }

    public static String avm_toString(double a)
    {
        return new String(java.lang.Double.toString(a));
    }

    public static Double avm_valueOf(String a)
    {
        return new Double(avm_parseDouble(a));
    }

    public static Double avm_valueOf(double origValue) {
        return new Double(origValue);
    }

    public static double avm_parseDouble(String a)
    {
        return java.lang.Double.parseDouble(a.getUnderlying());
    }

    public static boolean avm_isNaN(double v)
    {
        return (v != v);
    }

    public static boolean avm_isInfinite(double v) {
        return (v == avm_POSITIVE_INFINITY) || (v == avm_NEGATIVE_INFINITY);
    }

    public static boolean avm_isFinite(double d) {
        return Math.avm_abs(d) <= Double.avm_MAX_VALUE;
    }

    public boolean avm_isNaN() {
        return avm_isNaN(value);
    }

    public boolean avm_isInfinite() {
        return avm_isInfinite(value);
    }

    public String avm_toString()
    {
        return avm_toString(value);
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
        return (long)value;
    }

    public float avm_floatValue() {
        return (float)value;
    }

    public double avm_doubleValue() {
        return value;
    }

    public int avm_hashCode() {
        return Double.avm_hashCode(value);
    }

    public static int avm_hashCode(double value) {
        return java.lang.Double.hashCode(value);
    }

    public boolean equals(Object obj) {
        return (obj instanceof Double)
                && (avm_doubleToLongBits(((Double)obj).value) ==
                avm_doubleToLongBits(value));
    }

    public static long avm_doubleToLongBits(double value) {
        if (!avm_isNaN(value)) {
            return avm_doubleToRawLongBits(value);
        }
        return 0x7ff8000000000000L;
    }

    public static long avm_doubleToRawLongBits(double value){
        return java.lang.Double.doubleToRawLongBits(value);
    }

    public static double avm_longBitsToDouble(long bits){
        return java.lang.Double.longBitsToDouble(bits);
    }

    public int avm_compareTo(Double anotherDouble) {
        return avm_compare(value, anotherDouble.value);
    }

    public static int avm_compare(double d1, double d2){
        return java.lang.Double.compare(d1, d2);
    }

    public static double avm_sum(double a, double b) {
        return a + b;
    }

    public static double avm_max(double a, double b) {
        return Math.avm_max(a, b);
    }

    public static double avm_min(double a, double b) {
        return Math.avm_min(a, b);
    }


}
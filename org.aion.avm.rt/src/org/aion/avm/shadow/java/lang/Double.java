package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;


public class Double extends Number implements Comparable<Double>{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public Double(double d){
        this.v = d;
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

    public static final Class avm_TYPE = new Class(java.lang.Double.TYPE);

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
        return java.lang.Double.parseDouble(a.getV());
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
        return avm_isNaN(v);
    }

    public boolean avm_isInfinite() {
        return avm_isInfinite(v);
    }

    public String avm_toString()
    {
        return avm_toString(v);
    }

    public byte avm_byteValue() {
        return (byte) v;
    }

    public short avm_shortValue() {
        return (short) v;
    }

    public int avm_intValue() {
        return (int) v;
    }

    public long avm_longValue() {
        return (long) v;
    }

    public float avm_floatValue() {
        return (float) v;
    }

    public double avm_doubleValue() {
        return v;
    }

    public int avm_hashCode() {
        return Double.avm_hashCode(v);
    }

    public static int avm_hashCode(double value) {
        return java.lang.Double.hashCode(value);
    }

    public boolean equals(IObject obj) {
        return (obj instanceof Double)
                && (avm_doubleToLongBits(((Double)obj).v) ==
                avm_doubleToLongBits(v));
    }

    public static long avm_doubleToLongBits(double value) {
        return java.lang.Double.doubleToLongBits(value);
    }

    public static long avm_doubleToRawLongBits(double value){
        return java.lang.Double.doubleToRawLongBits(value);
    }

    public static double avm_longBitsToDouble(long bits){
        return java.lang.Double.longBitsToDouble(bits);
    }

    public int avm_compareTo(Double anotherDouble) {
        return avm_compare(v, anotherDouble.v);
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

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Double(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    private double v;

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}
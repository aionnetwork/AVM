package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;


public class Float extends Number implements Comparable<Float> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final float avm_POSITIVE_INFINITY = java.lang.Float.POSITIVE_INFINITY;

    public static final float avm_NEGATIVE_INFINITY = java.lang.Float.NEGATIVE_INFINITY;

    public static final float avm_NaN = java.lang.Float.NaN;

    public static final float avm_MAX_VALUE = java.lang.Float.MAX_VALUE;

    public static final float avm_MIN_NORMAL = java.lang.Float.MIN_NORMAL;

    public static final float avm_MIN_VALUE = java.lang.Float.MIN_VALUE;

    public static final int avm_MAX_EXPONENT = java.lang.Float.MAX_EXPONENT;

    public static final int avm_MIN_EXPONENT = java.lang.Float.MIN_EXPONENT;

    public static final int avm_SIZE = java.lang.Float.SIZE;

    public static final int avm_BYTES = java.lang.Float.BYTES;

    public static final Class<Float> avm_TYPE = new Class(java.lang.Float.TYPE);

    public Float(float f){
        this.v = f;
    }

    public static String avm_toString(float f){
        return new String(java.lang.Float.toString(f));
    }

    public static String avm_toHexString(float a){
        return new String(java.lang.Float.toHexString(a));
    }

    public static Float avm_valueOf(String s) throws NumberFormatException {
        return new Float(avm_parseFloat(s));
    }

    public static Float avm_valueOf(float f) {
        return new Float(f);
    }

    public static float avm_parseFloat(String s) throws NumberFormatException {
        return java.lang.Float.parseFloat(s.getUnderlying());
    }

    public static boolean avm_isNaN(float v) {
        return (v != v);
    }

    public static boolean avm_isInfinite(float v) {
        return (v == avm_POSITIVE_INFINITY) || (v == avm_NEGATIVE_INFINITY);
    }

    public static boolean avm_isFinite(float f) {
        return Math.avm_abs(f) <= Float.avm_MAX_VALUE;
    }

    public boolean avm_isNaN() {
        return avm_isNaN(v);
    }

    public boolean avm_isInfinite() {
        return avm_isInfinite(v);
    }

    public String avm_toString() {
        return Float.avm_toString(v);
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
        return v;
    }

    public double avm_doubleValue() {
        return (double) v;
    }

    public int avm_hashCode() {
        return Float.avm_hashCode(v);
    }

    public static int avm_hashCode(float value) {
        return avm_floatToIntBits(value);
    }

    public boolean avm_equals(IObject obj) {
        return (obj instanceof Float)
                && (avm_floatToIntBits(((Float)obj).v) == avm_floatToIntBits(v));
    }

    public static int avm_floatToIntBits(float value) {
        return java.lang.Float.floatToIntBits(value);
    }

    public static int avm_floatToRawIntBits(float value){
        return java.lang.Float.floatToRawIntBits(value);
    }

    public static float avm_intBitsToFloat(int bits){
        return java.lang.Float.intBitsToFloat(bits);
    }

    public int avm_compareTo(Float anotherFloat) {
        return Float.avm_compare(v, anotherFloat.v);
    }

    public static int avm_compare(float f1, float f2) {
        return java.lang.Float.compare(f1, f2);
    }

    public static float avm_sum(float a, float b) {
        return a + b;
    }

    public static float avm_max(float a, float b) {
        return Math.avm_max(a, b);
    }

    public static float avm_min(float a, float b) {
        return Math.avm_min(a, b);
    }


    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Float(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    private float v;

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================


}

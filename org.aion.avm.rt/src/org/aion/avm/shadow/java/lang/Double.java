package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.RuntimeMethodFeeSchedule;


public class Double extends Number implements Comparable<Double>{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    // These are the constructors provided in the JDK but we mark them private since they are deprecated.
    // (in the future, we may change these to not exist - depends on the kind of error we want to give the user).
    private Double(double d){
        this.v = d;
    }
    @SuppressWarnings("unused")
    private Double(String s){
        throw RuntimeAssertionError.unimplemented("This is only provided for a consistent error to user code - not to be called");
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
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_toHexString);
        return new String(java.lang.Double.toHexString(a));
    }

    public static String avm_toString(double a)
    {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_toString);
        return new String(java.lang.Double.toString(a));
    }

    public static Double avm_valueOf(String a)
    {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_valueOf);
        return new Double(avm_parseDouble(a));
    }

    public static Double avm_valueOf(double origValue) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_valueOf_1);
        return new Double(origValue);
    }

    public static double avm_parseDouble(String a)
    {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_parseDouble);
        return java.lang.Double.parseDouble(a.getUnderlying());
    }

    public static boolean avm_isNaN(double v)
    {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_isNaN);
        return (v != v);
    }

    public static boolean avm_isInfinite(double v) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_isInfinite);
        return (v == avm_POSITIVE_INFINITY) || (v == avm_NEGATIVE_INFINITY);
    }

    public static boolean avm_isFinite(double d) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_isFinite);
        return Math.avm_abs(d) <= Double.avm_MAX_VALUE;
    }

    public boolean avm_isNaN() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_isNaN_1);
        return avm_isNaN(v);
    }

    public boolean avm_isInfinite() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_isInfinite_1);
        return avm_isInfinite(v);
    }

    public String avm_toString()
    {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_toString_1);
        return avm_toString(v);
    }

    public byte avm_byteValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_byteValue);
        return (byte) v;
    }

    public short avm_shortValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_shortValue);
        return (short) v;
    }

    public int avm_intValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_intValue);
        return (int) v;
    }

    public long avm_longValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_longValue);
        return (long) v;
    }

    public float avm_floatValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_floatValue);
        return (float) v;
    }

    public double avm_doubleValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_doubleValue);
        return v;
    }

    public int avm_hashCode() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_hashCode);
        return Double.avm_hashCode(v);
    }

    public static int avm_hashCode(double value) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_hashCode_1);
        return java.lang.Double.hashCode(value);
    }

    public boolean equals(IObject obj) {
        return (obj instanceof Double)
                && (avm_doubleToLongBits(((Double)obj).v) ==
                avm_doubleToLongBits(v));
    }

    public static long avm_doubleToLongBits(double value) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_doubleToLongBits);
        return java.lang.Double.doubleToLongBits(value);
    }

    public static long avm_doubleToRawLongBits(double value){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_doubleToRawLongBits);
        return java.lang.Double.doubleToRawLongBits(value);
    }

    public static double avm_longBitsToDouble(long bits){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_longBitsToDouble);
        return java.lang.Double.longBitsToDouble(bits);
    }

    public int avm_compareTo(Double anotherDouble) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_compareTo);
        return avm_compare(v, anotherDouble.v);
    }

    public static int avm_compare(double d1, double d2){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_compare);
        return java.lang.Double.compare(d1, d2);
    }

    public static double avm_sum(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_sum);
        return a + b;
    }

    public static double avm_max(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_max);
        return Math.avm_max(a, b);
    }

    public static double avm_min(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Double_avm_min);
        return Math.avm_min(a, b);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Double(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
        lazyLoad();
    }

    private double v;

    public double getUnderlying() {
        return this.v;
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

}

package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class Integer extends Number implements Comparable<Integer> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final int avm_MAX_VALUE = java.lang.Integer.MAX_VALUE;

    public static final int avm_MIN_VALUE = java.lang.Integer.MIN_VALUE;

    public static final int avm_SIZE = java.lang.Integer.SIZE;

    public static final int avm_BYTES = java.lang.Integer.SIZE;

    public static final Class<Integer> avm_TYPE = new Class(java.lang.Integer.TYPE);

    public static String avm_toString(int i, int radix) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_toString);
        return new String(java.lang.Integer.toString(i, radix));
    }

    public static String avm_toUnsignedString(int i, int radix) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_toUnsignedString);
        return new String(java.lang.Integer.toUnsignedString(i, radix));
    }

    public static String avm_toHexString(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_toHexString);
        return new String(java.lang.Integer.toHexString(i));
    }

    public static String avm_toOctalString(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_toOctalString);
        return new String(java.lang.Integer.toOctalString(i));
    }

    public static String avm_toBinaryString(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_toBinaryString);
        return new String(java.lang.Integer.toBinaryString(i));
    }

    public static String avm_toString(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_toString_1);
        return new String(java.lang.Integer.toString(i));
    }

    public static String avm_toUnsignedString(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_toUnsignedString_1);
        return new String(java.lang.Integer.toUnsignedString(i));
    }

    public static int avm_parseInt(String s, int radix) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_parseInt);
        return java.lang.Integer.parseInt(s.getUnderlying(), radix);
    }

    public static int avm_parseInt(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_parseInt_1);
        return java.lang.Integer.parseInt(s.getUnderlying());
    }

    public static int avm_parseInt(CharSequence s, int beginIndex, int endIndex, int radix)
            throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_parseInt_2);
        return java.lang.Integer.parseInt(s.avm_toString().getUnderlying(), beginIndex, endIndex, radix);
    }

    public static int avm_parseUnsignedInt(String s, int radix){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_parseUnsignedInt);
        return java.lang.Integer.parseUnsignedInt(s.getUnderlying(), radix);
    }

    public static int avm_parseUnsignedInt(String s){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_parseUnsignedInt_1);
        return java.lang.Integer.parseUnsignedInt(s.getUnderlying());
    }

    public static int avm_parseUnsignedInt(CharSequence s, int beginIndex, int endIndex, int radix)
            throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_parseUnsignedInt_2);
        return java.lang.Integer.parseUnsignedInt(s.avm_toString().getUnderlying(), beginIndex, endIndex, radix);
    }

    public static Integer avm_valueOf(String s, int radix) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_valueOf);
        return Integer.avm_valueOf(avm_parseInt(s, radix));
    }

    public static Integer avm_valueOf(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_valueOf_1);
        return Integer.avm_valueOf(avm_parseInt(s, 10));
    }

    public static Integer avm_valueOf(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_valueOf_2);
        return new Integer(i);
    }

    public Integer(int v) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_constructor);
        this.v = v;
    }

    public Integer(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_constructor_1);
        this.v = avm_parseInt(s, 10);
    }

    public byte avm_byteValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_byteValue);
        return (byte) v;
    }

    public short avm_shortValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_shortValue);
        return (short) v;
    }

    public int avm_intValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_intValue);
        return v;
    }

    public long avm_longValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_longValue);
        return (long) v;
    }

    public float avm_floatValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_floatValue);
        return (float) v;
    }

    public double avm_doubleValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_doubleValue);
        return (double) v;
    }

    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_toString_2);
        return avm_toString(v);
    }

    @Override
    public int avm_hashCode() {
        return Integer.avm_hashCode(v);
    }

    public static int avm_hashCode(int value) {
        return value;
    }

    public boolean avm_equals(IObject obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_equals);
        if (obj instanceof Integer) {
            return v == ((Integer)obj).avm_intValue();
        }
        return false;
    }

    public static Integer avm_decode(String nm) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_decode);
        return new Integer(java.lang.Integer.decode(nm.getUnderlying()).intValue());
    }

    public int avm_compareTo(Integer anotherInteger) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_compareTo);
        return avm_compare(this.v, anotherInteger.v);
    }

    public static int avm_compare(int x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_compare);
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public static int avm_compareUnsigned(int x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_compareUnsigned);
        return avm_compare(x + avm_MIN_VALUE, y + avm_MIN_VALUE);
    }

    public static long avm_toUnsignedLong(int x) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_toUnsignedLong);
        return ((long) x) & 0xffffffffL;
    }

    public static int avm_divideUnsigned(int dividend, int divisor) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_divideUnsigned);
        // In lieu of tricky code, for now just use long arithmetic.
        return (int)(avm_toUnsignedLong(dividend) / avm_toUnsignedLong(divisor));
    }

    public static int avm_remainderUnsigned(int dividend, int divisor) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_remainderUnsigned);
        // In lieu of tricky code, for now just use long arithmetic.
        return (int)(avm_toUnsignedLong(dividend) % avm_toUnsignedLong(divisor));
    }

    public static int avm_highestOneBit(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_highestOneBit);
        return java.lang.Integer.highestOneBit(i);
    }

    public static int avm_lowestOneBit(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_lowestOneBit);
        return java.lang.Integer.lowestOneBit(i);
    }

    public static int avm_numberOfLeadingZeros(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_numberOfLeadingZeros);
        return java.lang.Integer.numberOfLeadingZeros(i);
    }

    public static int avm_numberOfTrailingZeros(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_numberOfTrailingZeros);
        return java.lang.Integer.numberOfTrailingZeros(i);
    }

    public static int avm_bitCount(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_bitCount);
        return java.lang.Integer.bitCount(i);
    }

    public static long avm_reverse(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_reverse);
        return java.lang.Integer.reverse(i);
    }

    public static int avm_signum(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_signum);
        return (i >> 31) | (-i >>> 31);
    }

    public static int avm_reverseBytes(int i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_reverseBytes);
        return java.lang.Integer.reverseBytes(i);
    }

    public static int avm_sum(int a, int b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_sum);
        return a + b;
    }

    public static int avm_max(int a, int b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_max);
        return Math.avm_max(a, b);
    }

    public static int avm_min(int a, int b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Integer_avm_min);
        return Math.avm_min(a, b);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Integer(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    private int v;

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

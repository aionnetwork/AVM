package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class Long extends Number implements Comparable<Long> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final long avm_MIN_VALUE = 0x8000000000000000L;

    public static final long avm_MAX_VALUE = 0x7fffffffffffffffL;

    public static final Class<Long> avm_TYPE = new Class(java.lang.Long.TYPE);

    public static String avm_toString(long i, int radix) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_toString);
        return new String(java.lang.Long.toString(i, radix));
    }

    public static String avm_toUnsignedString(long i, int radix){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_toUnsignedString);
        return new String(java.lang.Long.toUnsignedString(i, radix));
    }

    public static String avm_toHexString(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_toHexString);
        return new String(java.lang.Long.toHexString(i));
    }

    public static String avm_toOctalString(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_toOctalString);
        return new String(java.lang.Long.toOctalString(i));
    }

    public static String avm_toBinaryString(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_toBinaryString);
        return new String(java.lang.Long.toBinaryString(i));
    }

    public static String avm_toString(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_toString_1);
        return new String(java.lang.Long.toString(i));
    }

    public static String avm_toUnsignedString(long i){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_toUnsignedString_1);
        return new String(java.lang.Long.toUnsignedString(i));
    }

    public static long avm_parseLong(String s, int radix) throws NumberFormatException{
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_parseLong);
        return java.lang.Long.parseLong(s.getUnderlying(), radix);
    }

    public static long avm_parseLong(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_parseLong_1);
        return java.lang.Long.parseLong(s.getUnderlying(), 10);
    }

    public static long avm_parseLong(CharSequence s, int beginIndex, int endIndex, int radix)
            throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_parseLong_2);
        return java.lang.Long.parseLong(s.avm_toString().getUnderlying(), beginIndex, endIndex, radix);
    }

    public static long avm_parseUnsignedLong(String s, int radix) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_parseUnsignedLong);
        return java.lang.Long.parseUnsignedLong(s.getUnderlying(), radix);
    }

    public static long avm_parseUnsignedLong(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_parseUnsignedLong_1);
        return java.lang.Long.parseUnsignedLong(s.getUnderlying(), 10);
    }

    public static long avm_parseUnsignedLong(CharSequence s, int beginIndex, int endIndex, int radix)
            throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_parseUnsignedLong_2);
        return java.lang.Long.parseUnsignedLong(s.avm_toString().getUnderlying(), beginIndex, endIndex, radix);
    }

    public static Long avm_valueOf(String s, int radix) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_valueOf);
        return avm_valueOf(avm_parseLong(s, radix));
    }

    public static Long avm_valueOf(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_valueOf_1);
        return avm_valueOf(avm_parseLong(s, 10));
    }

    public static Long avm_valueOf(long l) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_valueOf_2);
        return new Long(l);
    }

    public static Long avm_decode(String nm) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_decode);
        return new Long(java.lang.Long.decode(nm.getUnderlying()).longValue());
    }

    public Long(long v) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_constructor);
        this.v = v;
    }

    public Long(String s) throws NumberFormatException {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_constructor_1);
        this.v = avm_parseLong(s, 10);
    }

    public byte avm_byteValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_byteValue);
        return (byte) v;
    }

    public short avm_shortValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_shortValue);
        return (short) v;
    }

    public int avm_intValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_intValue);
        return (int) v;
    }

    public long avm_longValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_longValue);
        return v;
    }

    public float avm_floatValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_floatValue);
        return (float) v;
    }

    public double avm_doubleValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_doubleValue);
        return (double) v;
    }

    public String avm_toString() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_toString_2);
        return avm_toString(v);
    }

    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_hashCode);
        return avm_hashCode(v);
    }

    public static int avm_hashCode(long value) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_hashCode_1);
        return (int)(value ^ (value >>> 32));
    }

    public boolean avm_equals(IObject obj) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_equals);
        if (obj instanceof Long) {
            return v == ((Long)obj).avm_longValue();
        }
        return false;
    }

    public int avm_compareTo(Long anotherLong) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_compareTo);
        return avm_compare(this.v, anotherLong.v);
    }

    public static int avm_compare(long x, long y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_compare);
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public static int avm_compareUnsigned(long x, long y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_compareUnsigned);
        return avm_compare(x + avm_MIN_VALUE, y + avm_MIN_VALUE);
    }

    public static long avm_divideUnsigned(long dividend, long divisor){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_divideUnsigned);
        return java.lang.Long.divideUnsigned(dividend, divisor);
    }

    public static long avm_remainderUnsigned(long dividend, long divisor){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_remainderUnsigned);
        return java.lang.Long.remainderUnsigned(dividend, divisor);
    }

    public static final int avm_SIZE = java.lang.Long.SIZE;

    public static final int avm_BYTES = avm_SIZE / Byte.avm_SIZE;

    public static long avm_highestOneBit(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_highestOneBit);
        return java.lang.Long.highestOneBit(i);
    }

    public static long avm_lowestOneBit(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_lowestOneBit);
        return java.lang.Long.lowestOneBit(i);
    }

    public static int avm_numberOfLeadingZeros(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_numberOfLeadingZeros);
        return java.lang.Long.numberOfLeadingZeros(i);
    }

    public static int avm_numberOfTrailingZeros(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_numberOfTrailingZeros);
        return java.lang.Long.numberOfTrailingZeros(i);
    }

    public static int avm_bitCount(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_bitCount);
        return java.lang.Long.bitCount(i);
    }

    public static long avm_rotateLeft(long i, int distance) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_rotateLeft);
        return (i << distance) | (i >>> -distance);
    }

    public static long avm_rotateRight(long i, int distance) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_rotateRight);
        return (i >>> distance) | (i << -distance);
    }

    public static long avm_reverse(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_reverse);
        return java.lang.Long.reverse(i);
    }

    public static int avm_signum(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_signum);
        return (int) ((i >> 63) | (-i >>> 63));
    }

    public static long avm_reverseBytes(long i) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_reverseBytes);
        return java.lang.Long.reverseBytes(i);
    }

    public static long avm_sum(long a, long b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_sum);
        return a + b;
    }

    public static long avm_max(long a, long b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_max);
        return Math.avm_max(a, b);
    }

    public static long avm_min(long a, long b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.Long_avm_min);
        return Math.avm_min(a, b);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    public Long(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
    }

    private long v;

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    // public static Long avm_getLong(String nm) {}

    // public static Long avm_getLong(String nm, long val) {}

    // public static Long avm_getLong(String nm, Long val) {}

}

package org.aion.avm.shadow.java.math;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.CodecIdioms;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.shadow.java.lang.Comparable;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Number;

import org.aion.avm.RuntimeMethodFeeSchedule;

public class BigInteger extends Number implements Comparable<BigInteger> {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public BigInteger(ByteArray val, int off, int len) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_constructor);
        v = new java.math.BigInteger(val.getUnderlying(), off, len);
    }

    public BigInteger(ByteArray val) {
        this(val, 0, val.length());
    }

    public BigInteger(int signum, ByteArray magnitude, int off, int len){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_constructor_2);
        v = new java.math.BigInteger(signum, magnitude.getUnderlying(), off, len);
    }

    public BigInteger(int signum, ByteArray magnitude){
        this(signum, magnitude, 0, magnitude.length());
    }

    public BigInteger(String val, int radix) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_constructor_4);
        v = new java.math.BigInteger(val.getUnderlying(), radix);
    }

    public BigInteger(String val) {
        this(val, 10);
    }

    public BigInteger avm_nextProbablePrime(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_nextProbablePrime);
        return new BigInteger(this.v.nextProbablePrime());
    }

    public static BigInteger avm_valueOf(long val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_valueOf);
        return new BigInteger(java.math.BigInteger.valueOf(val));
    }

    public static final BigInteger avm_ZERO = new BigInteger(java.math.BigInteger.ZERO);

    public static final BigInteger avm_ONE = new BigInteger(java.math.BigInteger.ONE);

    public static final BigInteger avm_TWO = new BigInteger(java.math.BigInteger.TWO);

    public static final BigInteger avm_TEN = new BigInteger(java.math.BigInteger.TEN);

    public BigInteger avm_add(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_add);
        return new BigInteger(v.add(val.v));
    }

    public BigInteger avm_subtract(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_subtract);
        return new BigInteger(v.subtract(val.v));
    }

    public BigInteger avm_multiply(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_multiply);
        return new BigInteger(v.multiply(val.v));
    }

    public BigInteger avm_divide(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_divide);
        return new BigInteger(v.divide(val.v));
    }

    public BigInteger avm_remainder(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_remainder);
        return new BigInteger(v.remainder(val.v));
    }

    public BigInteger avm_pow(int exponent) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_pow);
        return new BigInteger(v.pow(exponent));
    }

    public BigInteger avm_sqrt() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_sqrt);
        return new BigInteger(v.sqrt());
    }

    public BigInteger avm_gcd(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_gcd);
        return new BigInteger(v.gcd(val.v));
    }

    public BigInteger avm_abs() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_abs);
        return new BigInteger(v.abs());
    }

    public BigInteger avm_negate() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_negate);
        return new BigInteger(v.negate());
    }

    public int avm_signum() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_signum);
        return v.signum();
    }

    public BigInteger avm_mod(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_mod);
        return new BigInteger(v.mod(val.v));
    }

    public BigInteger avm_modPow(BigInteger exponent, BigInteger m) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_modPow);
        return new BigInteger(v.modPow(exponent.v, m.v));
    }

    public BigInteger avm_modInverse(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_modInverse);
        return new BigInteger(v.modInverse(val.v));
    }

    public BigInteger avm_shiftLeft(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_shiftLeft);
        return new BigInteger(v.shiftLeft(n));
    }

    public BigInteger avm_shiftRight(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_shiftRight);
        return new BigInteger(v.shiftRight(n));
    }

    public BigInteger avm_and(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_and);
        return new BigInteger(v.and(val.v));
    }

    public BigInteger avm_or(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_or);
        return new BigInteger(v.or(val.v));
    }

    public BigInteger avm_xor(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_xor);
        return new BigInteger(v.xor(val.v));
    }

    public BigInteger avm_not() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_not);
        return new BigInteger(v.not());
    }

    public BigInteger avm_andNot(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_andNot);
        return new BigInteger(v.andNot(val.v));
    }

    public boolean avm_testBit(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_testBit);
        return v.testBit(n);
    }

    public BigInteger avm_setBit(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_setBit);
        return new BigInteger(v.setBit(n));
    }

    public BigInteger avm_clearBit(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_clearBit);
        return new BigInteger(v.clearBit(n));
    }

    public BigInteger avm_flipBit(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_flipBit);
        return new BigInteger(v.flipBit(n));
    }

    public int avm_getLowestSetBit() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_getLowestSetBit);
        return v.getLowestSetBit();
    }

    public int avm_bitLength() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_bitLength);
        return v.bitLength();
    }

    public int avm_bitCount() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_bitCount);
        return v.bitLength();
    }

    public int avm_compareTo(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_compareTo);
        return v.compareTo(val.v);
    }

    public boolean avm_equals(IObject x) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_equals);
        if (x == this)
            return true;

        if (!(x instanceof BigInteger))
            return false;

        BigInteger xInt = (BigInteger) x;
        return v.equals(xInt.v);
    }

    public BigInteger avm_min(BigInteger val){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_min);
        return new BigInteger(v.min(val.v));
    }

    public BigInteger avm_max(BigInteger val){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_max);
        return new BigInteger(v.max(val.v));
    }

    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_hashCode);
        return v.hashCode();
    }

    public String avm_toString(int radix){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_toString);
        return new String(v.toString(radix));
    }

    public String avm_toString(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_toString_1);
        return new String(v.toString());
    }

    public ByteArray avm_toByteArray() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_toByteArray);
        return new ByteArray(v.toByteArray());
    }

    public int avm_intValue(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_intValue);
        return v.intValue();
    }

    public long avm_longValue(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_longValue);
        return v.longValue();
    }

    public float avm_floatValue(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_floatValue);
        return v.floatValue();
    }

    public double avm_doubleValue(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_doubleValue);
        return v.doubleValue();
    }

    public long avm_longValueExact(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_longValueExact);
        return v.longValueExact();
    }

    public int avm_intValueExact() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_intValueExact);
        return v.intValueExact();
    }

    public short avm_shortValueExact() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_shortValueExact);
        return v.shortValueExact();
    }

    public byte avm_byteValueExact() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_byteValueExact);
        return v.byteValueExact();
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.math.BigInteger v;

    public BigInteger(java.math.BigInteger u) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigInteger_avm_constructor_6);
        v = u;
    }

    public java.math.BigInteger getUnderlying() {
        return v;
    }

    // Deserializer support.
    public BigInteger(IDeserializer deserializer, long instanceId) {
        super(deserializer, instanceId);
        lazyLoad();
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(BigInteger.class, deserializer);
        
        // We can deserialize this as its actual 2s compliment byte array.
        this.v = new java.math.BigInteger(CodecIdioms.deserializeByteArray(deserializer));
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(String.class, serializer);
        
        // We can serialize this as its actual 2s compliment byte array.
        CodecIdioms.serializeByteArray(serializer, this.v.toByteArray());
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public BigInteger(int numBits, Random rnd)

    //public BigInteger(int bitLength, int certainty, Random rnd)

    //public static BigInteger probablePrime(int bitLength, Random rnd)

    //private static BigInteger smallPrime(int bitLength, int certainty, Random rnd)

    //private static BigInteger largePrime(int bitLength, int certainty, Random rnd)

    //public BigInteger[] divideAndRemainder(BigInteger val)

    //public BigInteger[] sqrtAndRemainder()

    //public boolean isProbablePrime(int certainty)



}

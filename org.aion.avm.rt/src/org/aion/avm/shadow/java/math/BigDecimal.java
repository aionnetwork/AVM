package org.aion.avm.shadow.java.math;

import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.CodecIdioms;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IObject;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.shadow.java.lang.Comparable;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Number;
import org.aion.avm.RuntimeMethodFeeSchedule;


public class BigDecimal extends Number implements Comparable<BigDecimal>{
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    public static final BigDecimal avm_ZERO;

    public static final BigDecimal avm_ONE;

    public static final BigDecimal avm_TEN;

    public BigDecimal(CharArray in, int offset, int len){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor);
        v = new java.math.BigDecimal(in.getUnderlying(), offset, len);
    }

    public BigDecimal(CharArray in, int offset, int len, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_1);
        v = new java.math.BigDecimal(in.getUnderlying(), offset, len,
                mc.getUnderlying());
    }

    public BigDecimal(CharArray in){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_2);
        v = new java.math.BigDecimal(in.getUnderlying());
    }

    public BigDecimal(CharArray in, MathContext mc){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_3);
        v = new java.math.BigDecimal(in.getUnderlying(), mc.getUnderlying());
    }

    public BigDecimal(String val){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_4);
        v = new java.math.BigDecimal(val.getUnderlying());
    }

    public BigDecimal(String val, MathContext mc){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_5);
        v = new java.math.BigDecimal(val.getUnderlying(), mc.getUnderlying());
    }

    public BigDecimal(double val){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_6);
        v = new java.math.BigDecimal(val);
    }

    public BigDecimal(double val, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_7);
        v = new java.math.BigDecimal(val, mc.getUnderlying());
    }

    public BigDecimal(BigInteger val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_8);
        v = new java.math.BigDecimal(val.getUnderlying());
    }

    public BigDecimal(BigInteger val, MathContext mc){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_9);
        v = new java.math.BigDecimal(val.getUnderlying(), mc.getUnderlying());
    }

    public BigDecimal(BigInteger unscaledVal, int scale) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_10);
        v = new java.math.BigDecimal(unscaledVal.getUnderlying(), scale);
    }

    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_11);
        v = new java.math.BigDecimal(unscaledVal.getUnderlying(), scale, mc.getUnderlying());
    }

    public BigDecimal(int val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_12);
        v = new java.math.BigDecimal(val);
    }

    public BigDecimal(int val, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_13);
        v = new java.math.BigDecimal(val, mc.getUnderlying());
    }

    public BigDecimal(long val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_14);
        v = new java.math.BigDecimal(val);
    }

    public BigDecimal(long val, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_15);
        v = new java.math.BigDecimal(val, mc.getUnderlying());
    }

    public static BigDecimal avm_valueOf(long unscaledVal, int scale) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_valueOf);
        return new BigDecimal(java.math.BigDecimal.valueOf(unscaledVal, scale));
    }

    public static BigDecimal avm_valueOf(long val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_valueOf_1);
        return new BigDecimal(java.math.BigDecimal.valueOf(val));
    }

    public static BigDecimal avm_valueOf(double val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_valueOf_2);
        return new BigDecimal(java.math.BigDecimal.valueOf(val));
    }

    public BigDecimal avm_add(BigDecimal augend) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_add);
        return new BigDecimal(v.add(augend.v));
    }

    public BigDecimal add(BigDecimal augend, MathContext mc) {
        return new BigDecimal(v.add(augend.v, mc.getUnderlying()));
    }

    public BigDecimal avm_subtract(BigDecimal subtrahend) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_subtract);
        return new BigDecimal(v.subtract(subtrahend.v));
    }

    public BigDecimal avm_subtract(BigDecimal subtrahend, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_subtract_1);
        return new BigDecimal(v.subtract(subtrahend.v, mc.getUnderlying()));
    }

    public BigDecimal avm_multiply(BigDecimal multiplicand) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_multiply);
        return new BigDecimal(v.multiply(multiplicand.v));
    }

    public BigDecimal avm_multiply(BigDecimal multiplicand, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_multiply_1);
        return new BigDecimal(v.multiply(multiplicand.v, mc.getUnderlying()));
    }

    public BigDecimal avm_divide(BigDecimal divisor, int scale, RoundingMode roundingMode){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divide);
        return new BigDecimal(v.divide(divisor.v, scale, roundingMode.getUnderlying()));
    }

    public BigDecimal avm_divide(BigDecimal divisor, RoundingMode roundingMode) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divide_1);
        return new BigDecimal(v.divide(divisor.v, roundingMode.getUnderlying()));
    }

    public BigDecimal avm_divide(BigDecimal divisor) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divide_2);
        return new BigDecimal(v.divide(divisor.v));
    }

    public BigDecimal avm_divide(BigDecimal divisor, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divide_3);
        return new BigDecimal(v.divide(divisor.v, mc.getUnderlying()));
    }

    public BigDecimal avm_divideToIntegralValue(BigDecimal divisor) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divideToIntegralValue);
        return new BigDecimal(v.divideToIntegralValue(divisor.v));
    }

    public BigDecimal avm_divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divideToIntegralValue_1);
        return new BigDecimal(v.divideToIntegralValue(divisor.v, mc.getUnderlying()));
    }

    public BigDecimal avm_remainder(BigDecimal divisor) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_remainder);
        return new BigDecimal(v.remainder(divisor.v));
    }

    public BigDecimal avm_remainder(BigDecimal divisor, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_remainder_1);
        return new BigDecimal(v.remainder(divisor.v, mc.getUnderlying()));
    }

    public BigDecimal avm_sqrt(MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_sqrt);
        return new BigDecimal(v.sqrt(mc.getUnderlying()));
    }

    public BigDecimal avm_pow(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_pow);
        return new BigDecimal(v.pow(n));
    }

    public BigDecimal avm_pow(int n, MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_pow_1);
        return new BigDecimal(v.pow(n, mc.getUnderlying()));
    }

    public BigDecimal avm_abs(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_abs);
        return new BigDecimal(v.abs());
    }

    public BigDecimal avm_abs(MathContext mc){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_abs_1);
        return new BigDecimal(v.abs(mc.getUnderlying()));
    }

    public BigDecimal avm_negate() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_negate);
        return new BigDecimal(v.negate());
    }

    public BigDecimal avm_negate(MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_negate_1);
        return new BigDecimal(v.negate(mc.getUnderlying()));
    }

    public BigDecimal avm_plus() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_plus);
        return new BigDecimal(v.negate());
    }

    public BigDecimal avm_plus(MathContext mc) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_plus_1);
        return new BigDecimal(v.negate(mc.getUnderlying()));
    }

    public int avm_signum() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_signum);
        return v.signum();
    }

    public int avm_scale() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_scale);
        return v.scale();
    }

    public int avm_precision() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_precision);
        return v.precision();
    }

    public BigInteger avm_unscaledValue() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_unscaledValue);
        return new BigInteger(v.unscaledValue());
    }

    public BigDecimal avm_round(MathContext mc){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_round);
        return new BigDecimal(v.round(mc.getUnderlying()));
    }

    public BigDecimal avm_setScale(int newScale, RoundingMode roundingMode) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_setScale);
        return new BigDecimal(v.setScale(newScale, roundingMode.getUnderlying()));
    }

    public BigDecimal avm_setScale(int newScale){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_setScale_1);
        return new BigDecimal(v.setScale(newScale));
    }

    public BigDecimal avm_movePointLeft(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_movePointLeft);
        return new BigDecimal(v.movePointLeft(n));
    }

    public BigDecimal avm_movePointRight(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_movePointRight);
        return new BigDecimal(v.movePointRight(n));
    }

    public BigDecimal avm_scaleByPowerOfTen(int n) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_scaleByPowerOfTen);
        return new BigDecimal(v.scaleByPowerOfTen(n));
    }

    public BigDecimal avm_stripTrailingZeros() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_stripTrailingZeros);
        return new BigDecimal(v.stripTrailingZeros());
    }

    public int avm_compareTo(BigDecimal val) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_compareTo);
        return v.compareTo(val.v);
    }

    public boolean equals(IObject x) {
        if (x == this)
            return true;

        if (!(x instanceof BigDecimal))
            return false;

        BigDecimal xDec = (BigDecimal) x;
        return v.equals(xDec.v);
    }

    public BigDecimal avm_min(BigDecimal val){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_min);
        return new BigDecimal(v.min(val.v));
    }

    public BigDecimal avm_max(BigDecimal val){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_max);
        return new BigDecimal(v.max(val.v));
    }

    public int avm_hashCode() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_hashCode);
        return v.hashCode();
    }

    public String avm_toString(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toString);
        return new String(v.toString());
    }

    public String avm_toEngineeringString(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toEngineeringString);
        return new String(v.toEngineeringString());
    }

    public String avm_toPlainString(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toPlainString);
        return new String(v.toPlainString());
    }

    public BigInteger avm_toBigInteger() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toBigInteger);
        return new BigInteger(v.toBigInteger());
    }

    public BigInteger avm_toBigIntegerExact() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toBigIntegerExact);
        return new BigInteger(v.toBigIntegerExact());
    }

    public long avm_longValue(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_longValue);
        return v.longValue();
    }

    public long avm_longValueExact(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_longValueExact);
        return v.longValueExact();
    }

    public int avm_intValue(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_intValue);
        return v.intValue();
    }

    public int avm_intValueExact() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_intValueExact);
        return v.intValueExact();
    }

    public short avm_shortValueExact() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_shortValueExact);
        return v.shortValueExact();
    }

    public byte avm_byteValueExact() {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_byteValueExact);
        return v.byteValueExact();
    }

    public float avm_floatValue(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_floatValue);
        return v.floatValue();
    }

    public double avm_doubleValue(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_doubleValue);
        return v.doubleValue();
    }

    public BigDecimal avm_ulp(){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_ulp);
        return new BigDecimal(v.ulp());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.math.BigDecimal v;

    public BigDecimal(java.math.BigDecimal u) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_16);
        v = u;
    }

    public java.math.BigDecimal getUnderlying() {
        return v;
    }

    // Deserializer support.
    public BigDecimal(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
        lazyLoad();
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(BigDecimal.class, deserializer);
        
        // We deserialize this as a string.
        java.lang.String simpler = CodecIdioms.deserializeString(deserializer);
        this.v = new java.math.BigDecimal(simpler);
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(String.class, serializer);
        
        // We serialize this as a string.
        CodecIdioms.serializeString(serializer, this.v.toString());
    }

    static{
        avm_ZERO    = new BigDecimal(java.math.BigDecimal.ZERO);
        avm_ONE     = new BigDecimal(java.math.BigDecimal.ONE);
        avm_TEN     = new BigDecimal(java.math.BigDecimal.TEN);
    }


    //========================================================
    // Methods below are deprecated
    //========================================================

    //public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode)

    //public BigDecimal divide(BigDecimal divisor, int roundingMode)

    //public BigDecimal setScale(int newScale, int roundingMode)

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public java.math.BigDecimal[] divideAndRemainder(java.math.BigDecimal divisor)

    //public BigDecimal[] divideAndRemainder(BigDecimal divisor, MathContext mc)
}

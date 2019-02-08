package org.aion.avm.shadow.java.math;

import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.CodecIdioms;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IInstrumentation;
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
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    public static final BigDecimal avm_ZERO;

    public static final BigDecimal avm_ONE;

    public static final BigDecimal avm_TEN;

    public BigDecimal(CharArray in, int offset, int len){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor);
        v = new java.math.BigDecimal(in.getUnderlying(), offset, len);
    }

    public BigDecimal(CharArray in, int offset, int len, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_1);
        v = new java.math.BigDecimal(in.getUnderlying(), offset, len,
                mc.getUnderlying());
    }

    public BigDecimal(CharArray in){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_2);
        v = new java.math.BigDecimal(in.getUnderlying());
    }

    public BigDecimal(CharArray in, MathContext mc){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_3);
        v = new java.math.BigDecimal(in.getUnderlying(), mc.getUnderlying());
    }

    public BigDecimal(String val){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_4);
        v = new java.math.BigDecimal(val.getUnderlying());
    }

    public BigDecimal(String val, MathContext mc){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_5);
        v = new java.math.BigDecimal(val.getUnderlying(), mc.getUnderlying());
    }

    public BigDecimal(double val){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_6);
        v = new java.math.BigDecimal(val);
    }

    public BigDecimal(double val, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_7);
        v = new java.math.BigDecimal(val, mc.getUnderlying());
    }

    public BigDecimal(BigInteger val) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_8);
        v = new java.math.BigDecimal(val.getUnderlying());
    }

    public BigDecimal(BigInteger val, MathContext mc){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_9);
        v = new java.math.BigDecimal(val.getUnderlying(), mc.getUnderlying());
    }

    public BigDecimal(BigInteger unscaledVal, int scale) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_10);
        v = new java.math.BigDecimal(unscaledVal.getUnderlying(), scale);
    }

    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_11);
        v = new java.math.BigDecimal(unscaledVal.getUnderlying(), scale, mc.getUnderlying());
    }

    public BigDecimal(int val) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_12);
        v = new java.math.BigDecimal(val);
    }

    public BigDecimal(int val, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_13);
        v = new java.math.BigDecimal(val, mc.getUnderlying());
    }

    public BigDecimal(long val) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_14);
        v = new java.math.BigDecimal(val);
    }

    public BigDecimal(long val, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_15);
        v = new java.math.BigDecimal(val, mc.getUnderlying());
    }

    public static BigDecimal avm_valueOf(long unscaledVal, int scale) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_valueOf);
        return new BigDecimal(java.math.BigDecimal.valueOf(unscaledVal, scale));
    }

    public static BigDecimal avm_valueOf(long val) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_valueOf_1);
        return new BigDecimal(java.math.BigDecimal.valueOf(val));
    }

    public static BigDecimal avm_valueOf(double val) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_valueOf_2);
        return new BigDecimal(java.math.BigDecimal.valueOf(val));
    }

    public BigDecimal avm_add(BigDecimal augend) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_add);
        lazyLoad();
        augend.lazyLoad();
        return new BigDecimal(v.add(augend.v));
    }

    public BigDecimal add(BigDecimal augend, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_add_1);
        lazyLoad();
        augend.lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.add(augend.v, mc.getUnderlying()));
    }

    public BigDecimal avm_subtract(BigDecimal subtrahend) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_subtract);
        lazyLoad();
        subtrahend.lazyLoad();
        return new BigDecimal(v.subtract(subtrahend.v));
    }

    public BigDecimal avm_subtract(BigDecimal subtrahend, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_subtract_1);
        lazyLoad();
        subtrahend.lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.subtract(subtrahend.v, mc.getUnderlying()));
    }

    public BigDecimal avm_multiply(BigDecimal multiplicand) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_multiply);
        lazyLoad();
        multiplicand.lazyLoad();
        return new BigDecimal(v.multiply(multiplicand.v));
    }

    public BigDecimal avm_multiply(BigDecimal multiplicand, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_multiply_1);
        lazyLoad();
        multiplicand.lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.multiply(multiplicand.v, mc.getUnderlying()));
    }

    public BigDecimal avm_divide(BigDecimal divisor, int scale, RoundingMode roundingMode){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divide);
        lazyLoad();
        divisor.lazyLoad();
        roundingMode.lazyLoad();
        return new BigDecimal(v.divide(divisor.v, scale, roundingMode.getUnderlying()));
    }

    public BigDecimal avm_divide(BigDecimal divisor, RoundingMode roundingMode) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divide_1);
        lazyLoad();
        divisor.lazyLoad();
        roundingMode.lazyLoad();
        return new BigDecimal(v.divide(divisor.v, roundingMode.getUnderlying()));
    }

    public BigDecimal avm_divide(BigDecimal divisor) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divide_2);
        lazyLoad();
        divisor.lazyLoad();
        return new BigDecimal(v.divide(divisor.v));
    }

    public BigDecimal avm_divide(BigDecimal divisor, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divide_3);
        lazyLoad();
        divisor.lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.divide(divisor.v, mc.getUnderlying()));
    }

    public BigDecimal avm_divideToIntegralValue(BigDecimal divisor) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divideToIntegralValue);
        lazyLoad();
        divisor.lazyLoad();
        return new BigDecimal(v.divideToIntegralValue(divisor.v));
    }

    public BigDecimal avm_divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_divideToIntegralValue_1);
        lazyLoad();
        divisor.lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.divideToIntegralValue(divisor.v, mc.getUnderlying()));
    }

    public BigDecimal avm_remainder(BigDecimal divisor) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_remainder);
        lazyLoad();
        divisor.lazyLoad();
        return new BigDecimal(v.remainder(divisor.v));
    }

    public BigDecimal avm_remainder(BigDecimal divisor, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_remainder_1);
        lazyLoad();
        divisor.lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.remainder(divisor.v, mc.getUnderlying()));
    }

    public BigDecimal avm_sqrt(MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_sqrt);
        lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.sqrt(mc.getUnderlying()));
    }

    public BigDecimal avm_pow(int n) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_pow);
        lazyLoad();
        return new BigDecimal(v.pow(n));
    }

    public BigDecimal avm_pow(int n, MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_pow_1);
        lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.pow(n, mc.getUnderlying()));
    }

    public BigDecimal avm_abs(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_abs);
        lazyLoad();
        return new BigDecimal(v.abs());
    }

    public BigDecimal avm_abs(MathContext mc){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_abs_1);
        lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.abs(mc.getUnderlying()));
    }

    public BigDecimal avm_negate() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_negate);
        lazyLoad();
        return new BigDecimal(v.negate());
    }

    public BigDecimal avm_negate(MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_negate_1);
        lazyLoad();
        mc.lazyLoad();
        return new BigDecimal(v.negate(mc.getUnderlying()));
    }

    public BigDecimal avm_plus() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_plus);
        lazyLoad();
        return new BigDecimal(v.negate());
    }

    public BigDecimal avm_plus(MathContext mc) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_plus_1);
        lazyLoad();
        return new BigDecimal(v.negate(mc.getUnderlying()));
    }

    public int avm_signum() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_signum);
        lazyLoad();
        return v.signum();
    }

    public int avm_scale() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_scale);
        lazyLoad();
        return v.scale();
    }

    public int avm_precision() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_precision);
        lazyLoad();
        return v.precision();
    }

    public BigInteger avm_unscaledValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_unscaledValue);
        lazyLoad();
        return new BigInteger(v.unscaledValue());
    }

    public BigDecimal avm_round(MathContext mc){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_round);
        lazyLoad();
        return new BigDecimal(v.round(mc.getUnderlying()));
    }

    public BigDecimal avm_setScale(int newScale, RoundingMode roundingMode) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_setScale);
        lazyLoad();
        return new BigDecimal(v.setScale(newScale, roundingMode.getUnderlying()));
    }

    public BigDecimal avm_setScale(int newScale){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_setScale_1);
        lazyLoad();
        return new BigDecimal(v.setScale(newScale));
    }

    public BigDecimal avm_movePointLeft(int n) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_movePointLeft);
        lazyLoad();
        return new BigDecimal(v.movePointLeft(n));
    }

    public BigDecimal avm_movePointRight(int n) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_movePointRight);
        lazyLoad();
        return new BigDecimal(v.movePointRight(n));
    }

    public BigDecimal avm_scaleByPowerOfTen(int n) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_scaleByPowerOfTen);
        lazyLoad();
        return new BigDecimal(v.scaleByPowerOfTen(n));
    }

    public BigDecimal avm_stripTrailingZeros() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_stripTrailingZeros);
        lazyLoad();
        return new BigDecimal(v.stripTrailingZeros());
    }

    public int avm_compareTo(BigDecimal val) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_compareTo);
        lazyLoad();
        val.lazyLoad();
        return v.compareTo(val.v);
    }

    public BigDecimal avm_min(BigDecimal val){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_min);
        lazyLoad();
        val.lazyLoad();
        return new BigDecimal(v.min(val.v));
    }

    public BigDecimal avm_max(BigDecimal val){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_max);
        lazyLoad();
        val.lazyLoad();
        return new BigDecimal(v.max(val.v));
    }

    public int avm_hashCode() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_hashCode);
        lazyLoad();
        return v.hashCode();
    }

    public String avm_toString(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toString);
        lazyLoad();
        return new String(v.toString());
    }

    public String avm_toEngineeringString(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toEngineeringString);
        lazyLoad();
        return new String(v.toEngineeringString());
    }

    public String avm_toPlainString(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toPlainString);
        lazyLoad();
        return new String(v.toPlainString());
    }

    public BigInteger avm_toBigInteger() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toBigInteger);
        lazyLoad();
        return new BigInteger(v.toBigInteger());
    }

    public BigInteger avm_toBigIntegerExact() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_toBigIntegerExact);
        lazyLoad();
        return new BigInteger(v.toBigIntegerExact());
    }

    public long avm_longValue(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_longValue);
        lazyLoad();
        return v.longValue();
    }

    public long avm_longValueExact(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_longValueExact);
        lazyLoad();
        return v.longValueExact();
    }

    public int avm_intValue(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_intValue);
        lazyLoad();
        return v.intValue();
    }

    public int avm_intValueExact() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_intValueExact);
        lazyLoad();
        return v.intValueExact();
    }

    public short avm_shortValueExact() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_shortValueExact);
        lazyLoad();
        return v.shortValueExact();
    }

    public byte avm_byteValueExact() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_byteValueExact);
        lazyLoad();
        return v.byteValueExact();
    }

    public float avm_floatValue(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_floatValue);
        lazyLoad();
        return v.floatValue();
    }

    public double avm_doubleValue(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_doubleValue);
        lazyLoad();
        return v.doubleValue();
    }

    public BigDecimal avm_ulp(){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_ulp);
        lazyLoad();
        return new BigDecimal(v.ulp());
    }

    public boolean avm_equals(IObject x) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_equals);
        if (x == this) {
            return true;
        }

        if (!(x instanceof BigDecimal)) {
            return false;
        }

        BigDecimal xInt = (BigDecimal) x;
        lazyLoad();
        xInt.lazyLoad();
        return v.equals(xInt.v);
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private java.math.BigDecimal v;

    public BigDecimal(java.math.BigDecimal u) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BigDecimal_avm_constructor_16);
        v = u;
    }

    public java.math.BigDecimal getUnderlying() {
        lazyLoad();
        return v;
    }

    // Deserializer support.
    public BigDecimal(IDeserializer deserializer, IPersistenceToken persistenceToken) {
        super(deserializer, persistenceToken);
    }

    public void deserializeSelf(java.lang.Class<?> firstRealImplementation, IObjectDeserializer deserializer) {
        super.deserializeSelf(BigDecimal.class, deserializer);
        
        // We deserialize this as a string.
        java.lang.String simpler = CodecIdioms.deserializeString(deserializer);
        this.v = new java.math.BigDecimal(simpler);
    }

    public void serializeSelf(java.lang.Class<?> firstRealImplementation, IObjectSerializer serializer) {
        super.serializeSelf(BigDecimal.class, serializer);
        
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

package org.aion.avm.shadow.java.math;

import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.internal.IObject;
import org.aion.avm.shadow.java.lang.Comparable;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.lang.Number;

public class BigDecimal extends Number implements Comparable<BigDecimal>{

    public static final BigDecimal avm_ZERO;

    public static final BigDecimal avm_ONE;

    public static final BigDecimal avm_TEN;

    public BigDecimal(CharArray in, int offset, int len){
        v = new java.math.BigDecimal(in.getUnderlying(), offset, len);
    }

    public BigDecimal(CharArray in, int offset, int len, MathContext mc) {
        v = new java.math.BigDecimal(in.getUnderlying(), offset, len,
                mc.getV());
    }

    public BigDecimal(CharArray in){
        v = new java.math.BigDecimal(in.getUnderlying());
    }

    public BigDecimal(CharArray in, MathContext mc){
        v = new java.math.BigDecimal(in.getUnderlying(), mc.getV());
    }

    public BigDecimal(String val){
        v = new java.math.BigDecimal(val.getV());
    }

    public BigDecimal(String val, MathContext mc){
        v = new java.math.BigDecimal(val.getV(), mc.getV());
    }

    public BigDecimal(double val){
        v = new java.math.BigDecimal(val);
    }

    public BigDecimal(double val, MathContext mc) {
        v = new java.math.BigDecimal(val, mc.getV());
    }

    public BigDecimal(BigInteger val) {
        v = new java.math.BigDecimal(val.getV());
    }

    public BigDecimal(BigInteger val, MathContext mc){
        v = new java.math.BigDecimal(val.getV(), mc.getV());
    }

    public BigDecimal(BigInteger unscaledVal, int scale) {
        v = new java.math.BigDecimal(unscaledVal.getV(), scale);
    }

    public BigDecimal(BigInteger unscaledVal, int scale, MathContext mc) {
        v = new java.math.BigDecimal(unscaledVal.getV(), scale, mc.getV());
    }

    public BigDecimal(int val) {
        v = new java.math.BigDecimal(val);
    }

    public BigDecimal(int val, MathContext mc) {
        v = new java.math.BigDecimal(val, mc.getV());
    }

    public BigDecimal(long val) {
        v = new java.math.BigDecimal(val);
    }

    public BigDecimal(long val, MathContext mc) {
        v = new java.math.BigDecimal(val, mc.getV());
    }

    public static BigDecimal avm_valueOf(long unscaledVal, int scale) {
        return new BigDecimal(java.math.BigDecimal.valueOf(unscaledVal, scale));
    }

    public static BigDecimal avm_valueOf(long val) {
        return new BigDecimal(java.math.BigDecimal.valueOf(val));
    }

    public static BigDecimal avm_valueOf(double val) {
        return new BigDecimal(java.math.BigDecimal.valueOf(val));
    }

    public BigDecimal avm_add(BigDecimal augend) {
        return new BigDecimal(v.add(augend.v));
    }

    public BigDecimal add(BigDecimal augend, MathContext mc) {
        return new BigDecimal(v.add(augend.v, mc.getV()));
    }

    public BigDecimal avm_subtract(BigDecimal subtrahend) {
        return new BigDecimal(v.subtract(subtrahend.v));
    }

    public BigDecimal avm_subtract(BigDecimal subtrahend, MathContext mc) {
        return new BigDecimal(v.subtract(subtrahend.v, mc.getV()));
    }

    public BigDecimal avm_multiply(BigDecimal multiplicand) {
        return new BigDecimal(v.multiply(multiplicand.v));
    }

    public BigDecimal avm_multiply(BigDecimal multiplicand, MathContext mc) {
        return new BigDecimal(v.multiply(multiplicand.v, mc.getV()));
    }

    public BigDecimal avm_divide(BigDecimal divisor, int scale, RoundingMode roundingMode){
        return new BigDecimal(v.divide(divisor.v, scale, roundingMode.getV()));
    }

    public BigDecimal avm_divide(BigDecimal divisor, RoundingMode roundingMode) {
        return new BigDecimal(v.divide(divisor.v, roundingMode.getV()));
    }

    public BigDecimal avm_divide(BigDecimal divisor) {
        return new BigDecimal(v.divide(divisor.v));
    }

    public BigDecimal avm_divide(BigDecimal divisor, MathContext mc) {
        return new BigDecimal(v.divide(divisor.v, mc.getV()));
    }

    public BigDecimal avm_divideToIntegralValue(BigDecimal divisor) {
        return new BigDecimal(v.divideToIntegralValue(divisor.v));
    }

    public BigDecimal avm_divideToIntegralValue(BigDecimal divisor, MathContext mc) {
        return new BigDecimal(v.divideToIntegralValue(divisor.v, mc.getV()));
    }

    public BigDecimal avm_remainder(BigDecimal divisor) {
        return new BigDecimal(v.remainder(divisor.v));
    }

    public BigDecimal avm_remainder(BigDecimal divisor, MathContext mc) {
        return new BigDecimal(v.remainder(divisor.v, mc.getV()));
    }

    public BigDecimal avm_sqrt(MathContext mc) {
        return new BigDecimal(v.sqrt(mc.getV()));
    }

    public BigDecimal avm_pow(int n) {
        return new BigDecimal(v.pow(n));
    }

    public BigDecimal avm_pow(int n, MathContext mc) {
        return new BigDecimal(v.pow(n, mc.getV()));
    }

    public BigDecimal avm_abs(){
        return new BigDecimal(v.abs());
    }

    public BigDecimal avm_abs(MathContext mc){
        return new BigDecimal(v.abs(mc.getV()));
    }

    public BigDecimal avm_negate() {
        return new BigDecimal(v.negate());
    }

    public BigDecimal avm_negate(MathContext mc) {
        return new BigDecimal(v.negate(mc.getV()));
    }

    public BigDecimal avm_plus() {
        return new BigDecimal(v.negate());
    }

    public BigDecimal avm_plus(MathContext mc) {
        return new BigDecimal(v.negate(mc.getV()));
    }

    public int avm_signum() {
        return v.signum();
    }

    public int avm_scale() {
        return v.scale();
    }

    public int avm_precision() {
        return v.precision();
    }

    public BigInteger avm_unscaledValue() {
        return new BigInteger(v.unscaledValue());
    }

    public BigDecimal avm_round(MathContext mc){
        return new BigDecimal(v.round(mc.getV()));
    }

    public BigDecimal avm_setScale(int newScale, RoundingMode roundingMode) {
        return new BigDecimal(v.setScale(newScale, roundingMode.getV()));
    }

    public BigDecimal avm_setScale(int newScale){
        return new BigDecimal(v.setScale(newScale));
    }

    public BigDecimal avm_movePointLeft(int n) {
        return new BigDecimal(v.movePointLeft(n));
    }

    public BigDecimal avm_movePointRight(int n) {
        return new BigDecimal(v.movePointRight(n));
    }

    public BigDecimal avm_scaleByPowerOfTen(int n) {
        return new BigDecimal(v.scaleByPowerOfTen(n));
    }

    public BigDecimal avm_stripTrailingZeros() {
        return new BigDecimal(v.stripTrailingZeros());
    }

    public int avm_compareTo(BigDecimal val) {
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
        return new BigDecimal(v.min(val.v));
    }

    public BigDecimal avm_max(BigDecimal val){
        return new BigDecimal(v.max(val.v));
    }

    public int avm_hashCode() {
        return v.hashCode();
    }

    public String avm_toString(){
        return new String(v.toString());
    }

    public String avm_toEngineeringString(){
        return new String(v.toEngineeringString());
    }

    public String avm_toPlainString(){
        return new String(v.toPlainString());
    }

    public BigInteger avm_toBigInteger() {
        return new BigInteger(v.toBigInteger());
    }

    public BigInteger avm_toBigIntegerExact() {
        return new BigInteger(v.toBigIntegerExact());
    }

    public long avm_longValue(){
        return v.longValue();
    }

    public long avm_longValueExact(){
        return v.longValueExact();
    }

    public int avm_intValue(){
        return v.intValue();
    }

    public int avm_intValueExact() {
        return v.intValueExact();
    }

    public short avm_shortValueExact() {
        return v.shortValueExact();
    }

    public byte avm_byteValueExact() {
        return v.byteValueExact();
    }

    public float avm_floatValue(){
        return v.floatValue();
    }

    public double avm_doubleValue(){
        return v.doubleValue();
    }

    public BigDecimal avm_ulp(){
        return new BigDecimal(v.ulp());
    }

    //========================================================
    // Methods below are used by runtime and test code only!
    //========================================================

    private final java.math.BigDecimal v;

    public BigDecimal(java.math.BigDecimal u) {
        v = u;
    }

    public java.math.BigDecimal getV() {
        return v;
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

package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IHelper;

import org.aion.avm.RuntimeMethodFeeSchedule;

public final class StrictMath extends Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    private StrictMath() {}

    public static final double avm_E = java.lang.StrictMath.E;

    public static final double avm_PI = java.lang.StrictMath.PI;

    public static double avm_sin(double a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_sin);
        return java.lang.StrictMath.sin(a);
    }

    public static double avm_cos(double a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_cos);
        return java.lang.StrictMath.cos(a);
    }

    public static double avm_tan(double a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_tan);
        return java.lang.StrictMath.tan(a);
    }

    public static double avm_asin(double a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_asin);
        return java.lang.StrictMath.asin(a);
    }

    public static double avm_acos(double a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_acos);
        return java.lang.StrictMath.acos(a);
    }

    public static double avm_atan(double a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_atan);
        return java.lang.StrictMath.atan(a);
    }

    public static strictfp double avm_toRadians(double angdeg) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_toRadians);
        return java.lang.StrictMath.toRadians(angdeg);
    }

    public static strictfp double avm_toDegrees(double angrad) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_toDegrees);
        return java.lang.StrictMath.toDegrees(angrad);
    }

    public static double avm_exp(double a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_exp);
        return java.lang.StrictMath.exp(a);
    }

    public static double avm_log(double a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_log);
        return java.lang.StrictMath.log(a);
    }

    public static double avm_log10(double a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_log10);
        return java.lang.StrictMath.log10(a);
    }

    public static double avm_sqrt(double a){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_sqrt);
        return java.lang.StrictMath.sqrt(a);
    }

    public static double avm_cbrt(double a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_cbrt);
        return java.lang.StrictMath.cbrt(a);
    }

    public static double avm_IEEEremainder(double f1, double f2){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_IEEEremainder);
        return java.lang.StrictMath.IEEEremainder(f1, f2);
    }

    public static double avm_ceil(double a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_ceil);
        return java.lang.StrictMath.ceil(a);
    }

    public static double avm_floor(double a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_floor);
        return java.lang.StrictMath.floor(a);
    }

    public static double avm_rint(double a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_rint);
        return java.lang.StrictMath.rint(a);
    }

    public static double avm_atan2(double y, double x){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_atan2);
        return java.lang.StrictMath.atan2(y, x);
    }

    public static double avm_pow(double a, double b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_pow);
        return java.lang.StrictMath.pow(a, b);
    }

    public static int avm_round(float a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_round);
        return java.lang.StrictMath.round(a);
    }

    public static long avm_round(double a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_round_1);
        return java.lang.StrictMath.round(a);
    }

    public static int avm_addExact(int x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_addExact);
        return java.lang.StrictMath.addExact(x, y);
    }

    public static long avm_addExact(long x, long y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_addExact_1);
        return java.lang.StrictMath.addExact(x, y);
    }

    public static int avm_subtractExact(int x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_subtractExact);
        return java.lang.StrictMath.subtractExact(x, y);
    }

    public static long avm_subtractExact(long x, long y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_subtractExact_1);
        return java.lang.StrictMath.subtractExact(x, y);
    }

    public static int avm_multiplyExact(int x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_multiplyExact);
        return java.lang.StrictMath.multiplyExact(x, y);
    }

    public static long avm_multiplyExact(long x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_multiplyExact_1);
        return java.lang.StrictMath.multiplyExact(x, y);
    }

    public static long avm_multiplyExact(long x, long y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_multiplyExact_2);
        return java.lang.StrictMath.multiplyExact(x, y);
    }

    public static int avm_toIntExact(long value) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_toIntExact);
        return java.lang.StrictMath.toIntExact(value);
    }

    public static long avm_multiplyFull(int x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_multiplyFull);
        return java.lang.StrictMath.multiplyFull(x, y);
    }

    public static long avm_multiplyHigh(long x, long y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_multiplyHigh);
        return java.lang.StrictMath.multiplyHigh(x, y);
    }

    public static int avm_floorDiv(int x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_floorDiv);
        return java.lang.StrictMath.floorDiv(x, y);
    }

    public static long avm_floorDiv(long x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_floorDiv_1);
        return java.lang.StrictMath.floorDiv(x, y);
    }

    public static long avm_floorDiv(long x, long y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_floorDiv_2);
        return java.lang.StrictMath.floorDiv(x, y);
    }

    public static int avm_floorMod(int x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_floorMod);
        return java.lang.StrictMath.floorMod(x , y);
    }

    public static int avm_floorMod(long x, int y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_floorMod_1);
        return java.lang.StrictMath.floorMod(x , y);
    }

    public static long avm_floorMod(long x, long y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_floorMod_2);
        return java.lang.StrictMath.floorMod(x, y);
    }

    public static int avm_abs(int a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_abs);
        return java.lang.StrictMath.abs(a);
    }

    public static long avm_abs(long a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_abs_1);
        return java.lang.StrictMath.abs(a);
    }

    public static float avm_abs(float a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_abs_2);
        return java.lang.StrictMath.abs(a);
    }

    public static double avm_abs(double a) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_abs_3);
        return java.lang.StrictMath.abs(a);
    }

    public static int avm_max(int a, int b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_max);
        return java.lang.StrictMath.max(a, b);
    }

    public static long avm_max(long a, long b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_max_1);
        return java.lang.StrictMath.max(a, b);
    }

    public static float avm_max(float a, float b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_max_2);
        return java.lang.StrictMath.max(a, b);
    }

    public static double avm_max(double a, double b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_max_3);
        return java.lang.StrictMath.max(a, b);
    }

    public static int avm_min(int a, int b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_min);
        return java.lang.StrictMath.min(a, b);
    }

    public static long avm_min(long a, long b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_min_1);
        return java.lang.StrictMath.min(a, b);
    }

    public static float avm_min(float a, float b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_min_2);
        return java.lang.StrictMath.min(a, b);
    }

    public static double avm_min(double a, double b) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_min_3);
        return java.lang.StrictMath.min(a, b);
    }

    public static double avm_fma(double a, double b, double c) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_fma);
        return Math.avm_fma(a, b, c);
    }

    public static float avm_fma(float a, float b, float c) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_fma_1);
        return Math.avm_fma(a, b, c);
    }

    public static double avm_ulp(double d) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_ulp);
        return Math.avm_ulp(d);
    }

    public static float avm_ulp(float f) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_ulp_1);
        return Math.avm_ulp(f);
    }

    public static double avm_signum(double d) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_signum);
        return Math.avm_signum(d);
    }

    public static float avm_signum(float f) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_signum_1);
        return Math.avm_signum(f);
    }

    public static double avm_sinh(double x){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_sinh);
        return java.lang.StrictMath.sinh(x);
    }

    public static double avm_cosh(double x){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_cosh);
        return java.lang.StrictMath.cosh(x);
    }

    public static double avm_tanh(double x){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_tanh);
        return java.lang.StrictMath.tanh(x);
    }

    public static double avm_hypot(double x, double y) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_hypot);
        return java.lang.StrictMath.hypot(x, y);
    }

    public static double avm_expm1(double x){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_expm1);
        return java.lang.StrictMath.expm1(x);
    }

    public static double avm_log1p(double x){
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_log1p);
        return java.lang.StrictMath.log1p(x);
    }

    public static double avm_copySign(double magnitude, double sign) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_copySign);
        return Math.avm_copySign(magnitude, (Double.avm_isNaN(sign)?1.0d:sign));
    }

    public static float avm_copySign(float magnitude, float sign) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_copySign_1);
        return Math.avm_copySign(magnitude, (Float.avm_isNaN(sign)?1.0f:sign));
    }

    public static int avm_getExponent(float f) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_getExponent);
        return Math.avm_getExponent(f);
    }

    public static int avm_getExponent(double d) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_getExponent_1);
        return Math.avm_getExponent(d);
    }

    public static double avm_nextAfter(double start, double direction) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_nextAfter);
        return Math.avm_nextAfter(start, direction);
    }

    public static float avm_nextAfter(float start, double direction) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_nextAfter_1);
        return Math.avm_nextAfter(start, direction);
    }

    public static double avm_nextUp(double d) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_nextUp);
        return Math.avm_nextUp(d);
    }

    public static float avm_nextUp(float f) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_nextUp_1);
        return Math.avm_nextUp(f);
    }

    public static double avm_nextDown(double d) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_nextDown);
        return Math.avm_nextDown(d);
    }

    public static float avm_nextDown(float f) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_nextDown_1);
        return Math.avm_nextDown(f);
    }

    public static double avm_scalb(double d, int scaleFactor) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_scalb);
        return Math.avm_scalb(d, scaleFactor);
    }

    public static float avm_scalb(float f, int scaleFactor) {
        IHelper.currentContractHelper.get().externalChargeEnergy(RuntimeMethodFeeSchedule.StrictMath_avm_scalb_1);
        return Math.avm_scalb(f, scaleFactor);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static double random()



}

package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IInstrumentation;

import org.aion.avm.RuntimeMethodFeeSchedule;

public final class Math extends Object {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IInstrumentation.attachedThreadInstrumentation.get().bootstrapOnly();
    }

    public static final double avm_E = java.lang.Math.E;

    public static final double avm_PI = java.lang.Math.PI;

    private Math() {}

    public static double avm_IEEEremainder(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_IEEEremainder);
        return java.lang.Math.IEEEremainder(a, b);
    }

    public static double avm_abs(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_abs);
        return java.lang.Math.abs(a);
    }

    public static int avm_abs(int a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_abs_1);
        return java.lang.Math.abs(a);
    }

    public static long avm_abs(long a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_abs_2);
        return java.lang.Math.abs(a);
    }

    public static float avm_abs(float a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_abs_3);
        return java.lang.Math.abs(a);
    }

    public static double avm_acos(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_acos);
        return java.lang.Math.acos(a);
    }

    public static long avm_addExact(long a, long b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_addExact);
        return java.lang.Math.addExact(a, b);
    }

    public static int avm_addExact(int a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_addExact_1);
        return java.lang.Math.addExact(a, b);
    }

    public static double avm_asin(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_asin);
        return java.lang.Math.asin(a);
    }

    public static double avm_atan(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_atan);
        return java.lang.Math.atan(a);
    }

    public static double avm_atan2(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_atan2);
        return java.lang.Math.atan2(a, b);
    }

    public static double avm_cbrt(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_cbrt);
        return java.lang.Math.cbrt(a);
    }

    public static double avm_ceil(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_ceil);
        return java.lang.Math.ceil(a);
    }

    public static float avm_copySign(float a, float b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_copySign);
        return java.lang.Math.copySign(a, b);
    }

    public static double avm_copySign(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_copySign_1);
        return java.lang.Math.copySign(a, b);
    }

    public static double avm_cos(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_cos);
        return java.lang.Math.cos(a);
    }

    public static double avm_cosh(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_cosh);
        return java.lang.Math.cosh(a);
    }

    public static long avm_decrementExact(long a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_decrementExact);
        return java.lang.Math.decrementExact(a);
    }

    public static int avm_decrementExact(int a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_decrementExact_1);
        return java.lang.Math.decrementExact(a);
    }

    public static double avm_exp(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_exp);
        return java.lang.Math.exp(a);
    }

    public static double avm_expm1(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_expm1);
        return java.lang.Math.expm1(a);
    }

    public static double avm_floor(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_floor);
        return java.lang.Math.floor(a);
    }

    public static int avm_floorDiv(int a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_floorDiv);
        return java.lang.Math.floorDiv(a, b);
    }

    public static long avm_floorDiv(long a, long b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_floorDiv_1);
        return java.lang.Math.floorDiv(a, b);
    }

    public static long avm_floorDiv(long a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_floorDiv_2);
        return java.lang.Math.floorDiv(a, b);
    }

    public static int avm_floorMod(int a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_floorMod);
        return java.lang.Math.floorMod(a, b);
    }

    public static long avm_floorMod(long a, long b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_floorMod_1);
        return java.lang.Math.floorMod(a, b);
    }

    public static int avm_floorMod(long a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_floorMod_2);
        return java.lang.Math.floorMod(a, b);
    }

    public static double avm_fma(double a, double b, double c) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_fma);
        return java.lang.Math.fma(a, b, c);
    }

    public static float avm_fma(float a, float b, float c) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_fma_1);
        return java.lang.Math.fma(a, b, c);
    }

    public static int avm_getExponent(float a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_getExponent);
        return java.lang.Math.getExponent(a);
    }

    public static int avm_getExponent(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_getExponent_1);
        return java.lang.Math.getExponent(a);
    }

    public static double avm_hypot(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_hypot);
        return java.lang.Math.hypot(a, b);
    }

    public static int avm_incrementExact(int a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_incrementExact);
        return java.lang.Math.incrementExact(a);
    }

    public static long avm_incrementExact(long a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_incrementExact_1);
        return java.lang.Math.incrementExact(a);
    }

    public static double avm_log(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_log);
        return java.lang.Math.log(a);
    }

    public static double avm_log10(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_log10);
        return java.lang.Math.log10(a);
    }

    public static double avm_log1p(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_log1p);
        return java.lang.Math.log1p(a);
    }

    public static float avm_max(float a, float b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_max);
        return java.lang.Math.max(a, b);
    }

    public static long avm_max(long a, long b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_max_1);
        return java.lang.Math.max(a, b);
    }

    public static int avm_max(int a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_max_2);
        return java.lang.Math.max(a, b);
    }

    public static double avm_max(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_max_3);
        return java.lang.Math.max(a, b);
    }

    public static long avm_min(long a, long b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_min);
        return java.lang.Math.min(a, b);
    }

    public static double avm_min(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_min_1);
        return java.lang.Math.min(a, b);
    }

    public static float avm_min(float a, float b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_min_2);
        return java.lang.Math.min(a, b);
    }

    public static int avm_min(int a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_min_3);
        return java.lang.Math.min(a, b);
    }

    public static long avm_multiplyExact(long a, long b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_multiplyExact);
        return java.lang.Math.multiplyExact(a, b);
    }

    public static long avm_multiplyExact(long a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_multiplyExact_1);
        return java.lang.Math.multiplyExact(a, b);
    }

    public static int avm_multiplyExact(int a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_multiplyExact_2);
        return java.lang.Math.multiplyExact(a, b);
    }

    public static long avm_multiplyFull(int a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_multiplyFull);
        return java.lang.Math.multiplyFull(a, b);
    }

    public static long avm_multiplyHigh(long a, long b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_multiplyHigh);
        return java.lang.Math.multiplyHigh(a, b);
    }

    public static int avm_negateExact(int a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_negateExact);
        return java.lang.Math.negateExact(a);
    }

    public static long avm_negateExact(long a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_negateExact_1);
        return java.lang.Math.negateExact(a);
    }

    public static double avm_nextAfter(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_nextAfter);
        return java.lang.Math.nextAfter(a, b);
    }

    public static float avm_nextAfter(float a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_nextAfter_1);
        return java.lang.Math.nextAfter(a, b);
    }

    public static float avm_nextDown(float a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_nextDown);
        return java.lang.Math.nextDown(a);
    }

    public static double avm_nextDown(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_nextDown_1);
        return java.lang.Math.nextDown(a);
    }

    public static double avm_nextUp(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_nextUp);
        return java.lang.Math.nextUp(a);
    }

    public static float avm_nextUp(float a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_nextUp_1);
        return java.lang.Math.nextUp(a);
    }

    public static double avm_pow(double a, double b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_pow);
        return java.lang.Math.pow(a, b);
    }

    public static double avm_rint(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_rint);
        return java.lang.Math.rint(a);
    }

    public static long avm_round(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_round);
        return java.lang.Math.round(a);
    }

    public static int avm_round(float a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_round_1);
        return java.lang.Math.round(a);
    }

    public static double avm_scalb(double a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_scalb);
        return java.lang.Math.scalb(a, b);
    }

    public static float avm_scalb(float a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_scalb_1);
        return java.lang.Math.scalb(a, b);
    }

    public static float avm_signum(float a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_signum);
        return java.lang.Math.signum(a);
    }

    public static double avm_signum(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_signum_1);
        return java.lang.Math.signum(a);
    }

    public static double avm_sin(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_sin);
        return java.lang.Math.sin(a);
    }

    public static double avm_sinh(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_sinh);
        return java.lang.Math.sinh(a);
    }

    public static double avm_sqrt(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_sqrt);
        return java.lang.Math.sqrt(a);
    }

    public static int avm_subtractExact(int a, int b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_subtractExact);
        return java.lang.Math.subtractExact(a, b);
    }

    public static long avm_subtractExact(long a, long b) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_subtractExact_1);
        return java.lang.Math.subtractExact(a, b);
    }

    public static double avm_tan(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_tan);
        return java.lang.Math.tan(a);
    }

    public static double avm_tanh(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_tanh);
        return java.lang.Math.tanh(a);
    }

    public static double avm_toDegrees(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_toDegrees);
        return java.lang.Math.toDegrees(a);
    }

    public static int avm_toIntExact(long a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_toIntExact);
        return java.lang.Math.toIntExact(a);
    }

    public static double avm_toRadians(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_toRadians);
        return java.lang.Math.toRadians(a);
    }

    public static double avm_ulp(double a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_ulp);
        return java.lang.Math.ulp(a);
    }

    public static float avm_ulp(float a) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.Math_avm_ulp_1);
        return java.lang.Math.ulp(a);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static double random()
}

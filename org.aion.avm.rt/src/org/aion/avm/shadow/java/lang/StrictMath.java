package org.aion.avm.shadow.java.lang;

import org.aion.avm.internal.IHelper;


public final class StrictMath {
    static {
        // Shadow classes MUST be loaded during bootstrap phase.
        IHelper.currentContractHelper.get().externalBootstrapOnly();
    }

    private StrictMath() {}

    public static final double avm_E = java.lang.StrictMath.E;

    public static final double avm_PI = java.lang.StrictMath.PI;

    public static double avm_sin(double a){
        return java.lang.StrictMath.sin(a);
    }

    public static double avm_cos(double a){
        return java.lang.StrictMath.cos(a);
    }

    public static double avm_tan(double a){
        return java.lang.StrictMath.tan(a);
    }

    public static double avm_asin(double a){
        return java.lang.StrictMath.asin(a);
    }

    public static double avm_acos(double a){
        return java.lang.StrictMath.acos(a);
    }

    public static double avm_atan(double a){
        return java.lang.StrictMath.atan(a);
    }

    public static strictfp double avm_toRadians(double angdeg) {
        return java.lang.StrictMath.toRadians(angdeg);
    }

    public static strictfp double avm_toDegrees(double angrad) {
        return java.lang.StrictMath.toDegrees(angrad);
    }

    public static double avm_exp(double a) {
        return java.lang.StrictMath.exp(a);
    }

    public static double avm_log(double a){
        return java.lang.StrictMath.log(a);
    }

    public static double avm_log10(double a){
        return java.lang.StrictMath.log10(a);
    }

    public static double avm_sqrt(double a){
        return java.lang.StrictMath.sqrt(a);
    }

    public static double avm_cbrt(double a) {
        return java.lang.StrictMath.cbrt(a);
    }

    public static double avm_IEEEremainder(double f1, double f2){
        return java.lang.StrictMath.IEEEremainder(f1, f2);
    }

    public static double avm_ceil(double a) {
        return java.lang.StrictMath.ceil(a);
    }

    public static double avm_floor(double a) {
        return java.lang.StrictMath.floor(a);
    }

    public static double avm_rint(double a) {
        return java.lang.StrictMath.rint(a);
    }

    public static double avm_atan2(double y, double x){
        return java.lang.StrictMath.atan2(y, x);
    }

    public static double avm_pow(double a, double b) {
        return java.lang.StrictMath.pow(a, b);
    }

    public static int avm_round(float a) {
        return java.lang.StrictMath.round(a);
    }

    public static long avm_round(double a) {
        return java.lang.StrictMath.round(a);
    }

    public static int avm_addExact(int x, int y) {
        return java.lang.StrictMath.addExact(x, y);
    }

    public static long avm_addExact(long x, long y) {
        return java.lang.StrictMath.addExact(x, y);
    }

    public static int avm_subtractExact(int x, int y) {
        return java.lang.StrictMath.subtractExact(x, y);
    }

    public static long avm_subtractExact(long x, long y) {
        return java.lang.StrictMath.subtractExact(x, y);
    }

    public static int avm_multiplyExact(int x, int y) {
        return java.lang.StrictMath.multiplyExact(x, y);
    }

    public static long avm_multiplyExact(long x, int y) {
        return java.lang.StrictMath.multiplyExact(x, y);
    }

    public static long avm_multiplyExact(long x, long y) {
        return java.lang.StrictMath.multiplyExact(x, y);
    }

    public static int avm_toIntExact(long value) {
        return java.lang.StrictMath.toIntExact(value);
    }

    public static long avm_multiplyFull(int x, int y) {
        return java.lang.StrictMath.multiplyFull(x, y);
    }

    public static long avm_multiplyHigh(long x, long y) {
        return java.lang.StrictMath.multiplyHigh(x, y);
    }

    public static int avm_floorDiv(int x, int y) {
        return java.lang.StrictMath.floorDiv(x, y);
    }

    public static long avm_floorDiv(long x, int y) {
        return java.lang.StrictMath.floorDiv(x, y);
    }

    public static long avm_floorDiv(long x, long y) {
        return java.lang.StrictMath.floorDiv(x, y);
    }

    public static int avm_floorMod(int x, int y) {
        return java.lang.StrictMath.floorMod(x , y);
    }

    public static int avm_floorMod(long x, int y) {
        return java.lang.StrictMath.floorMod(x , y);
    }

    public static long avm_floorMod(long x, long y) {
        return java.lang.StrictMath.floorMod(x, y);
    }

    public static int avm_abs(int a) {
        return java.lang.StrictMath.abs(a);
    }

    public static long avm_abs(long a) {
        return java.lang.StrictMath.abs(a);
    }

    public static float avm_abs(float a) {
        return java.lang.StrictMath.abs(a);
    }

    public static double avm_abs(double a) {
        return java.lang.StrictMath.abs(a);
    }

    public static int avm_max(int a, int b) {
        return java.lang.StrictMath.max(a, b);
    }

    public static long avm_max(long a, long b) {
        return java.lang.StrictMath.max(a, b);
    }

    public static float avm_max(float a, float b) {
        return java.lang.StrictMath.max(a, b);
    }

    public static double avm_max(double a, double b) {
        return java.lang.StrictMath.max(a, b);
    }

    public static int avm_min(int a, int b) {
        return java.lang.StrictMath.min(a, b);
    }

    public static long avm_min(long a, long b) {
        return java.lang.StrictMath.min(a, b);
    }

    public static float avm_min(float a, float b) {
        return java.lang.StrictMath.min(a, b);
    }

    public static double avm_min(double a, double b) {
        return java.lang.StrictMath.min(a, b);
    }

    public static double avm_fma(double a, double b, double c) {
        return Math.avm_fma(a, b, c);
    }

    public static float avm_fma(float a, float b, float c) {
        return Math.avm_fma(a, b, c);
    }

    public static double avm_ulp(double d) {
        return Math.avm_ulp(d);
    }

    public static float avm_ulp(float f) {
        return Math.avm_ulp(f);
    }

    public static double avm_signum(double d) {
        return Math.avm_signum(d);
    }

    public static float avm_signum(float f) {
        return Math.avm_signum(f);
    }

    public static double avm_sinh(double x){
        return java.lang.StrictMath.sinh(x);
    }

    public static double avm_cosh(double x){
        return java.lang.StrictMath.cosh(x);
    }

    public static double avm_tanh(double x){
        return java.lang.StrictMath.tanh(x);
    }

    public static double avm_hypot(double x, double y) {
        return java.lang.StrictMath.hypot(x, y);
    }

    public static double avm_expm1(double x){
        return java.lang.StrictMath.expm1(x);
    }

    public static double avm_log1p(double x){
        return java.lang.StrictMath.log1p(x);
    }

    public static double avm_copySign(double magnitude, double sign) {
        return Math.avm_copySign(magnitude, (Double.avm_isNaN(sign)?1.0d:sign));
    }

    public static float avm_copySign(float magnitude, float sign) {
        return Math.avm_copySign(magnitude, (Float.avm_isNaN(sign)?1.0f:sign));
    }

    public static int avm_getExponent(float f) {
        return Math.avm_getExponent(f);
    }

    public static int avm_getExponent(double d) {
        return Math.avm_getExponent(d);
    }

    public static double avm_nextAfter(double start, double direction) {
        return Math.avm_nextAfter(start, direction);
    }

    public static float avm_nextAfter(float start, double direction) {
        return Math.avm_nextAfter(start, direction);
    }

    public static double avm_nextUp(double d) {
        return Math.avm_nextUp(d);
    }

    public static float avm_nextUp(float f) {
        return Math.avm_nextUp(f);
    }

    public static double avm_nextDown(double d) {
        return Math.avm_nextDown(d);
    }

    public static float avm_nextDown(float f) {
        return Math.avm_nextDown(f);
    }

    public static double avm_scalb(double d, int scaleFactor) {
        return Math.avm_scalb(d, scaleFactor);
    }

    public static float avm_scalb(float f, int scaleFactor) {
        return Math.avm_scalb(f, scaleFactor);
    }

    //========================================================
    // Methods below are excluded from shadowing
    //========================================================

    //public static double random()



}

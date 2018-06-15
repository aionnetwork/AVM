package org.aion.avm.java.lang;

public final class Math extends Object {

    /**
     * Don't let anyone instantiate this class.
     */
    private Math() {
    }

    public static final double E = 2.7182818284590452354;

    public static final double PI = 3.14159265358979323846;

    private static final double DEGREES_TO_RADIANS = 0.017453292519943295;

    private static final double RADIANS_TO_DEGREES = 57.29577951308232;

    /**
     * JVM intrinsics
     */
    public static double avm_sin(double a) {
        return java.lang.Math.sin(a);
    }

    public static double avm_cos(double a) {
        return java.lang.Math.cos(a);
    }

    public static double avm_tan(double a) {
        return java.lang.Math.tan(a);
    }

    public static double avm_exp(double a) {
        return java.lang.Math.exp(a);
    }

    public static double avm_log(double a) {
        return java.lang.Math.log(a);
    }

    public static double avm_log10(double a) {
        return java.lang.Math.log10(a);
    }

    public static double avm_sqrt(double a) {
        return java.lang.Math.sqrt(a);
    }

    public static double avm_atan2(double y, double x) {
        return java.lang.Math.atan2(y, x);
    }

    public static double avm_pow(double a, double b) {
        return java.lang.Math.pow(a, b);
    }

    public static long avm_multiplyHigh(long x, long y){
        return java.lang.Math.multiplyHigh(x, y);
    }

    // TODO: Port avm_....Exact

    public static int avm_addExact(int x, int y){
        return 0;
    }

    public static long avm_addExact(long x, long y){
        return 0;
    }

    public static int avm_subtractExact(int x, int y){
        return 0;
    }

    public static long avm_subtractExact(long x, long y){
        return 0;
    }

    public static int avm_multiplyExact(int x, int y) {
        return 0;
    }

    public static long avm_multiplyExact(long x, int y) {
        return 0;
    }

    public static long avm_multiplyExact(long x, long y) {
        return 0;
    }

    public static int avm_incrementExact(int x){
        return 0;
    }

    public static long avm_incrementExact(long x){
        return 0;
    }

    public static int avm_decrementExact(int x){
        return 0;
    }

    public static long avm_decrementExact(long x){
        return 0;
    }

    public static int avm_negateExact(int x){
        return 0;
    }

    public static long avm_negateExact(long x){
        return 0;
    }

    public static double avm_abs(double a) {
        return java.lang.Math.abs(a);
    }

    public static int avm_max(int a, int b) {
        return java.lang.Math.max(a, b);
    }

    public static int avm_min(int a, int b) {
        return java.lang.Math.min(a, b);
    }

    public static double avm_fma(double a, double b, double c){
        return java.lang.Math.fma(a, b, c);
    }

    public static float avm_fma(float a, float b, float c){
        return java.lang.Math.fma(a, b, c);
    }


    /**
     * For these methods, we call can java.lang.StrictMath directly when possible
     */
    public static double avm_asin(double a) {
        return StrictMath.asin(a);
    }

    public static double avm_acos(double a) {
        return StrictMath.acos(a);
    }

    public static double avm_atan(double a) {
        return StrictMath.atan(a);
    }

    public static double avm_toRadians(double angdeg) {
        return angdeg * DEGREES_TO_RADIANS;
    }

    public static double avm_toDegrees(double angrad) {
        return angrad * RADIANS_TO_DEGREES;
    }

    public static double avm_cbrt(double a) {
        return StrictMath.cbrt(a);
    }

    public static double avm_IEEEremainder(double f1, double f2) {
        return StrictMath.IEEEremainder(f1, f2);
    }

    public static double avm_ceil(double a) {
        return StrictMath.ceil(a);
    }

    public static double avm_floor(double a) {
        return StrictMath.floor(a);
    }

    public static double avm_rint(double a) {
        return StrictMath.rint(a);
    }

    public static int avm_round(float a){
        return java.lang.Math.round(a);
    }

    public static long avm_double(double a){
        return java.lang.Math.round(a);
    }

    public static long avm_multiplyFull(int x, int y) {
        return (long)x * (long)y;
    }

    public static int avm_floorDiv(int x, int y) {
        int r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    public static long avm_floorDiv(long x, int y) {
        return avm_floorDiv(x, (long)y);
    }

    public static long avm_floorDiv(long x, long y) {
        long r = x / y;
        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    public static int avm_floorMod(int x, int y) {
        return x - avm_floorDiv(x, y) * y;
    }

    public static int avm_floorMod(long x, int y) {
        // Result cannot overflow the range of int.
        return (int)(x - avm_floorDiv(x, y) * y);
    }

    public static long avm_floorMod(long x, long y) {
        return x - avm_floorDiv(x, y) * y;
    }

    public static int avm_abs(int a) {
        return (a < 0) ? -a : a;
    }

    public static long avm_abs(long a) {
        return (a < 0) ? -a : a;
    }

    public static float avm_abs(float a) {
        return (a <= 0.0F) ? 0.0F - a : a;
    }

    public static long avm_max(long a, long b) {
        return (a >= b) ? a : b;
    }

    public static float avm_max(float a, float b){
        return java.lang.Math.max(a, b);
    }

    public static double avm_max(double a, double b) {
        return java.lang.Math.max(a, b);
    }

    public static long avm_min(long a, long b) {
        return (a <= b) ? a : b;
    }

    public static float avm_min(float a, float b){
        return java.lang.Math.min(a, b);
    }

    public static double avm_min(double a, double b) {
        return java.lang.Math.min(a, b);
    }




    public static double avm_random(){
        // Reject?
        return 0;
    }


}
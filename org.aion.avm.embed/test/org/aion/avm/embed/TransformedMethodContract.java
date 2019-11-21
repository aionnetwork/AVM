package org.aion.avm.embed;

import org.aion.avm.tooling.abi.Callable;

import avm.Blockchain;

/**
 * Simple contract that explores different method signatures etc. for testing to ensure that our
 * transformed classes are consistent with Java behaviour.
 */
public class TransformedMethodContract {

    @Callable
    public static boolean tryPrimitiveBool(boolean b) {
        return getBool(b);
    }

    @Callable
    public static byte tryPrimitiveByte(byte b) {
        return getByte(b);
    }

    @Callable
    public static char tryPrimitiveChar(char c) {
        return getChar(c);
    }

    @Callable
    public static short tryPrimitiveShort(short s) {
        return getShort(s);
    }

    @Callable
    public static int tryPrimitiveInt(int i) {
        return getInt(i);
    }

    @Callable
    public static long tryPrimitiveLong(long l) {
        return getLong(l);
    }

    @Callable
    public static float tryPrimitiveFloat(float f) {
        return getFloat(f);
    }

    @Callable
    public static double tryPrimitiveDouble(double d) {
        return getDouble(d);
    }

    @Callable
    public static int triggerOverloadedF1() {
        return overloaded(new F[]{});
    }

    @Callable
    public static int triggerOverloadedF2() {
        return overloaded((F[]) new D[]{});
    }

    @Callable
    public static int triggerOverloadedF3() {
        return overloaded((F[]) new C[]{});
    }

    @Callable
    public static int triggerOverloadedF4() {
        return overloaded((F[]) new E[]{});
    }

    @Callable
    public static int triggerOverloadedD1() {
        return overloaded(new D[]{});
    }

    @Callable
    public static int triggerOverloadedD2() {
        return overloaded((D[]) new C[]{});
    }

    @Callable
    public static int triggerOverloadedD3() {
        return overloaded(new E[]{});
    }

    @Callable
    public static int triggerOverloadedC1() {
        return overloaded(new C[]{});
    }

    @Callable
    public static int triggerOverloadedC2() {
        return overloaded((C[]) new A[]{});
    }

    @Callable
    public static int triggerOverloadedB1() {
        return overloaded(new B[]{});
    }

    @Callable
    public static int triggerOverloadedB2() {
        return overloaded((B[]) new A[]{});
    }

    @Callable
    public static int triggerDistill() {
        return overloaded(distill(new F[]{ new A() }))
            + overloaded(distill(new F[]{ new E() }))
            + overloaded(distill(new F[]{ new A(), new E() }));
    }

    @Callable
    public static int triggerUpcast() {
        return overloaded(upcast(new A[]{}));
    }

    @Callable
    public static int triggerMixedTypes() {
        F[] f = new C[]{ new A() };
        int i = 89;
        C[] c1 = new C[]{}, c2 = new C[]{ new A(), new A() };
        long l = 8732346436L;
        Boolean b1 = Boolean.TRUE;
        Boolean[] b2 = new Boolean[]{ false, true, true };
        boolean[] b3 = new boolean[]{ true, true, false };
        D[] d = new D[]{};

        return overloaded(mixedTypes(f, i, c1, c2, l, b1, b2, b3, d));
    }

    @Callable
    public static int trigger2Darrays() {
        F[][] receive = arrays2D(new C[][]{});
//        fails --> overloaded(arrays2D(new C[][]{new C[]{}})[0]);
        Blockchain.require(null != receive);
        return 0;
    }

    @Callable
    public static int trigger3Darrays() {
        F[][][] receive = arrays3D(new C[][][]{});
        Blockchain.require(null != receive);
        return 0;
    }

    /**
     * Ensure that these multi-dimensional type conversions are legal.
     */
    @Callable
    public static int triggerMultiDimPrimitiveToObjectArray() {
        byte[][][] b = new byte[][][]{};
        tryAsMultiDimObjectArray(b);
        boolean[][][] b2 = new boolean[][][]{};
        tryAsMultiDimObjectArray(b2);
        char[][][] c = new char[][][]{};
        tryAsMultiDimObjectArray(c);
        short[][][] s = new short[][][]{};
        tryAsMultiDimObjectArray(s);
        int[][][] i = new int[][][]{};
        tryAsMultiDimObjectArray(i);
        float[][][] f = new float[][][]{};
        tryAsMultiDimObjectArray(f);
        long[][][] l = new long[][][]{};
        tryAsMultiDimObjectArray(l);
        double[][][] d = new double[][][]{};
        tryAsMultiDimObjectArray(d);
        return 0;
    }

    // ================== the methods to call ======================

    private static void tryAsMultiDimObjectArray(Object[][] o) {}

    @Callable
    public static void nothing() {}

    // booleans

    private static boolean getBool(boolean b) {
        return b;
    }

    @Callable
    public static boolean collapseBools(boolean b1, boolean b2, boolean b3, boolean b4) {
        return b1 ^ b2 ^ b3 ^ b4;
    }

    // bytes

    private static byte getByte(byte b) {
        return b;
    }

    @Callable
    public static byte collapseBytes(byte b1, byte b2, byte b3, byte b4) {
        return (byte) (b1 + b2 + b3 + b4);
    }

    // chars

    private static char getChar(char c) {
        return c;
    }

    @Callable
    public static char collapseChars(char c1, char c2, char c3, char c4) {
        return (char) (c1 ^ c2 ^ c3 ^ c4);
    }

    // shorts

    private static short getShort(short s) {
        return s;
    }

    @Callable
    public static short collapseShorts(short s1, short s2, short s3, short s4) {
        return (short) (s1 + s2 + s3 + s4);
    }

    // ints

    private static int getInt(int i) {
        return i;
    }

    @Callable
    public static int collapseInts(int i1, int i2, int i3, int i4) {
        return i1 + i2 + i3 + i4;
    }

    // longs

    private static long getLong(long l) {
        return l;
    }

    @Callable
    public static long collapseLongs(long l1, long l2, long l3, long l4) {
        return l1 + l2 + l3 + l4;
    }

    // float

    private static float getFloat(float f) {
        return f;
    }

    // doubles

    private static double getDouble(double d) {
        return d;
    }

    @Callable
    public static double collapseDoubles(double d1, double d2, double d3, double d4) {
        return d1 + d2 + d3 + d4;
    }

    // arrays

    private static int overloaded(F[] f) {
        return 0;
    }

    private static int overloaded(D[] d) {
        return 1;
    }

    private static int overloaded(C[] c) {
        return 2;
    }

    private static int overloaded(B[] b) {
        return 3;
    }

    private static C[] distill(F[] f) {
        return new C[]{};
    }

    private static F[] upcast(A[] a) {
        return a;
    }

    private static B[] mixedTypes(F[] f, int i, C[] c1, C[] c2, long l, Boolean b1, Boolean[] b2, boolean[] b3, D[] d) {
        return new A[]{new A()};
    }

    private static F[][] arrays2D(C[][] c) {
//        return (F[][]) c;
        return c;
    }

    private static F[][][] arrays3D(C[][][] c) {
        return c;
    }

//    // classes

    private interface F {}

    private interface D extends F {}

    private interface B {}

    private static abstract class C implements D {}

    private static class E implements D {}

    private static class A extends C implements B {}

}

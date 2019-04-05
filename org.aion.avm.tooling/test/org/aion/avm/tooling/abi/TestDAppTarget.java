package org.aion.avm.tooling.abi;

import avm.Address;

public class TestDAppTarget {

    @Callable
    public static String returnHelloWorld() {
        return "Hello world";
    }

    @Callable
    public static String returnGoodbyeWorld() {
        return "Goodbye world";
    }

    @Callable
    public static String returnEcho(String s) {
        return s;
    }

    @Callable
    public static Address returnEchoAddress(Address s) {
        return s;
    }

    @Callable
    public static String returnAppended(String s1, String s2) {
        return s1 + s2;
    }

    @Callable
    public static String returnAppendedMultiTypes(String s1, String s2, boolean b, int l) {
        return s1 + s2 + b + l;
    }

    @Callable
    public static int[] returnArrayOfInt(int i1, int i2, int i3) {
        return new int[]{i1, i2, i3};
    }

    @Callable
    public static String[] returnArrayOfString(String v1, String v2, String v3) {
        return new String[]{v1, v2, v3};
    }

    @Callable
    public static int[] returnArrayOfIntEcho(int[] array) {
        return array;
    }

    @Callable
    public static int[][] returnArrayOfInt2D(int i1, int i2, int i3, int i4) {
        int[][] ret = new int[2][2];
        ret[0][0] = i1;
        ret[0][1] = i2;
        ret[1][0] = i3;
        ret[1][1] = i4;
        return ret;
    }

    @Callable
    public static int[][] returnArrayOfInt2DEcho(int[][] arr2D) {
        return arr2D;
    }

    @Callable
    public static void doNothing() {
    }
}

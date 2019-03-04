package org.aion.avm.tooling.abi;

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
    public static String returnAppended(String s1, String s2) {
        return s1 + s2;
    }

    @Callable
    public static String returnAppendedMultiTypes(String s1, String s2, Boolean b, Integer l) {
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
}

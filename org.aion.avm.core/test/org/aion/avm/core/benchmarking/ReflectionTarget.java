package org.aion.avm.core.benchmarking;

import java.util.List;
import java.util.Random;

public class ReflectionTarget {
    public static Long staticField1;
    public static char staticField2;
    public static Boolean staticField3;
    public static List<?> staticField4;

    public int instanceField1;
    public Object instanceField2;
    public String instanceField3;
    public double instanceField4;

    public ReflectionTarget() {}

    public ReflectionTarget(String s1, String s2, int i) {}

    public ReflectionTarget(String s, Object o, Character c, Float... f) {}

    public String instanceMethod1() { return null; }

    public Character instanceMethod2() { return null; }

    public void instanceMethod3() {}

    public void instanceMethod4(Integer i, Object o, float f) {}

    public static int staticMethod1() { return 0; }

    public static void staticMethod2() {}

    public static long staticMethod3(Long l) { return 1; }

    public static Object staticMethod4(Number n, Random r) { return null; }

}

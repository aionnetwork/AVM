package org.aion.avm.core.persistence;


/**
 * Used within LoadedDAppTest.
 * Note that these all must be public, due to reflection restrictions on modern JDK.
 */
public class LoadedDAppTarget extends s.java.lang.Object {
    public static boolean s_one;
    public static byte s_two;
    public static short s_three;
    public static char s_four;
    public static int s_five;
    public static float s_six;
    public static long s_seven;
    public static double s_eight;
    // We will allow ourselves to store an arbitrary object to test instance mapping of JDK constants, etc.
    public static s.java.lang.Object s_nine;
}

package org.aion.avm.core.persistence;


/**
 * Used within ReflectionStructureCodecTest and LoadedDAppTest.
 * This is a sub-class of ReflectionStructureCodecTarget with the same fields and names to make sure that this isn't a serialization issue.
 */
public class ReflectionStructureCodecTargetSub extends ReflectionStructureCodecTarget {
    public static boolean s_one;
    public static byte s_two;
    public static short s_three;
    public static char s_four;
    public static int s_five;
    public static float s_six;
    public static long s_seven;
    public static double s_eight;
    public static ReflectionStructureCodecTargetSub s_nine;

    public boolean i_one;
    public byte i_two;
    public short i_three;
    public char i_four;
    public int i_five;
    public float i_six;
    public long i_seven;
    public double i_eight;
    public ReflectionStructureCodecTargetSub i_nine;

    // Normal constructor.
    public ReflectionStructureCodecTargetSub() {
    }

    // We need to manually define the deserialization constructor since we aren't a transformed test.
    public ReflectionStructureCodecTargetSub(Void ignore, int readIndex) {
        super(ignore, readIndex);
    }
}

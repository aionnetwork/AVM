package org.aion.avm.core.persistence;

import org.aion.avm.internal.IDeserializer;


/**
 * Used within ReflectionStructureCodecTest.
 * Note that these all must be public, due to reflection restrictions on modern JDK.
 */
public class ReflectionStructureCodecTarget extends org.aion.avm.shadow.java.lang.Object {
    public static boolean s_one;
    public static byte s_two;
    public static short s_three;
    public static char s_four;
    public static int s_five;
    public static float s_six;
    public static long s_seven;
    public static double s_eight;
    public static ReflectionStructureCodecTarget s_nine;

    public boolean i_one;
    public byte i_two;
    public short i_three;
    public char i_four;
    public int i_five;
    public float i_six;
    public long i_seven;
    public double i_eight;
    public ReflectionStructureCodecTarget i_nine;

    // Normal constructor.
    public ReflectionStructureCodecTarget() {
    }

    // We need to manually define the deserialization constructor since we aren't a transformed test.
    public ReflectionStructureCodecTarget(IDeserializer deserializer, int hashCode, long instanceId) {
        super(deserializer, hashCode, instanceId);
    }
}

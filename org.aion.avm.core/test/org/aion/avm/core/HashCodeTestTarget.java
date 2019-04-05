package org.aion.avm.core;

import avm.Address;


/**
 * The test class loaded by HashCodeTest.
 */
public class HashCodeTestTarget {
    // If this is 0, we will use the built-in hashcode.  Otherwise, we will use this value.
    private final int hashCode;
    
    public HashCodeTestTarget() {
        // Empty instance - we just create these for their hashcodes.
        this.hashCode = 0;
    }
    
    public HashCodeTestTarget(int hashCode) {
        // Set this override.
        this.hashCode = hashCode;
    }
    
    @Override
    public int hashCode() {
        return (0 == this.hashCode)
                ? super.hashCode()
                : this.hashCode;
    }
    
    public static int getOneHashCode() {
        return new HashCodeTestTarget().hashCode();
    }
    
    public static String getStringConstant() {
        return "single string constant";
    }
    
    public static int getStringHash() {
        return getStringConstant().hashCode();
    }
    
    public static Class<?> getClassConstant() {
        return HashCodeTestTarget.class;
    }
    
    private static NullPointerException initialVmThrow;
    public static boolean matchRethrowVmException() {
        boolean didMatch = false;
        try {
            innerCallThrow();
        } catch (NullPointerException e) {
            didMatch = (initialVmThrow == e);
        }
        return didMatch;
    }
    
    private static int innerCallThrow() {
        try {
            return ((Object)null).hashCode();
        } catch (NullPointerException e) {
            initialVmThrow = e;
            throw e;
        }
    }
    
    public static boolean compareClassName() {
        String name1 = HashCodeTestTarget.class.getName();
        String name2 = HashCodeTestTarget.class.getName();
        return name1 == name2;
    }
    
    public static boolean compareStringString() {
        String testing = "string constant";
        String name1 = testing.toString();
        String name2 = testing.toString();
        return name1 == name2;
    }
    
    public static int getOverrideHashCode(int override) {
        return new HashCodeTestTarget(override).hashCode();
    }
    
    public static int runUntilExhausted() {
        int result = 0;
        try {
            while (result >= 0) {
                result += 1;
            }
        } finally {
            // Note that we should fail to execute this since we should re-throw on out of energy.
            result = 1;
        }
        return result;
    }
    
    public static int lengthOfClonedByteArray() {
        byte[] original = new byte[] {1,2,3};
        byte[] copy = original.clone();
        return copy.length;
    }
    
    public static Address createAddress(byte[] data) {
        return new Address(data);
    }
    
    public static boolean compareAddresses(Address outside, Address inside) {
        return (outside.hashCode() == inside.hashCode())
                && (outside.equals(inside));
    }
    
    public static int diffByteHashes() {
        return diffHashes(Byte.valueOf((byte)1), Byte.valueOf((byte)1));
    }
    
    public static int diffCharHashes() {
        return diffHashes(Character.valueOf('a'), Character.valueOf('a'));
    }
    
    public static int diffDoubleHashes() {
        return diffHashes(Double.valueOf(5.0d), Double.valueOf(5.0d));
    }
    
    public static int diffFloatHashes() {
        return diffHashes(Float.valueOf(5.0f), Float.valueOf(5.0f));
    }
    
    public static int diffIntegerHashes() {
        return diffHashes(Integer.valueOf(5), Integer.valueOf(5));
    }
    
    public static int diffLongHashes() {
        return diffHashes(Long.valueOf(5L), Long.valueOf(5L));
    }
    
    public static int diffShortHashes() {
        return diffHashes(Short.valueOf((short)5), Short.valueOf((short)5));
    }
    
    
    private static int diffHashes(Object one, Object two) {
        // The test requires that these not be the same instance.
        if (one == two) {
            throw new AssertionError();
        }
        return one.hashCode() ^ two.hashCode();
    }
}

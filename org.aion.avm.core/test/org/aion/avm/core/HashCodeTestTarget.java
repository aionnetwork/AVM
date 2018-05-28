package org.aion.avm.core;


/**
 * The test class loaded by HashCodeTest.
 */
public class HashCodeTestTarget {
    public HashCodeTestTarget() {
        // Empty instance - we just create these for their hashcodes.
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
}

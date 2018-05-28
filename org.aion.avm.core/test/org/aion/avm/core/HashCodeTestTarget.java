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
}

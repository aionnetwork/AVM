package org.aion.avm.core.shadowing.misc;


public class TestResource {
    public static Object returnObject() {
        return new Object();
    }

    public static String returnString() {
        return "hello";
    }

    public static Class<?> returnClass() {
        return String.class;
    }

    public static boolean cast(Class<?> clazz, Object instance) {
        boolean success = false;
        try {
            clazz.cast(instance);
            success = true;
        } catch (ClassCastException e) {
            // Some tests are looking for this.  We want to prove we throw it in the right cases but can also catch it here.
            success = false;
        }
        return success;
    }
}

package org.aion.avm.core.util;


/**
 * Common assertions for general correctness verification.
 */
public class Assert {
    public static void assertTrue(boolean statement) {
        if (!statement) {
            throw new AssertionError("Statement MUST be true");
        }
    }

    public static void assertNull(Object object) {
        if (null != object) {
            throw new AssertionError("Object MUST be null");
        }
    }

    public static void assertNotNull(Object object) {
        if (null == object) {
            throw new AssertionError("Object MUST not be null");
        }
    }

    public static void unreachable(String message) {
        throw new AssertionError("Unreachable code reached: " + message);
    }
}

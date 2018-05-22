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

    /**
     * Note that we unimplemented paths are mostly just to enable incremental development and deep
     * prototyping.  Cutting off a path with unimplemented will make it easier for us to find, later.
     * 
     * @param message The message to describe why this is unimplemented.
     */
    public static void unimplemented(String message) {
        throw new AssertionError("Unimplemented path: " + message);
    }

    public static void unexpected(Throwable t) {
        throw new AssertionError("Unexpected Throwable", t);
    }
}

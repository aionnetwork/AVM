package org.aion.avm.internal;


/**
 * A class of our fatal errors specifically for internal assertion failures.
 * Instances of this class are only intended to be instantiated from its static helpers.
 */
public class RuntimeAssertionError extends FatalAvmError {
    private static final long serialVersionUID = 1L;

    public static void assertTrue(boolean statement) {
        if (!statement) {
            throw new RuntimeAssertionError("Statement MUST be true");
        }
    }

    /**
     * Note that this method internally throws, but also defines that it will return the exception, so the caller can satisfy
     * the compiler by throwing the response (important for reachability detection).
     * This idea is useful in cases where all paths throw.
     */
    public static RuntimeAssertionError unexpected(Throwable t) {
        throw new RuntimeAssertionError("Unexpected Throwable", t);
    }


    public RuntimeAssertionError(String message) {
        super(message);
    }

    public RuntimeAssertionError(String message, Throwable cause) {
        super(message, cause);
    }
}

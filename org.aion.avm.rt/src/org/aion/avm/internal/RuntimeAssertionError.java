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

    public static void unexpected(Throwable t) {
        throw new RuntimeAssertionError("Unexpected Throwable", t);
    }


    private RuntimeAssertionError(String message) {
        super(message);
    }

    private RuntimeAssertionError(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.aion.avm.abi.internal;

public class RuntimeAssertionError extends RuntimeException{
    private static final long serialVersionUID = 1L;

    /**
     * Verifies that a statement is true, throwing RuntimeAssertionError if not.
     *
     * @param statement The statement to check.
     */
    public static void assertTrue(boolean statement) {
        if (!statement) {
            throw new RuntimeAssertionError("Statement MUST be true");
        }
    }

    /**
     * Called when a code-path thought impossible to enter is executed.  In general, this is used to denote that an interface
     * method is not called in a certain configuration/implementation.
     *
     * @param message The message explaining why this shouldn't be called.
     * @return The thrown exception (for caller reachability convenience).
     */
    public static RuntimeAssertionError unreachable(String message) {
        throw new RuntimeAssertionError("Unreachable code reached: " + message);
    }


    private RuntimeAssertionError(String message) {
        super(message);
    }

}

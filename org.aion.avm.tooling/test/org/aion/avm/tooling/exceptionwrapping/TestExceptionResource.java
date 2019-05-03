package org.aion.avm.tooling.exceptionwrapping;

import org.aion.avm.tooling.abi.Callable;

/**
 * Note that this class is just used as a resource by the other tests in this package.
 */
public class TestExceptionResource {

    @Callable
    public static void tryCatchLoop(int count) {
        for (int i = 0; i < count; i++) {
            try {
                ((Object) null).hashCode();
            } catch (Exception e) {
            }
        }
    }

    @Callable
    public static int tryMultiCatchFinally() {
        int r = 0;
        try {
            r = 1;
            // Cause the throw to happen.
            r = ((Object)null).hashCode();
        } catch (NullPointerException | IllegalArgumentException e) {
            // Make sure that we call something which only an exception could have.
            e.getCause();
            r = 2;
        } finally {
            r = 3;
        }
        return r;
    }

    /**
     * This method tests that we actually did go into the exception hander.
     * The result will be 2.
     */
    @Callable
    public static int tryMultiCatch() {
        int r = 0;
        try {
            r = 1;
            // Cause the throw to happen.
            r = ((Object)null).hashCode();
        } catch (NullPointerException | IllegalArgumentException e) {
            // Make sure that we call something which only an exception could have.
            e.getCause();
            r = 2;
        }
        return r;
    }

    /**
     * We this calls the innerCatch, below, to make sure that re-throwing VM-generated exceptions works.
     */
    @Callable
    public static int outerCatch() {
        int result = 0;
        try {
            innerCatch();
            result = 1;
        } catch (NullPointerException | IllegalArgumentException e) {
            // We expect this.
            result = 2;
        } catch (Throwable t) {
            // We shouldn't end up here.
            result = 3;
        }
        return result;
    }

    /**
     * Makes sure that we can re-throw an exception.
     */
    @Callable
    public static void innerCatch() {
        try {
            // Cause the throw to happen.
            ((Object)null).hashCode();
        } catch (NullPointerException | IllegalArgumentException e) {
            // Re-throw.
            throw e;
        }
    }

    @Callable
    public static void manuallyThrowNull() {
        throw new NullPointerException("faked");
    }

    @Callable
    public static String userDefinedCatch() {
        String result = "one";
        try {
            userDefinedThrow("two");
        } catch (UserDefinedException e) {
            result = e.getMessage();
        }
        return result;
    }

    @Callable
    public static void userDefinedThrow(String message) throws UserDefinedException {
        throw new UserDefinedException(message);
    }

    @Callable
    public static void userDefinedThrowRuntime(String message) {
        throw new UserDefinedRuntimeException(message);
    }

    /**
     * Used to demonstrate what happens when an NPE is thrown by the VM and we don't catch it (issue-141).
     */
    @Callable
    public static void originalNull() {
        ((Object)null).hashCode();
    }


    public static class UserDefinedException extends Throwable {
        private static final long serialVersionUID = 1L;
        public UserDefinedException(String message) {
            super(message);
        }
    }

    public static class UserDefinedRuntimeException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public UserDefinedRuntimeException(String message) {
            super(message);
        }
    }
}

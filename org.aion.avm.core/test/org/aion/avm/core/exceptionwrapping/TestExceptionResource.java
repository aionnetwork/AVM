package org.aion.avm.core.exceptionwrapping;


/**
 * Note that this class is just used as a resource by the other tests in this package.
 */
public class TestExceptionResource {
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
    public static void innerCatch() {
        try {
            // Cause the throw to happen.
            ((Object)null).hashCode();
        } catch (NullPointerException | IllegalArgumentException e) {
            // Re-throw.
            throw e;
        }
    }

    public static void manuallyThrowNull() {
        throw new NullPointerException("faked");
    }

    public static String userDefinedCatch() {
        String result = "one";
        try {
            userDefinedThrow("two");
        } catch (UserDefinedException e) {
            result = e.getMessage();
        }
        return result;
    }

    public static void userDefinedThrow(String message) throws UserDefinedException {
        throw new UserDefinedException(message);
    }

    public static class UserDefinedException extends Throwable {
        private static final long serialVersionUID = 1L;
        public UserDefinedException(String message) {
            super(message);
        }
    }
}

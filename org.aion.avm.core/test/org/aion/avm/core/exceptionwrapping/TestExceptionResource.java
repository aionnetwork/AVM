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
}

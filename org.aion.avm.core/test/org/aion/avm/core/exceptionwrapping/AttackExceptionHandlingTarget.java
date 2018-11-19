package org.aion.avm.core.exceptionwrapping;


/**
 * A test which tries various techniques to attack the system by running code in generic handlers or finally blocks.
 * All it does is run a simple loop, making it easier to determine where/when the failure should occur.
 */
public class AttackExceptionHandlingTarget {
    public static byte[] main() {
        byte[] result = new byte[1];
        try {
            runLoopForever();
            // Above method never returns.
            result[0] = 1;
        } catch (Throwable t) {
            // We should fail to actually do anything, in this case.
            result[0] = 2;
        } finally {
            // Similarly, we shouldn't be able to actually run any code.
            result[0] = 3;
        }
        return result;
    }

    private static void runLoopForever() {
        Object foo = new Object();
        // This comparison is just to avoid unreachability warnings/errors.
        while (null != foo) {
            foo = new Object();
        }
    }
}

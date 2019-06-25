package org.aion.avm.embed;

import org.aion.avm.tooling.abi.Callable;

/**
 * The test class loaded by AssertKeywordIntegrationTest.
 */
public class AssertKeywordIntegrationTestTarget {
    private static boolean didCheck;
    private static boolean shouldFail;

    @Callable
    public static boolean getAndClearState() {
        boolean val = didCheck;
        didCheck = false;
        return val;
    }

    @Callable
    public static boolean setShouldFail(boolean fail) {
        boolean previous = shouldFail;
        shouldFail = fail;
        return previous;
    }

    private static boolean doRunCheck() {
        didCheck = true;
        return !shouldFail;
    }

    @Callable
    public static int runEmptyCheck() {
        try {
            assert doRunCheck();
            return -1;
        } catch (AssertionError e) {
            String message = e.getMessage();
            return (null != message)
                    ? message.length()
                    : 0;
        }
    }

    @Callable
    public static int runIntCheck(int value) {
        try {
            assert doRunCheck() : value;
            return -1;
        } catch (AssertionError e) {
            String message = e.getMessage();
            return (null != message)
                    ? message.length()
                    : 0;
        }
    }
}

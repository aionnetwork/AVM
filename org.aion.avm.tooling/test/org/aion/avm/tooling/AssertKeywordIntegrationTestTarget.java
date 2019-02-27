package org.aion.avm.tooling;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * The test class loaded by AssertKeywordIntegrationTest.
 */
public class AssertKeywordIntegrationTestTarget {
    private static boolean didCheck;
    private static boolean shouldFail;

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(AssertKeywordIntegrationTestTarget.class, BlockchainRuntime.getData());
    }

    public static boolean getAndClearState() {
        boolean val = didCheck;
        didCheck = false;
        return val;
    }

    public static boolean setShouldFail(boolean fail) {
        boolean previous = shouldFail;
        shouldFail = fail;
        return previous;
    }

    private static boolean doRunCheck() {
        didCheck = true;
        return !shouldFail;
    }

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

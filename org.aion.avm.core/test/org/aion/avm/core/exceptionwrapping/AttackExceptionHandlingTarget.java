package org.aion.avm.core.exceptionwrapping;

import java.math.BigInteger;

import avm.Blockchain;


/**
 * A test which tries various techniques to attack the system by running code in generic handlers or finally blocks.
 * All it does is run a simple loop, making it easier to determine where/when the failure should occur.
 */
public class AttackExceptionHandlingTarget {
    public static byte[] main() {
        byte[] input = Blockchain.getData();
        byte[] result = new byte[1];
        try {
            // See if we should invoke ourselves.
            if (input.length > 0) {
                // Yes, call ourselves with an empty array.
                BigInteger value = BigInteger.ZERO;
                byte[] data = new byte[0];
                long energyLimit = 500_000L;
                Blockchain.call(Blockchain.getAddress(), value, data, energyLimit);
            } else {
                // No, just run the loop, ourselves.
                runLoopForever();
            }
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

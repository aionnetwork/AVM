package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * A test created as part of issue-167 to test out re-entrance concerns when a DApp calls itself.
 */
public class ReentrantCrossCallResource {
    // We use these to verify the commit/rollback of object graph state, during reentrant calls.
    private static int direct = 1;
    private static ReentrantCrossCallResource constant = new ReentrantCrossCallResource();
    private int near = 1;
    private int[] far = new int[] {1};


    public static byte[] main() {
        byte[] input = BlockchainRuntime.getData();
        return ABIDecoder.decodeAndRunWithObject(new ReentrantCrossCallResource(), input);
    }

    public static Object callSelfForNull() {
        // Call this method via the runtime.
        long value = 1;
        byte[] data = ABIEncoder.encodeMethodArguments("returnNull");
        long energyLimit = 500000;
        byte[] response = BlockchainRuntime.call(BlockchainRuntime.getAddress(), value, data, energyLimit);
        return (null != response)
                ? ABIDecoder.decodeOneObject(response)
                : response;
    }

    public static Object returnNull() {
        return null;
    }

    public static int getRecursiveHashCode(int iterationsRemaining) {
        Object object = new Object();
        int toReturn = 0;
        if (0 == iterationsRemaining) {
            toReturn = object.hashCode();
        } else {
            // Call this method via the runtime.
            long value = 1;
            byte[] data = ABIEncoder.encodeMethodArguments("getRecursiveHashCode", iterationsRemaining - 1);
            long energyLimit = 500000;
            byte[] response = BlockchainRuntime.call(BlockchainRuntime.getAddress(), value, data, energyLimit);
            toReturn = (Integer)ABIDecoder.decodeOneObject(response);
        }
        return toReturn;
    }

    // The get(Direct/Near/Far) all act by calling the corresponding "inc*" method, as a reentrant runtime call, and checking
    // the change in the caller's local state, upon return.
    // An expectation is determined, ahead-of-time, and either the new number (on success) or zero (on failure) is returned.
    public static int getDirect(boolean shouldFail) {
        // Cache the original answer to make sure the increment happens correctly.
        int expected = shouldFail
                ? direct
                : direct + 1;
        
        // Call ourselves.
        reentrantCall("incDirect", shouldFail);
        
        // If this matches expectation, return the new value, otherwise we return 0;
        return (expected == direct)
                ? direct
                : 0;
    }

    public static int getNear(boolean shouldFail) {
        // Cache the original answer to make sure the increment happens correctly.
        int expected = shouldFail
                ? constant.near
                : constant.near + 1;
        
        // Call ourselves.
        reentrantCall("incNear", shouldFail);
        
        // If this matches expectation, return the new value, otherwise we return 0;
        return (expected == constant.near)
                ? constant.near
                : 0;
    }

    public static int getFar(boolean shouldFail) {
        // Cache the original answer to make sure the increment happens correctly.
        int expected = shouldFail
                ? constant.far[0]
                : constant.far[0] + 1;
        
        // Call ourselves.
        reentrantCall("incFar", shouldFail);
        
        // If this matches expectation, return the new value, otherwise we return 0;
        return (expected == constant.far[0])
                ? constant.far[0]
                : 0;
    }

    /**
     * This case calls incFar, as a successful reentrant call, then fails in itself.
     * @return False if the reentrant call didn't observably change state (otherwise, fails - never returns true).
     */
    public static boolean localFailAfterReentrant() {
        // Cache the original answer to make sure the increment happens correctly.
        int expected = constant.far[0] + 1;
        
        // Call ourselves.
        reentrantCall("incFar", false);
        
        boolean doesMatch = (expected == constant.far[0]);
        if (doesMatch) {
            // This is the expected case so fail.
            causeFailure();
        }
        // We only get this far is something incorrect happened in the reentrant call (doesMatch being false).
        return doesMatch;
    }

    public static void incDirect(boolean shouldFail) {
        direct += 2;
        if (shouldFail) {
            causeFailure();
        }
        direct -= 1;
    }

    public static void incNear(boolean shouldFail) {
        constant.near += 2;
        if (shouldFail) {
            causeFailure();
        }
        constant.near -= 1;
    }

    public static void incFar(boolean shouldFail) {
        constant.far[0] += 2;
        if (shouldFail) {
            causeFailure();
        }
        constant.far[0] -= 1;
    }

    private static void reentrantCall(String methodName, boolean shouldFail) {
        long value = 1;
        byte[] data = ABIEncoder.encodeMethodArguments(methodName, shouldFail);
        long energyLimit = 5000;
        BlockchainRuntime.call(BlockchainRuntime.getAddress(), value, data, energyLimit);
    }

    // We should probably replace this with some kind of explicit failure mechanism, once one exists.
    // For now, our only want to ensure failure is to drain our energy.
    private static void causeFailure() {
        while (true) {
            new Object();
        }
    }
}

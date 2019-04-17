package org.aion.avm.core;

import java.math.BigInteger;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;


/**
 * A test created as part of issue-167 to test out re-entrance concerns when a DApp calls itself.
 */
public class ReentrantCrossCallResource {
    // We use these to verify the commit/rollback of object graph state, during reentrant calls.
    private static int direct = 1;
    private static ReentrantCrossCallResource constant = new ReentrantCrossCallResource();
    private static Nested nestedInstance;

    private int near = 1;
    private int[] far = new int[] {1};


    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("getNear")) {
                return ABIEncoder.encodeOneInteger(getNear(decoder.decodeOneBoolean()));
            } else if (methodName.equals("getFar")) {
                return ABIEncoder.encodeOneInteger(getFar((decoder.decodeOneBoolean())));
            } else if (methodName.equals("getDirect")) {
                return ABIEncoder.encodeOneInteger(getDirect(decoder.decodeOneBoolean()));
            } else if (methodName.equals("localFailAfterReentrant")) {
                return ABIEncoder.encodeOneBoolean(localFailAfterReentrant());
            } else if (methodName.equals("getFarWithEnergy")) {
                return ABIEncoder.encodeOneBoolean(getFarWithEnergy(decoder.decodeOneLong()));
            } else if (methodName.equals("recursiveChangeNested")) {
                return ABIEncoder.encodeOneInteger(recursiveChangeNested(decoder.decodeOneInteger(), decoder.decodeOneInteger()));
            } else if (methodName.equals("getRecursiveHashCode")) {
                return ABIEncoder.encodeOneInteger(getRecursiveHashCode(decoder.decodeOneInteger()));
            } else if (methodName.equals("incFar")) {
                incFar(decoder.decodeOneBoolean());
                return new byte[0];
            } else if (methodName.equals("incNear")) {
                incNear(decoder.decodeOneBoolean());
                return new byte[0];
            } else if (methodName.equals("incDirect")) {
                incDirect(decoder.decodeOneBoolean());
                return new byte[0];
            } else if (methodName.equals("callSelfForNull")) {
                Object obj = callSelfForNull();
                if (null == obj) {
                    // if the response is null, we correctly encode null as an empty byte array
                    return new byte[0];
                } else {
                    Blockchain.println(obj + "");
                    // if it is something else, something went wrong, so we fail by returning null
                    return null;
                }
            } else if (methodName.equals("returnNull")) {
                // We bypass the function call and directly return null
                return null;
            } else {
                return new byte[0];
            }
        }
    }

    public static Object callSelfForNull() {
        // Call this method via the runtime.
        BigInteger value = BigInteger.ZERO;
        byte[] data = ABIEncoder.encodeOneString("returnNull");
        long energyLimit = 500000;
        return Blockchain.call(Blockchain.getAddress(), value, data, energyLimit).getReturnData();
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
            BigInteger value = BigInteger.ZERO;

            ABIStreamingEncoder encoder = new ABIStreamingEncoder();
            byte[] data = encoder.encodeOneString("getRecursiveHashCode")
                .encodeOneInteger(iterationsRemaining - 1)
                .toBytes();

            long energyLimit = 500000;
            byte[] response = Blockchain.call(Blockchain.getAddress(), value, data, energyLimit).getReturnData();
            ABIDecoder decoder = new ABIDecoder(response);
            toReturn = decoder.decodeOneInteger();
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

    public static boolean getFarWithEnergy(long energyLimit) {
        // Note that we ALWAYS expect this call to fail so we expect no change in the value.
        int expected = constant.far[0];
        
        // Call ourselves.
        BigInteger value = BigInteger.ZERO;
        boolean calleeShouldFail = false;

        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] data = encoder.encodeOneString("incFar")
            .encodeOneBoolean(calleeShouldFail)
            .toBytes();

        Blockchain.call(Blockchain.getAddress(), value, data, energyLimit);
        
        // Return true if this value changed.
        return (expected != constant.far[0]);
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

    /**
     * This case calls incFar, as a successful reentrant call, then fails in itself.
     * @return False if the reentrant call didn't observably change state (otherwise, fails - never returns true).
     */
    public static int recursiveChangeNested(int hashToCheck, int iterationsToCall) {
        // Check the state.
        if (0 == hashToCheck) {
            if (null != nestedInstance) {
                Blockchain.println("expected null");
                throw new AssertionError();
            }
        } else {
            if (hashToCheck != nestedInstance.hashCode()) {
                Blockchain.println("expected match");
                throw new AssertionError();
            }
            // We can also verify the state.
            if ((iterationsToCall + 1) != nestedInstance.data) {
                Blockchain.println("incorrect data");
                throw new AssertionError();
            }
        }
        
        // Install new state.
        Nested ourState = new Nested(iterationsToCall);
        nestedInstance = ourState;
        
        // Call recursive with our expectations based on the new state.
        if (iterationsToCall > 0) {
            // Make the reentrant call.
            BigInteger value = BigInteger.ZERO;

            ABIStreamingEncoder encoder = new ABIStreamingEncoder();
            byte[] data = encoder.encodeOneString("recursiveChangeNested")
                .encodeOneInteger(ourState.hashCode())
                .encodeOneInteger(iterationsToCall - 1)
                .toBytes();

            long energyLimit = 5_000_000L;
            Result txResult = Blockchain.call(Blockchain.getAddress(), value, data, energyLimit);
            byte[] result = txResult.getReturnData();
            if (txResult.isSuccess()) {

                ABIDecoder decoder = new ABIDecoder(result);
                int responseHash = decoder.decodeOneInteger();

                // Verify that we see this hash for the nestedInstance.
                if (responseHash != nestedInstance.hashCode()) {
                    Blockchain.println("response hash mismatch");
                  throw new AssertionError();
                }
                // Verify the data in the nestedInstance.
                if ((iterationsToCall - 1) != nestedInstance.data) {
                    Blockchain.println("response data mismatch");
                  throw new AssertionError();
                }
            }
            else {
                throw new IllegalStateException("Call depth limit is exceeded");
            }
            
            // Re-install our instance.
            nestedInstance = ourState;
        }
        
        // Return the hash of our state.
        return ourState.hashCode();
    }

    private static void reentrantCall(String methodName, boolean shouldFail) {
        BigInteger value = BigInteger.ZERO;

        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] data = encoder.encodeOneString(methodName).encodeOneBoolean(shouldFail).toBytes();

        // WARNING:  This number is finicky since some tests want to barely pass and others barely fail.
        long energyLimit = 1_000_000L;
        Blockchain.call(Blockchain.getAddress(), value, data, energyLimit);
    }

    // We should probably replace this with some kind of explicit failure mechanism, once one exists.
    // For now, our only want to ensure failure is to drain our energy.
    private static void causeFailure() {
        while (true) {
            new Object();
        }
    }


    public static class Nested {
        public Integer data;
        public Nested(Integer data) {
            this.data = data;
        }
    }
}

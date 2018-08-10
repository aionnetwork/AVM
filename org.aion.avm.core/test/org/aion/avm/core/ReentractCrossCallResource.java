package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * A test created as part of issue-167 to test out re-entrance concerns when a DApp calls itself.
 */
public class ReentractCrossCallResource {
    public static byte[] main() {
        byte[] input = BlockchainRuntime.getData();
        return ABIDecoder.decodeAndRun(new ReentractCrossCallResource(), input);
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
}

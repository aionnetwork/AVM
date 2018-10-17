package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * This test exists only to prove that a DApp cannot catch a decoding error.
 */
public class ABIFailureTestResource {
    public static byte[] main() {
        byte[] result = null;
        try {
            result = ABIDecoder.decodeAndRunWithClass(ABIFailureTestResource.class, BlockchainRuntime.getData());
        } catch (Throwable t) {
            // We don't expect to get here - this exception should be uncatchable.
            result = new byte[0];
        }
        return result;
    }
}

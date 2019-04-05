package org.aion.avm.core;

import java.math.BigInteger;
import avm.BlockchainRuntime;


/**
 * Tests how we handle different attempts to CREATE from within a clinit.
 */
public class RecursiveSpawnerResource {
    static {
        byte[] data = BlockchainRuntime.getData();
        // If more data was provided, create another.
        if (data.length > 0) {
            byte[] contractAddress = BlockchainRuntime.create(BigInteger.ZERO, data, BlockchainRuntime.getRemainingEnergy()).getReturnData();
            if (null == contractAddress) {
                // We want to fail since the call depth is too deep.
                throw new AssertionError();
            }
        }
    }

    public static byte[] main() {
        return new byte[0];
    }
}

package org.aion.avm.core;

import org.aion.avm.api.BlockchainRuntime;

public class DeploymentArgumentSmallTarget {

    static {
        byte[] data = BlockchainRuntime.getData();
        // This constant is the expected length of the data array containing the 5 expected deployment args
        BlockchainRuntime.require(918 == data.length);
    }

    public static byte[] main() {
        return new byte[0];
    }
}

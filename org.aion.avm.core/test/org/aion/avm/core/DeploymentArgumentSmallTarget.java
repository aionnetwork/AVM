package org.aion.avm.core;

import avm.BlockchainRuntime;

public class DeploymentArgumentSmallTarget {

    static {
        byte[] data = BlockchainRuntime.getData();
        // This constant is the expected length of the data array containing the 5 expected deployment args
        BlockchainRuntime.require(906 == data.length);
    }

    public static byte[] main() {
        return new byte[0];
    }
}

package org.aion.avm.core;

import avm.Blockchain;

public class DeploymentArgumentSmallTarget {

    static {
        byte[] data = Blockchain.getData();
        // This constant is the expected length of the data array containing the 5 expected deployment args
        Blockchain.require(899 == data.length);
    }

    public static byte[] main() {
        return new byte[0];
    }
}

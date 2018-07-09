package org.aion.avm.core.testCall;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

/**
 * Demo contract.
 */
public class Contract {
    public static byte[] main() {
        byte[] data = BlockchainRuntime.getData();
        if (data != null && data.length != 0) {
            BlockchainRuntime.call(new Address(data), 0, new byte[0], 1000);
        }

        return  "done".getBytes();
    }
}

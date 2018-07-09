package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class AvmImplTestResource {

    public static byte[] main() {
        byte[] calldata = BlockchainRuntime.getData();

        if (calldata != null && calldata.length == Address.LENGTH) {
            Address address = new Address(BlockchainRuntime.getData());
            long value = 1;
            byte[] data = new byte[0];
            long energyLimit = 500000;

            BlockchainRuntime.call(address, value, data, energyLimit);

            return "CALL".getBytes();
        }

        return "NORMAL".getBytes();
    }
}
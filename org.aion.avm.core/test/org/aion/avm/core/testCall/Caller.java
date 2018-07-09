package org.aion.avm.core.testCall;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;


public class Caller {
    private static Address genAddress(int n) {
        byte[] bytes = new byte[Address.LENGTH];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) n;
        }

        return new Address(bytes);
    }

    public static byte[] main() {
        byte[] bytes = new byte[Address.LENGTH];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = 1;
        }

        Address address = genAddress(1);
        long value = 2;
        byte[] data = "hello".getBytes();
        long energyLimit = 10000;

        return BlockchainRuntime.call(address, value, data, energyLimit);
    }

}

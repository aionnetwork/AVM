package org.aion.avm.core.testCall;

import java.math.BigInteger;
import avm.Address;
import avm.BlockchainRuntime;


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
        BigInteger value = BigInteger.TWO;
        byte[] data = "hello".getBytes();
        long energyLimit = 10000;

        return BlockchainRuntime.call(address, value, data, energyLimit).getReturnData();
    }

}

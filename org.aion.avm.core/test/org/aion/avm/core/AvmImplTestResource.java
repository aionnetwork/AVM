package org.aion.avm.core;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;

public class AvmImplTestResource {

    public static void init(){}

    public static byte[] main() {
        byte[] calldata = Blockchain.getData();

        if (calldata != null && calldata.length == Address.LENGTH) {
            Address address = new Address(Blockchain.getData());
            BigInteger value = BigInteger.ZERO;
            byte[] data = new byte[0];
            long energyLimit = 500000;

            Blockchain.call(address, value, data, energyLimit);

            return "CALL".getBytes();
        }

        return "NORMAL".getBytes();
    }
}
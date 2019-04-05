package org.aion.avm.core.testCall;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;

/**
 * Demo contract.
 */
public class Contract {
    public static byte[] main() {
        byte[] data = Blockchain.getData();
        if (data != null && data.length != 0) {
            Blockchain.call(new Address(data), BigInteger.ZERO, new byte[0], 1000);
        }

        return  "done".getBytes();
    }
}

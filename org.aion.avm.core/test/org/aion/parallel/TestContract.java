package org.aion.parallel;

import java.math.BigInteger;
import avm.Address;
import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

public class TestContract {

    static Address deployer;

    static {
        deployer = new Address(Blockchain.getCaller().toByteArray());
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("doTransfer")) {
                doTransfer();
                return new byte[0];
            } else if (methodName.equals("addValue")) {
                addValue();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }

    public static void doTransfer() {
        Blockchain.call(deployer, BigInteger.valueOf(1000), new byte[0], 100_000L);
    }

    public static void addValue() {
    }

}

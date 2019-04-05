package org.aion.avm.core;

import java.math.BigInteger;
import avm.Blockchain;
import avm.Result;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class ReentrantClassWrapperTestResource {

    private static Class stringClass;

    public static void recursiveStringClassCheck() {
        Class secondStringClass = String.class;
        if(stringClass != secondStringClass) {
            Blockchain.revert();
        }
    }

    public static void testStringClass() {
        stringClass = String.class;

        byte[] data = ABIEncoder.encodeOneString("recursiveStringClassCheck");
        Result result = Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data, Blockchain.getEnergyLimit());
        if(!result.isSuccess()) {
            Blockchain.revert();
        }
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("recursiveStringClassCheck")) {
                recursiveStringClassCheck();
                return new byte[0];
            } else if (methodName.equals("testStringClass")) {
                testStringClass();
                return new byte[0];
            } else {
                return new byte[0];
            }
        }
    }
}

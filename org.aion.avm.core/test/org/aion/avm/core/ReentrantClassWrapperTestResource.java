package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class ReentrantClassWrapperTestResource {

    private static Class stringClass;

    public static void recursiveStringClassCheck() {
        Class secondStringClass = String.class;
        if(stringClass != secondStringClass) {
            BlockchainRuntime.revert();
        }
    }

    public static void testStringClass() {
        stringClass = String.class;

        byte[] data = ABIEncoder.encodeMethodArguments("recursiveStringClassCheck");
        Result result = BlockchainRuntime.call(BlockchainRuntime.getAddress(), BigInteger.ZERO, data, BlockchainRuntime.getEnergyLimit());
        if(!result.isSuccess()) {
            BlockchainRuntime.revert();
        }
    }

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
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

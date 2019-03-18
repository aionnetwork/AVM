package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.Result;

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
        return ABIDecoder.decodeAndRunWithClass(ReentrantClassWrapperTestResource.class, BlockchainRuntime.getData());
    }
}

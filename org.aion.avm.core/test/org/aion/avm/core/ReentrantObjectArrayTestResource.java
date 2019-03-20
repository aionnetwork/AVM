package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.BlockchainRuntime;

public class ReentrantObjectArrayTestResource {

    private static String[] names = {"foo, bar"};

    public static void setString() {
        names[0] = "bar";
    }

    public static void testString() {
        byte[] data = ABIEncoder.encodeMethodArguments("setString");
        BlockchainRuntime.call(BlockchainRuntime.getAddress(), BigInteger.ZERO, data, BlockchainRuntime.getEnergyLimit());
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(ReentrantObjectArrayTestResource.class, BlockchainRuntime.getData());
    }
}

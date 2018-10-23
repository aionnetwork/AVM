package org.aion.parallel;

import java.math.BigInteger;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class TestContract {

    static Address deployer;

    static {
        deployer = new Address(BlockchainRuntime.getCaller().unwrap());
    }

    public static byte[] main() {

        return ABIDecoder.decodeAndRunWithClass(TestContract.class, BlockchainRuntime.getData());
    }

    public static void doTransfer() {
        BlockchainRuntime.call(deployer, BigInteger.valueOf(1000), new byte[0], 100_000L);
    }

    public static void addValue() {
    }

}

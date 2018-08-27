package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;


/**
 * A test DApp used in tests related to CREATE and CALL tests.
 * CREATE accepts a byte[] which is the code+args to pass to another create.
 * On CALL, creates a new DApp with its CREATE args and then invokes CALL on that, using the argument it was given.
 */
public class SpawnerDApp {
    private static final byte[] CODE_AND_ARGS;
    static {
        CODE_AND_ARGS = BlockchainRuntime.getData();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(SpawnerDApp.class, BlockchainRuntime.getData());
    }

    public static byte[] spawnAndCall(byte[] array) {
        byte[] contractAddress = BlockchainRuntime.create(1L, CODE_AND_ARGS, 1_000_000L).getReturnData();
        return BlockchainRuntime.call(new Address(contractAddress), 1L, array, 1_000_000L).getReturnData();
    }

    public static Address spawnOnly(boolean shouldFail) {
        byte[] contractAddress = BlockchainRuntime.create(1L, CODE_AND_ARGS, 1_000_000L).getReturnData();
        if (shouldFail) {
            BlockchainRuntime.invalid();
        }
        return new Address(contractAddress);
    }
}

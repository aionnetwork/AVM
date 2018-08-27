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
        Address result = BlockchainRuntime.create(1L, CODE_AND_ARGS, 1_000_000L);
        return BlockchainRuntime.call(result, 1L, array, 1_000_000L);
    }

    public static Address spawnOnly(boolean shouldFail) {
        Address result = BlockchainRuntime.create(1L, CODE_AND_ARGS, 1_000_000L);
        if (shouldFail) {
            BlockchainRuntime.invalid();
        }
        return result;
    }
}

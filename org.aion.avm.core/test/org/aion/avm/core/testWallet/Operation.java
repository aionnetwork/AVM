package org.aion.avm.core.testWallet;

import avm.BlockchainRuntime;


/**
 * This is just a hash of some data used to identity a specific operation.
 */
public class Operation extends ByteArrayWrapper {
    public static Operation fromMessage() {
        byte[] data = BlockchainRuntime.getData();
        byte[] hash = BlockchainRuntime.blake2b(data);
        return new Operation(hash);
    }

    public static Operation fromRawBytes(byte[] bytes) {
        byte[] hash = BlockchainRuntime.blake2b(bytes);
        return new Operation(hash);
    }

    public static Operation fromHashedBytes(byte[] bytes) {
        return new Operation(bytes);
    }

    public static byte[] rawOperationForCurrentMessageAndBlock() {
        byte[] data = BlockchainRuntime.getData();
        long blockNumber = BlockchainRuntime.getBlockNumber();
        byte[] fullData = ByteArrayHelpers.appendLong(data, blockNumber);
        byte[] hash = BlockchainRuntime.blake2b(fullData);
        return hash;
    }

    private Operation(byte[] identifier) {
        super(identifier);
    }
}

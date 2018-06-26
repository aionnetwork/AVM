package org.aion.avm.core.testWallet;

import org.aion.avm.api.BlockchainRuntime;


/**
 * This is just a hash of some data used to identity a specific operation.
 */
public class Operation extends ByteArrayWrapper {
    public static Operation fromMessage() {
        byte[] data = BlockchainRuntime.getData();
        byte[] hash = BlockchainRuntime.sha3(data);
        return new Operation(hash);
    }

    public static Operation fromBytes(byte[] bytes) {
        return new Operation(bytes);
    }

    public static byte[] rawOperationForCurrentMessageAndBlock() {
        byte[] data = BlockchainRuntime.getData();
        long blockNumber = BlockchainRuntime.getBlockNumber();
        byte[] fullData = ByteArrayHelpers.appendLong(data, blockNumber);
        byte[] hash = BlockchainRuntime.sha3(fullData);
        return hash;
    }

    private Operation(byte[] identifier) {
        super(identifier);
    }
}

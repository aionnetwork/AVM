package org.aion.avm.core.testWallet;

import org.aion.avm.api.BlockchainRuntime;


/**
 * This is just a hash of some data used to identity a specific operation.
 */
public class Operation extends ByteArrayWrapper {
    public static Operation fromMessage(BlockchainRuntime context) {
        byte[] data = context.getMessageData();
        byte[] hash = context.sha3(data);
        return new Operation(hash);
    }

    public static Operation fromBytes(byte[] bytes) {
        return new Operation(bytes);
    }

    public static byte[] rawOperationForCurrentMessageAndBlock(BlockchainRuntime context) {
        byte[] data = context.getMessageData();
        long blockNumber = context.getBlockNumber();
        byte[] fullData = ByteArrayHelpers.appendLong(data, blockNumber);
        byte[] hash = context.sha3(fullData);
        return hash;
    }

    private Operation(byte[] identifier) {
        super(identifier);
    }
}

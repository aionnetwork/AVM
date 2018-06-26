package org.aion.avm.core.testWallet;

import org.aion.avm.api.IBlockchainRuntime;


/**
 * This is just a hash of some data used to identity a specific operation.
 */
public class Operation extends ByteArrayWrapper {
    public static Operation fromMessage(IBlockchainRuntime context) {
        byte[] data = context.getData();
        byte[] hash = context.sha3(data);
        return new Operation(hash);
    }

    public static Operation fromBytes(byte[] bytes) {
        return new Operation(bytes);
    }

    public static byte[] rawOperationForCurrentMessageAndBlock(IBlockchainRuntime context) {
        byte[] data = context.getData();
        long blockNumber = context.getBlockNumber();
        byte[] fullData = ByteArrayHelpers.appendLong(data, blockNumber);
        byte[] hash = context.sha3(fullData);
        return hash;
    }

    private Operation(byte[] identifier) {
        super(identifier);
    }
}

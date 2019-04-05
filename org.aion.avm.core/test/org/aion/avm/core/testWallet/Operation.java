package org.aion.avm.core.testWallet;

import avm.Blockchain;


/**
 * This is just a hash of some data used to identity a specific operation.
 */
public class Operation extends ByteArrayWrapper {
    public static Operation fromMessage() {
        byte[] data = Blockchain.getData();
        byte[] hash = Blockchain.blake2b(data);
        return new Operation(hash);
    }

    public static Operation fromRawBytes(byte[] bytes) {
        byte[] hash = Blockchain.blake2b(bytes);
        return new Operation(hash);
    }

    public static Operation fromHashedBytes(byte[] bytes) {
        return new Operation(bytes);
    }

    public static byte[] rawOperationForCurrentMessageAndBlock() {
        byte[] data = Blockchain.getData();
        long blockNumber = Blockchain.getBlockNumber();
        byte[] fullData = ByteArrayHelpers.appendLong(data, blockNumber);
        byte[] hash = Blockchain.blake2b(fullData);
        return hash;
    }

    private Operation(byte[] identifier) {
        super(identifier);
    }
}

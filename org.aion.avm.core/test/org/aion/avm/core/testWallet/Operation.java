package org.aion.avm.core.testWallet;


/**
 * This is just a hash of some data used to identity a specific operation.
 */
public class Operation extends ByteArrayWrapper {
    public static Operation from(IFutureRuntime context) {
        byte[] data = context.getMessageData();
        byte[] hash = context.sha3(data);
        return new Operation(hash);
    }

    private Operation(byte[] identifier) {
        super(identifier);
    }
}

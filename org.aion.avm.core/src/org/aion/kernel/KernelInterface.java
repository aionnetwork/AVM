package org.aion.kernel;


/**
 * Interface for accessing kernel features.
 */
public interface KernelInterface {
    void putTransformedCode(byte[] address, DappCode.CodeVersion version, byte[] code);

    byte[] getTransformedCode(byte[] address);

    void putStorage(byte[] address, byte[] key, byte[] value);

    byte[] getStorage(byte[] address, byte[] key);

    TransactionResult call(InternalTransaction internalTx, Block parentBlock);

    void updateCode(byte[] address, byte[] code);

    void selfdestruct(byte[] address, byte[] beneficiary);
}

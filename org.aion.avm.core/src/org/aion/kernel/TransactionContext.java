package org.aion.kernel;


/**
 * Interface for accessing kernel features.
 */
public interface TransactionContext {

    Transaction getTransaction();

    Block getBlock();

    void putTransformedCode(byte[] address, DappCode.CodeVersion version, byte[] code);

    byte[] getTransformedCode(byte[] address);

    void putStorage(byte[] address, byte[] key, byte[] value);

    byte[] getStorage(byte[] address, byte[] key);

    TransactionResult call(byte[] from, byte[] to, long value, byte[] data, long energyLimit);

    void updateCode(byte[] address, byte[] code);

    void selfdestruct(byte[] address, byte[] beneficiary);

    void log(byte[] address, byte[] index0, byte[] data);
}

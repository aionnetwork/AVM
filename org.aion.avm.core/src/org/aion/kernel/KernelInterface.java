package org.aion.kernel;

import org.aion.avm.core.Avm;


/**
 * Interface for accessing kernel features.
 */
public interface KernelInterface {

    /**
     * Sets the code of an account.
     *
     * @param address the account addres
     * @param code the immortal code
     */
    void putCode(byte[] address, VersionedCode code);

    /**
     * Retrieves the code of an account.
     *
     * @param address the account address
     * @return the code of the account, or NULL if not exists.
     */
    VersionedCode getCode(byte[] address);

    void putStorage(byte[] address, byte[] key, byte[] value);

    byte[] getStorage(byte[] address, byte[] key);

    void updateCode(byte[] address, byte[] code);

    void selfdestruct(byte[] address, byte[] beneficiary);
}

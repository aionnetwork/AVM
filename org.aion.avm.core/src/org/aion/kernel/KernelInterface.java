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

    /**
     * Put a key-value pair into the account's storage.
     *
     * @param address the account address
     * @param key the storage key
     * @param value the storage value
     */
    void putStorage(byte[] address, byte[] key, byte[] value);

    /**
     * Get the value that is mapped to the key, for the given account.
     *
     * @param address the account address
     * @param key the storage key
     */
    byte[] getStorage(byte[] address, byte[] key);


    /**
     * Deletes an account.
     *
     * @param address
     */
    void deleteAccount(byte[] address);
}

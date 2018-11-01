package org.aion.kernel;


import java.math.BigInteger;

/**
 * Interface for accessing kernel features.
 */
public interface KernelInterface {

    /**
     * Creates an account with the specified address.
     *
     * @param address The account to create.
     */
    void createAccount(byte[] address);

    /**
     * Returns true if, and only if, the specified address has account state. That is, it has a
     * positive nonce or balance or contains contract code.
     *
     * @param address The address whose existence is to be decided.
     * @return True if the account exists.
     */
    boolean hasAccountState(byte[] address);

    /**
     * Sets the code of an account.
     *
     * @param address the account address
     * @param code    the immortal code
     */
    void putCode(byte[] address, byte[] code);

    /**
     * Retrieves the code of an account.
     *
     * @param address the account address
     * @return the code of the account, or NULL if not exists.
     */
    byte[] getCode(byte[] address);

    /**
     * Put a key-value pair into the account's storage.
     *
     * @param address the account address
     * @param key     the storage key
     * @param value   the storage value
     */
    void putStorage(byte[] address, byte[] key, byte[] value);

    /**
     * Get the value that is mapped to the key, for the given account.
     *
     * @param address the account address
     * @param key     the storage key
     */
    byte[] getStorage(byte[] address, byte[] key);

    /**
     * Deletes an account.
     * This is used to implement the self-destruct functionality.
     *
     * @param address the account address
     */
    void deleteAccount(byte[] address);

    /**
     * Returns the balance of an account.
     *
     * @param address the account address
     * @return
     */
    BigInteger getBalance(byte[] address);

    /**
     * Adds/removes the balance of an account.
     *
     * @param address the account address
     * @param delta   the change
     */
    void adjustBalance(byte[] address, BigInteger delta);

    /**
     * Returns the nonce of an account.
     *
     * @param address the account address
     * @return the nonce
     */
    long getNonce(byte[] address);

    /**
     * Increases the nonce of an account by 1.
     *
     * @param address the account address
     */
    void incrementNonce(byte[] address);

}

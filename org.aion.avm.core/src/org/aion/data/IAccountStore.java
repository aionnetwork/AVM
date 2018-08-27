package org.aion.data;

import java.util.Map;

import org.aion.avm.core.util.ByteArrayWrapper;


/**
 * The abstract interface over a single account residing in an IDataStore instance.
 */
public interface IAccountStore {
    /**
     * @return The code stored for this account.
     */
    public byte[] getCode();

    /**
     * @param code The code to store for this account.
     */
    public void setCode(byte[] code);

    /**
     * @return The account balance.
     */
    public long getBalance();

    /**
     * @param balance The new account balance.
     */
    public void setBalance(long balance);

    /**
     * @return The account nonce.
     */
    public long getNonce();

    /**
     * @param nonce The new account nonce.
     */
    public void setNonce(long nonce);

    /**
     * Reads the application key-value store.
     * 
     * @param key The key to read.
     * @return The value for the key (null if the key is not found).
     */
    public byte[] getData(byte[] key);

    /**
     * Writes the application key-value store.
     * 
     * @param key The key to read.
     * @param value The value to store for the key.
     */
    public void setData(byte[] key, byte[] value);

    /**
     * Used only for testing and will be removed in the future.
     * 
     * @return A map of the entries in the account's application key-value store.
     */
    public Map<ByteArrayWrapper, byte[]> getStorageEntries();
}

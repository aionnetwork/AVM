package org.aion.avm.rt;

/**
 * Represents the hub of AVM runtime.
 */
public interface BlockchainRuntime {

    /**
     * Returns the sender address.
     *
     * @return
     */
    byte[] getSender();

    /**
     * Returns the address of the executing account.
     *
     * @return
     */
    byte[] getAddress();

    /**
     * Returns the energy limit.
     *
     * @return
     */
    long getEnergyLimit();

    /**
     * Returns the corresponding value in the storage.
     *
     * @param key
     * @return
     */
    byte[] getStorage(byte[] key);

    /**
     * Inserts/updates a key-value pair.
     *
     * @param key
     * @param value
     */
    void putStorage(byte[] key, byte[] value);
}

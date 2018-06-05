package org.aion.avm.rt;

import org.aion.avm.arraywrapper.ByteArray;

/**
 * Represents the hub of AVM runtime.
 */
public interface BlockchainRuntime {

    /**
     * Returns the sender address.
     *
     * @return
     */
    Address getSender();

    /**
     * Returns the address of the executing account.
     *
     * @return
     */
    Address getAddress();

    /**
     * Returns the energy limit.
     *
     * @return
     */
    long getEnergyLimit();

    /**
     * Returns the transaction data.
     * @return
     */
    ByteArray getData();

    /**
     * Returns the corresponding value in the storage.
     *
     * @param key
     * @return
     */
    ByteArray getStorage(ByteArray key);

    /**
     * Inserts/updates a key-value pair.
     *
     * @param key
     * @param value
     */
    void putStorage(ByteArray key, ByteArray value);
}

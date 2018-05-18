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
    ByteArray getSender();

    /**
     * Returns the address of the executing account.
     *
     * @return
     */
    ByteArray getAddress();

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
    ByteArray getStorage(ByteArray key);

    /**
     * Inserts/updates a key-value pair.
     *
     * @param key
     * @param value
     */
    void putStorage(ByteArray key, ByteArray value);
}

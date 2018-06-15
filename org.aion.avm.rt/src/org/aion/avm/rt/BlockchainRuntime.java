package org.aion.avm.rt;

import org.aion.avm.arraywrapper.ByteArray;

import java.io.File;

/**
 * Represents the hub of AVM runtime.
 */
public interface BlockchainRuntime {

    /**
     * Returns the sender address.
     *
     * @return
     */
    Address avm_getSender();

    /**
     * Returns the address of the executing account.
     *
     * @return
     */
    Address avm_getAddress();

    /**
     * Returns the energy limit.
     *
     * @return
     */
    long avm_getEnergyLimit();

    /**
     * Returns the transaction data.
     * @return
     */
    ByteArray avm_getData();

    /**
     * Returns the corresponding value in the storage.
     *
     * @param key
     * @return
     */
    ByteArray avm_getStorage(ByteArray key);

    /**
     * Inserts/updates a key-value pair.
     *
     * @param key
     * @param value
     */
    void avm_putStorage(ByteArray key, ByteArray value);

    void avm_storeTransformedDapp(File transformedJar);

    File avm_loadTransformedDapp(Address address);
}

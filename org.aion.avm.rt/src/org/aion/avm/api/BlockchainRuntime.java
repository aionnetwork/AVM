package org.aion.avm.api;

import org.aion.avm.arraywrapper.ByteArray;


/**
 * Represents the hub of AVM runtime.
 */
public interface BlockchainRuntime {
    // Runtime-facing implementation.
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

    /**
     * Update the Dapp code with a new version.
     *
     * @param newCode
     */
    void avm_updateCode(ByteArray newCode);

    /**
     * Destruct the Dapp.
     *
     * @param beneficiary
     */
    void avm_selfDestruct(Address beneficiary);

    /**
     * @return The time of the current block, as seconds since the Epoch.
     */
    long avm_getBlockEpochSeconds();

    /**
     * @return The number of the current block.
     */
    long avm_getBlockNumber();

    /**
     * Computes the sha3 digest of the given data.
     * Note that this response is always 32 bytes.  User-space might want to wrap it.  Should we wrap it on the runtime interface level?
     * 
     * @param data The data to hash.
     * @return The sha3 digest (always 32 bytes long).
     */
    ByteArray avm_sha3(ByteArray data);

    /**
     * Calls the contract denoted by the targetAddress, sending payload data and energyToSend energy for the invocation.  Returns the response of the contract.
     * NOTE:  This is likely to change as we work out the details of the ABI and cross-call semantics but exists to handle expectations of ported Solidity applications.
     * 
     * @param targetAddress The address of the contract to call.
     * @param value The value to transfer
     * @param data The data payload to send to that contract.
     * @param energyToSend The energy to send that contract.
     * @return The response of executing the contract.
     */
    ByteArray avm_call(Address targetAddress, ByteArray value, ByteArray data, long energyToSend);

    /**
     * Logs information for offline analysis or external listening.
     * 
     * @param index0 One identifying element for the event.
     * @param data Arbitrary unstructed data assocated with the event.
     */
    void avm_log(ByteArray index0, ByteArray data);


    // Compiler-facing implementation.
    default Address getSender() { return avm_getSender(); }

    default Address getAddress() { return avm_getAddress(); }

    default long getEnergyLimit() { return avm_getEnergyLimit(); }

    default byte[] getData() { return avm_getData().getUnderlying(); }

    default byte[] getStorage(byte[] key) { return avm_getStorage(new ByteArray(key)).getUnderlying(); }

    default void putStorage(byte[] key, byte[] value) { avm_putStorage(new ByteArray(key), new ByteArray(value)); }

    default void updateCode(byte[] newCode) { avm_updateCode(new ByteArray(newCode)); }

    default void selfDestruct(Address beneficiary) { avm_selfDestruct(beneficiary); }

    default long getBlockEpochSeconds() { return avm_getBlockEpochSeconds(); }

    default long getBlockNumber() { return avm_getBlockNumber(); }

    default byte[] sha3(byte[] data) { return avm_sha3(new ByteArray(data)).getUnderlying(); }

    default byte[] call(Address targetAddress, byte[] value, byte[] data, long energyLimit) { return avm_call(targetAddress, null, new ByteArray(data), energyLimit).getUnderlying(); }

    default void log(byte[] index0, byte[] data) { avm_log(new ByteArray(index0), (null != data) ? new ByteArray(data) : null); }
}

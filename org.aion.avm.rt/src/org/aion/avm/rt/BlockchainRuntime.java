package org.aion.avm.rt;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.java.lang.String;


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
     * @param codeVersion
     */
    void avm_updateCode(ByteArray newCode, String codeVersion);

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
     * @return The raw message data which started this invocation.
     */
    ByteArray avm_getMessageData();

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
     * @param energyToSend The energy to send that contract.
     * @param payload The data payload to send to that contract.
     * @return The response of executing the contract.
     */
    ByteArray avm_call(Address targetAddress, long energyToSend, ByteArray payload);


    // Compiler-facing implementation.
    default Address getSender() { return avm_getSender(); }

    default Address getAddress() { return avm_getAddress(); }

    default long getEnergyLimit() { return avm_getEnergyLimit(); }

    default byte[] getData() { return avm_getData().getUnderlying(); }

    default byte[] getStorage(byte[] key) { return avm_getStorage(new ByteArray(key)).getUnderlying(); }

    default void putStorage(byte[] key, byte[] value) { avm_putStorage(new ByteArray(key), new ByteArray(value)); }

    default void updateCode(byte[] newCode, java.lang.String codeVersion) { avm_updateCode(new ByteArray(newCode), new String(codeVersion)); }

    default void selfDestruct(Address beneficiary) { avm_selfDestruct(beneficiary); }

    default long getBlockEpochSeconds() { return avm_getBlockEpochSeconds(); }

    default byte[] getMessageData() { return avm_getMessageData().getUnderlying(); }

    default long getBlockNumber() { return avm_getBlockNumber(); }

    default byte[] sha3(byte[] data) { return avm_sha3(new ByteArray(data)).getUnderlying(); }

    default byte[] call(Address targetAddress, long energyToSend, byte[] payload) { return avm_call(targetAddress, energyToSend, new ByteArray(payload)).getUnderlying(); }
}

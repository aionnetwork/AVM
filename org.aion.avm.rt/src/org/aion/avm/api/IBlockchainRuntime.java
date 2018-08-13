package org.aion.avm.api;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.IObject;


/**
 * Represents the hub of AVM runtime.
 */
public interface IBlockchainRuntime extends IObject {
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
    ByteArray avm_call(Address targetAddress, long value, ByteArray data, long energyToSend);

    /**
     * Logs information for offline analysis or external listening.
     *
     * @param data Arbitrary unstructed data assocated with the event.
     */
    void avm_log(ByteArray data);

    void avm_log(ByteArray topic1, ByteArray data);

    void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data);

    void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data);

    void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data);
}

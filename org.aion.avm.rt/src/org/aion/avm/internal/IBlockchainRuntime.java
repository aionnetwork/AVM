package org.aion.avm.internal;

import org.aion.avm.api.Address;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.shadow.java.math.BigInteger;
import org.aion.avm.shadow.java.lang.String;


/**
 * Represents the hub of AVM runtime.
 */
public interface IBlockchainRuntime {
    //================
    // transaction
    //================

    /**
     * Returns the owner's address, whose state is being accessed.
     */
    Address avm_getAddress();

    /**
     * Returns the caller's address.
     */
    Address avm_getCaller();

    /**
     * Returns the originator's address.
     */
    Address avm_getOrigin();

    /**
     * Returns the energy limit.
     */
    long avm_getEnergyLimit();

    /**
     * Returns the energy price.
     */
    long avm_getEnergyPrice();

    /**
     * Returns the value being transferred along the transaction.
     */
    long avm_getValue();

    /**
     * Returns the transaction data.
     */
    ByteArray avm_getData();


    //================
    // block
    //================

    /**
     * Block timestamp.
     *
     * @return The time of the current block, as seconds since the Epoch.
     */
    long avm_getBlockTimestamp();

    /**
     * Block number.
     *
     * @return The number of the current block.
     */
    long avm_getBlockNumber();

    /**
     * Block energy limit
     *
     * @return The block energy limit
     */
    long avm_getBlockEnergyLimit();

    /**
     * Block coinbase address
     *
     * @return the miner address of the block
     */
    Address avm_getBlockCoinbase();

    /**
     * Block prevHash
     *
     * @return the hash of the previous block.
     */
    ByteArray avm_getBlockPreviousHash();

    /**
     * Block difficulty
     *
     * @return the difficulty of the block.
     */
    BigInteger avm_getBlockDifficulty();

    //================
    // State
    //================

    // TODO: how to expose the underlying storage, to cooperate with our persistence model

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
     * Returns the address of an account.
     *
     * @param address account address
     * @return the balance if the account
     */
    long avm_getBalance(Address address);

    /**
     * Returns the code size of an account.
     *
     * @param address account address
     * @return the code size of the account
     */
    int avm_getCodeSize(Address address);

    //================
    // System
    //================

    /**
     * Checks the current remaining energy.
     *
     * @return the remaining energy.
     */
    long avm_getRemainingEnergy();

    /**
     * Calls the contract denoted by the targetAddress, sending payload data and energyToSend energy for the invocation.  Returns the response of the contract.
     * NOTE:  This is likely to change as we work out the details of the ABI and cross-call semantics but exists to handle expectations of ported Solidity applications.
     *
     * @param targetAddress The address of the contract to call.
     * @param value         The value to transfer
     * @param data          The data payload to send to that contract.
     * @param energyToSend  The energy to send that contract.
     * @return The response of executing the contract.
     */
    ByteArray avm_call(Address targetAddress, long value, ByteArray data, long energyToSend);

    Address avm_create(long value, ByteArray data, long energyToSend);

    /**
     * Destructs this Dapp and refund all balance to the beneficiary.
     *
     * @param beneficiary
     */
    void avm_selfDestruct(Address beneficiary);

    /**
     * Logs information for offline analysis or external listening.
     *
     * @param data arbitrary unstructured data.
     */
    void avm_log(ByteArray data);

    void avm_log(ByteArray topic1, ByteArray data);

    void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data);

    void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data);

    void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data);

    /**
     * Computes the Blake2b digest of the given data.
     *
     * @param data The data to hash.
     * @return The 32-byte digest.
     */
    ByteArray avm_blake2b(ByteArray data);

    /**
     * Stop the current execution, rollback any state changes, and refund the remaining energy to caller.
     */
    void avm_revert();

    /**
     * Stop the current execution, rollback any state changes, and consume all remaining energy.
     */
    void avm_invalid();

    /**
     * Prints a message to console for debugging purpose
     *
     * @param message
     */
    void avm_print(String message);

    /**
     * Prints a message to console for debugging purpose
     *
     * @param message
     */
    void avm_println(String message);
}

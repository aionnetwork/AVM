package org.aion.avm.api;

import java.math.BigInteger;

/**
 * Every DApp has an associated <code>BlockchainRuntime</code> which allows
 * the application to interface with the environment teh app is running.
 * <p>
 * Typically, it includes the transaction and block context, and other blockchain
 * functionality.
 */
public final class BlockchainRuntime {

    private BlockchainRuntime() {
    }

    //===================
    // Transaction
    //===================

    /**
     * Returns the owner's address, whose state is being accessed.
     *
     * @return an address
     */
    public static Address getAddress() {
        return null;
    }

    /**
     * Returns the callers's address.
     *
     * @return an address
     */
    public static Address getCaller() {
        return null;
    }

    /**
     * Returns the originator's address.
     *
     * @return an address
     */
    public static Address getOrigin() {
        return null;
    }

    /**
     * Returns the energy limit for this current invocation.
     *
     * @return the max consumable energy
     */
    public static long getEnergyLimit() {
        return 0;
    }

    /**
     * Returns the energy price specified in the transaction.
     *
     * @return energy price.
     */
    public static long getEnergyPrice() {
        return 0;
    }

    /**
     * Returns the value being transferred to this dapp.
     *
     * @return the value in 10^-18 Aion
     */
    public static long getValue() {
        return 0;
    }

    /**
     * Returns the data passed to this dapp.
     *
     * @return an byte array, non-NULL.
     */
    public static byte[] getData() {
        return null;
    }

    //===================
    // Block
    //===================

    /**
     * Returns the block timestamp.
     *
     * @return a timestamp indicates when the block is forged.
     */
    public static long getBlockTimestamp() {
        return 0;
    }

    /**
     * Returns the block number.
     *
     * @return the number of the block, in which the transaction is included
     */
    public static long getBlockNumber() {
        return 0;
    }

    /**
     * Returns the block energy limit.
     *
     * @return the energy cap of the block.
     */
    public static long getBlockEnergyLimit() {
        return 0;
    }

    /**
     * Returns the block coinbase.
     *
     * @return the miner's address of the block.
     */
    public static Address getBlockCoinbase() {
        return null;
    }

    /**
     * Returns the hash of the previous block.
     *
     * @return a 32-byte array
     */
    public static byte[] getBlockPreviousHash() {
        return null;
    }

    /**
     * Returns the block difficulty.
     *
     * @return the PoW difficulty of the block.
     */
    public static BigInteger getBlockDifficulty() {
        return null;
    }

    //===================
    // Storage
    //===================

    /**
     * Returns the balance of an account.
     *
     * @param address the account address.
     * @return the account balance, or 0 if the account does not exist
     */
    public static long getBalance(Address address) {
        return 0;
    }

    /**
     * Returns the size of the code, of the given account.
     *
     * @param address the account address.
     * @return the code size, or 0 if the account does not exist
     */
    public static int getCodeSize(Address address) {
        return 0;
    }

    //===================
    // System
    //===================

    /**
     * Returns the remaining energy, at the moment this method is being called.
     *
     * @return the remaining energy
     */
    public static long getRemainingEnergy() {
        return 0;
    }

    /**
     * Calls another account, whether it's normal account or dapp.
     *
     * @param targetAddress the account address
     * @param value         the value to transfer
     * @param data          the value to pass
     * @param energyLimit   the max energy the invoked dapp can use.
     * @return the invocation result.
     * @throws IllegalArgumentException when the arguments are invalid, e.g. insufficient balance or null address.
     */
    public static Result call(Address targetAddress, long value, byte[] data, long energyLimit) throws IllegalArgumentException {
        return null;
    }

    /**
     * Creates an account.
     *
     * @param value       the value to transfer to the account to be created.
     * @param data        the data, in the format of <code>size_of_code + code + size_of_data + data</code>
     * @param energyLimit the max energy the invoked dapp can use.
     * @return the invocation result.
     * @throws IllegalArgumentException when the arguments are invalid, e.g. insufficient balance.
     */
    public static Result create(long value, byte[] data, long energyLimit) {
        return null;
    }

    /**
     * Destroys this dapp and refund all balance to the beneficiary address.
     *
     * @param beneficiary the beneficiary's address
     */
    public static void selfDestruct(Address beneficiary) {
    }

    /**
     * Records a log on blockchain.
     *
     * @param data any arbitrary data, non-NULL
     */
    public static void log(byte[] data) {
    }

    /**
     * Records a log on blockchain.
     *
     * @param topic1 the 1st topic
     * @param data   any arbitrary data, non-NULL
     */
    public static void log(byte[] topic1, byte[] data) {
    }

    /**
     * Records a log on blockchain.
     *
     * @param topic1 the 1st topic
     * @param topic2 the 2nd topic
     * @param data   any arbitrary data, non-NULL
     */
    public static void log(byte[] topic1, byte[] topic2, byte[] data) {
    }

    /**
     * Records a log on blockchain.
     *
     * @param topic1 the 1st topic
     * @param topic2 the 2nd topic
     * @param topic3 the 3rd topic
     * @param data   any arbitrary data, non-NULL
     */
    public static void log(byte[] topic1, byte[] topic2, byte[] topic3, byte[] data) {
    }

    /**
     * Records a log on blockchain.
     *
     * @param topic1 the 1st topic
     * @param topic2 the 2nd topic
     * @param topic3 the 3rd topic
     * @param topic4 the 4th topic
     * @param data   any arbitrary data, non-NULL
     */
    public static void log(byte[] topic1, byte[] topic2, byte[] topic3, byte[] topic4, byte[] data) {
    }

    /**
     * Calculates the blake2b digest of the input data.
     *
     * @param data the input data
     * @return the hash digest
     */
    public static byte[] blake2b(byte[] data) {
        return null;
    }

    /**
     * Stop the current execution and roll back all state changes.
     * <p>
     * the remaining energy will be refunded.
     */
    public static void revert() {
    }

    /**
     * Stop the current execution and roll back all state changes.
     * <p>
     * the remaining energy will be consumed.
     */
    public static void invalid() {
    }

    /**
     * Prints a message, for debugging purpose
     *
     * @param message the message to print
     */
    public static void print(String message) {

    }

    /**
     * Prints a message, for debugging purpose
     *
     * @param message the message to print
     */
    public static void println(String message) {

    }
}

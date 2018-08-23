package org.aion.avm.api;

import java.math.BigInteger;

/**
 * Represents the hub of AVM runtime.
 */
public final class BlockchainRuntime {

    private BlockchainRuntime() {
    }

    //===================
    // Transaction
    //===================

    public static Address getAddress() {
        return null;
    }

    public static Address getSender() {
        return null;
    }

    public static Address getOrigin() {
        return null;
    }

    public static long getEnergyLimit() {
        return 0;
    }

    public static long getEnergyPrice() {
        return 0;
    }

    public static long getValue() {
        return 0;
    }

    public static byte[] getData() {
        return null;
    }

    //===================
    // Block
    //===================

    public static long getBlockTimestamp() {
        return 0;
    }

    public static long getBlockNumber() {
        return 0;
    }

    public static long getBlockEnergyLimit() {
        return 0;
    }

    public static Address getBlockCoinbase() {
        return null;
    }

    public static byte[] getBlockPreviousHash() {
        return null;
    }

    public static BigInteger getBlockDifficulty() {
        return null;
    }

    //===================
    // Storage
    //===================

    public static byte[] getStorage(byte[] key) {
        return null;
    }

    public static void putStorage(byte[] key, byte[] value) {
    }

    public static long getBalance(Address address) {
        return 0;
    }

    public static int getCodeSize(Address address) {
        return 0;
    }

    //===================
    // System
    //===================

    public static long getRemainingEnergy() {
        return 0;
    }

    public static byte[] call(Address targetAddress, long value, byte[] data, long energyToSend) {
        return null;
    }

    public static Address create(long value, byte[] data, long energyToSend) {
        return null;
    }

    public static void selfDestruct(Address beneficiary) {
    }

    public static void log(byte[] data) {
    }

    public static void log(byte[] topic1, byte[] data) {
    }

    public static void log(byte[] topic1, byte[] topic2, byte[] data) {
    }

    public static void log(byte[] topic1, byte[] topic2, byte[] topic3, byte[] data) {
    }

    public static void log(byte[] topic1, byte[] topic2, byte[] topic3, byte[] topic4, byte[] data) {
    }

    public static byte[] blake2b(byte[] data) {
        return null;
    }

    public static void revert() {
    }

    public static void invalid() {
    }

    public static void print(String message) {

    }

    public static void println(String message) {

    }
}

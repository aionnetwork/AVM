package org.aion.avm.api;

/**
 * Represents the hub of AVM runtime.
 */
public class BlockchainRuntime {

    // TODO: clean up the method names, or re-organize by types

    //===================
    // Transaction
    //===================

    public static Address getSender() {
        return null;
    }

    public static Address getAddress() {
        return null;
    }

    public static byte[] getData() {
        return null;
    }

    public static long getEnergyLimit() {
        return 0;
    }

    //===================
    // Block
    //===================

    public static long getBlockEpochSeconds() {
        return 0;
    }

    public static long getBlockNumber() {
        return 0;
    }

    //===================
    // Storage
    //===================

    public static byte[] getStorage(byte[] key) {
        return null;
    }

    public static void putStorage(byte[] key, byte[] value) {
    }

    //===================
    // Misc
    //===================

    public static void updateCode(byte[] newCode) {
    }

    public static void selfDestruct(Address beneficiary) {
    }

    public static byte[] sha3(byte[] data) {
        return null;
    }

    public static byte[] call(Address to, byte[] value, byte[] data, long energyLimit) {
        return null;
    }

    public static void log(byte[] index0, byte[] data) {
    }
}

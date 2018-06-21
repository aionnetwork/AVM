package org.aion.avm.api;

/**
 * Represents the hub of AVM runtime.
 */
public class BlockchainRuntime {

    private static Block block = null;
    private static Transaction tx = null;

    private BlockchainRuntime() {
    }

    public static Block block() {
        return block;
    }

    public static Transaction transaction() {
        return tx;
    }

    public static byte[] getStorage(byte[] key) {
        return null;
    }

    public static void putStorage(byte[] key, byte[] value) {
    }

    public static byte[] sha3(byte[] data) {
        return null;
    }

    public static void call(Address targetAddress, byte[] value, byte[] data, long energyLimit) {
    }

    public static void updateCode(byte[] newCode) {
    }

    public static void selfDestruct(Address beneficiary) {
    }
}

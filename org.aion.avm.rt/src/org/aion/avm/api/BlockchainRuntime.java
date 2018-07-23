package org.aion.avm.api;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.Helper;

public class BlockchainRuntime {

    // Runtime-facing implementation.

    public static Address avm_getSender() {
        return Helper.blockchainRuntime.avm_getSender();
    }

    public static Address avm_getAddress() {
        return Helper.blockchainRuntime.avm_getAddress();
    }

    public static long avm_getEnergyLimit() {
        return Helper.blockchainRuntime.avm_getEnergyLimit();
    }

    public static ByteArray avm_getData() {
        return Helper.blockchainRuntime.avm_getData();
    }

    public static ByteArray avm_getStorage(ByteArray key) {
        return Helper.blockchainRuntime.avm_getStorage(key);
    }

    public static void avm_putStorage(ByteArray key, ByteArray value) {
        Helper.blockchainRuntime.avm_putStorage(key, value);
    }

    public static void avm_updateCode(ByteArray newCode) {
        Helper.blockchainRuntime.avm_updateCode(newCode);
    }

    public static void avm_selfDestruct(Address beneficiary) {
        Helper.blockchainRuntime.avm_selfDestruct(beneficiary);
    }

    public static long avm_getBlockEpochSeconds() {
        return Helper.blockchainRuntime.avm_getBlockEpochSeconds();
    }

    public static long avm_getBlockNumber() {
        return Helper.blockchainRuntime.avm_getBlockNumber();
    }

    public static ByteArray avm_sha3(ByteArray data) {
        return Helper.blockchainRuntime.avm_sha3(data);
    }

    public static ByteArray avm_call(Address targetAddress, long value, ByteArray data, long energyLimit) {
        return Helper.blockchainRuntime.avm_call(targetAddress, value, data, energyLimit);
    }

    public static void avm_log(ByteArray data) {
        Helper.blockchainRuntime.avm_log(data);
    }
    public static void avm_log(ByteArray topic1, ByteArray data) {
        Helper.blockchainRuntime.avm_log(topic1, data);
    }
    public static void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {
        Helper.blockchainRuntime.avm_log(topic1, topic2, data);
    }
    public static void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {
        Helper.blockchainRuntime.avm_log(topic1, topic2, topic3, data);
    }
    public static void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {
        Helper.blockchainRuntime.avm_log(topic1, topic2, topic3, topic4, data);
    }

    // Compiler-facing implementation.

    public static Address getSender() {
        return avm_getSender();
    }

    public static Address getAddress() {
        return avm_getAddress();
    }

    public static long getEnergyLimit() {
        return avm_getEnergyLimit();
    }

    public static byte[] getData() {
        return avm_getData().getUnderlying();
    }

    public static byte[] getStorage(byte[] key) {
        return avm_getStorage(new ByteArray(key)).getUnderlying();
    }

    public static void putStorage(byte[] key, byte[] value) {
        avm_putStorage(new ByteArray(key), new ByteArray(value));
    }

    public static void updateCode(byte[] newCode) {
        avm_updateCode(new ByteArray(newCode));
    }

    public static void selfDestruct(Address beneficiary) {
        avm_selfDestruct(beneficiary);
    }

    public static long getBlockEpochSeconds() {
        return avm_getBlockEpochSeconds();
    }

    public static long getBlockNumber() {
        return avm_getBlockNumber();
    }

    public static byte[] sha3(byte[] data) {
        return avm_sha3(new ByteArray(data)).getUnderlying();
    }

    public static byte[] call(Address targetAddress, long value, byte[] data, long energyLimit) {
        return avm_call(targetAddress, value, new ByteArray(data), energyLimit).getUnderlying();
    }

    public static void log(byte[] data) {
        avm_log(new ByteArray(data));
    }

    public static void log(byte[] topic1, byte[] data) {
        avm_log(new ByteArray(topic1), new ByteArray(data));
    }

    public static void log(byte[] topic1, byte[] topic2, byte[] data) {
        avm_log(new ByteArray(topic1), new ByteArray(topic2), new ByteArray(data));
    }

    public static void log(byte[] topic1, byte[] topic2, byte[] topic3, byte[] data) {
        avm_log(new ByteArray(topic1), new ByteArray(topic2), new ByteArray(topic3), new ByteArray(data));
    }

    public static void log(byte[] topic1, byte[] topic2, byte[] topic3, byte[] topic4, byte[] data) {
        avm_log(new ByteArray(topic1), new ByteArray(topic2), new ByteArray(topic3), new ByteArray(topic4), new ByteArray(data));
    }
}

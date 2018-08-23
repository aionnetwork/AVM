package org.aion.avm.api;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.Helper;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.math.BigInteger;


public final class BlockchainRuntime {

    private BlockchainRuntime() {
    }

    // Runtime-facing implementation.

    public static Address avm_getAddress() {
        return Helper.blockchainRuntime.avm_getAddress();
    }

    public static Address avm_getCaller() {
        return Helper.blockchainRuntime.avm_getCaller();
    }

    public static Address avm_getOrigin() {
        return Helper.blockchainRuntime.avm_getOrigin();
    }

    public static long avm_getEnergyLimit() {
        return Helper.blockchainRuntime.avm_getEnergyLimit();
    }

    public static long avm_getEnergyPrice() {
        return Helper.blockchainRuntime.avm_getEnergyPrice();
    }

    public static long avm_getValue() {
        return Helper.blockchainRuntime.avm_getValue();
    }

    public static ByteArray avm_getData() {
        return Helper.blockchainRuntime.avm_getData();
    }


    public static long avm_getBlockTimestamp() {
        return Helper.blockchainRuntime.avm_getBlockTimestamp();
    }

    public static long avm_getBlockNumber() {
        return Helper.blockchainRuntime.avm_getBlockNumber();
    }

    public static long avm_getBlockEnergyLimit() {
        return Helper.blockchainRuntime.avm_getBlockEnergyLimit();
    }

    public static Address avm_getBlockCoinbase() {
        return Helper.blockchainRuntime.avm_getBlockCoinbase();
    }

    public static ByteArray avm_getBlockPreviousHash() {
        return Helper.blockchainRuntime.avm_getBlockPreviousHash();
    }

    public static BigInteger avm_getBlockDifficulty() {
        return Helper.blockchainRuntime.avm_getBlockDifficulty();
    }


    public static ByteArray avm_getStorage(ByteArray key) {
        return Helper.blockchainRuntime.avm_getStorage(key);
    }

    public static void avm_putStorage(ByteArray key, ByteArray value) {
        Helper.blockchainRuntime.avm_putStorage(key, value);
    }

    public static long avm_getBalance(Address address) {
        return Helper.blockchainRuntime.avm_getBalance(address);
    }

    public static int avm_getCodeSize(Address address) {
        return Helper.blockchainRuntime.avm_getCodeSize(address);
    }


    public static long avm_getRemainingEnergy() {
        return Helper.blockchainRuntime.avm_getRemainingEnergy();
    }

    public static ByteArray avm_call(Address targetAddress, long value, ByteArray data, long energyLimit) {
        return Helper.blockchainRuntime.avm_call(targetAddress, value, data, energyLimit);
    }

    public static Address avm_create(long value, ByteArray data, long energyLimit) {
        return Helper.blockchainRuntime.avm_create(value, data, energyLimit);
    }

    public static void avm_selfDestruct(Address beneficiary) {
        Helper.blockchainRuntime.avm_selfDestruct(beneficiary);
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

    public static ByteArray avm_blake2b(ByteArray data) {
        return Helper.blockchainRuntime.avm_blake2b(data);
    }

    public static void avm_revert() {
        Helper.blockchainRuntime.avm_revert();
    }

    public static void avm_invalid() {
        Helper.blockchainRuntime.avm_invalid();
    }

    public static void avm_print(String message) {
        Helper.blockchainRuntime.avm_print(message);
    }

    public static void avm_println(String message) {
        Helper.blockchainRuntime.avm_println(message);
    }

    // Compiler-facing implementation.

    public static Address getAddress() {
        return avm_getAddress();
    }

    public static Address getCaller() {
        return avm_getCaller();
    }

    public static Address getOrigin() {
        return avm_getOrigin();
    }

    public static long getEnergyLimit() {
        return avm_getEnergyLimit();
    }

    public static long getEnergyPrice() {
        return avm_getEnergyPrice();
    }

    public static long getValue() {
        return avm_getValue();
    }

    public static byte[] getData() {
        return avm_getData().getUnderlying();
    }

    public static long getBlockTimestamp() {
        return avm_getBlockTimestamp();
    }

    public static long getBlockNumber() {
        return avm_getBlockNumber();
    }

    public static long getBlockEnergyLimit() {
        return avm_getBlockEnergyLimit();
    }

    public static Address getBlockCoinbase() {
        return avm_getBlockCoinbase();
    }

    public static byte[] getBlockPreviousHash() {
        return avm_getBlockPreviousHash().getUnderlying();
    }

    public static java.math.BigInteger getBlockDifficulty() {
        return avm_getBlockDifficulty().getUnderlying();
    }


    public static byte[] getStorage(byte[] key) {
        return avm_getStorage(new ByteArray(key)).getUnderlying();
    }

    public static void putStorage(byte[] key, byte[] value) {
        avm_putStorage(new ByteArray(key), new ByteArray(value));
    }

    public static long getBalance(Address address) {
        return avm_getBalance(address);
    }

    public static int getCodeSize(Address address) {
        return avm_getCodeSize(address);
    }


    public static long getRemainingEnergy() {
        return avm_getRemainingEnergy();
    }

    public static byte[] call(Address targetAddress, long value, byte[] data, long energyLimit) {
        return avm_call(targetAddress, value, new ByteArray(data), energyLimit).getUnderlying();
    }

    public static Address create(long value, byte[] data, long energyToSend) {
        return avm_create(value, new ByteArray(data), energyToSend);
    }

    public static void selfDestruct(Address beneficiary) {
        avm_selfDestruct(beneficiary);
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

    public static byte[] blake2b(byte[] data) {
        return avm_blake2b(new ByteArray(data)).getUnderlying();
    }

    public static void revert() {
        avm_revert();
    }

    public static void invalid() {
        avm_invalid();
    }

    public static void print(java.lang.String message) {
        avm_print(new String(message));
    }

    public static void println(java.lang.String message) {
        avm_println(new String(message));
    }
}

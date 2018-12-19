package org.aion.avm.api;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.internal.IBlockchainRuntime;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.math.BigInteger;

import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.RuntimeMethodFeeSchedule;


public final class BlockchainRuntime {
    public static IBlockchainRuntime blockchainRuntime;

    private BlockchainRuntime() {
    }

    // Runtime-facing implementation.

    public static Address avm_getAddress() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getAddress);
        return blockchainRuntime.avm_getAddress();
    }

    public static Address avm_getCaller() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getCaller);
        return blockchainRuntime.avm_getCaller();
    }

    public static Address avm_getOrigin() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getOrigin);
        return blockchainRuntime.avm_getOrigin();
    }

    public static long avm_getEnergyLimit() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getEnergyLimit);
        return blockchainRuntime.avm_getEnergyLimit();
    }

    public static long avm_getEnergyPrice() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getEnergyPrice);
        return blockchainRuntime.avm_getEnergyPrice();
    }

    public static BigInteger avm_getValue() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getValue);
        return blockchainRuntime.avm_getValue();
    }

    public static ByteArray avm_getData() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getData);
        return blockchainRuntime.avm_getData();
    }


    public static long avm_getBlockTimestamp() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getBlockTimestamp);
        return blockchainRuntime.avm_getBlockTimestamp();
    }

    public static long avm_getBlockNumber() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getBlockNumber);
        return blockchainRuntime.avm_getBlockNumber();
    }

    public static long avm_getBlockEnergyLimit() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getBlockEnergyLimit);
        return blockchainRuntime.avm_getBlockEnergyLimit();
    }

    public static Address avm_getBlockCoinbase() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getBlockCoinbase);
        return blockchainRuntime.avm_getBlockCoinbase();
    }

    public static BigInteger avm_getBlockDifficulty() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getBlockDifficulty);
        return blockchainRuntime.avm_getBlockDifficulty();
    }


    public static BigInteger avm_getBalance(Address address) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getBalance);
        return blockchainRuntime.avm_getBalance(address);
    }

    public static int avm_getCodeSize(Address address) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getCodeSize);
        return blockchainRuntime.avm_getCodeSize(address);
    }


    public static long avm_getRemainingEnergy() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_getRemainingEnergy);
        return blockchainRuntime.avm_getRemainingEnergy();
    }

    public static Result avm_call(Address targetAddress, BigInteger value, ByteArray data, long energyLimit) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_call);
        return blockchainRuntime.avm_call(targetAddress, value, data, energyLimit);
    }

    public static Result avm_create(BigInteger value, ByteArray data, long energyLimit) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_create);
        return blockchainRuntime.avm_create(value, data, energyLimit);
    }

    public static void avm_selfDestruct(Address beneficiary) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_selfDestruct);
        blockchainRuntime.avm_selfDestruct(beneficiary);
    }

    public static void avm_log(ByteArray data) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_log);
        blockchainRuntime.avm_log(data);
    }

    public static void avm_log(ByteArray topic1, ByteArray data) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_log_1);
        blockchainRuntime.avm_log(topic1, data);
    }

    public static void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_log_2);
        blockchainRuntime.avm_log(topic1, topic2, data);
    }

    public static void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_log_3);
        blockchainRuntime.avm_log(topic1, topic2, topic3, data);
    }

    public static void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_log_4);
        blockchainRuntime.avm_log(topic1, topic2, topic3, topic4, data);
    }

    public static ByteArray avm_blake2b(ByteArray data) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_blake2b);
        return blockchainRuntime.avm_blake2b(data);
    }

    public static ByteArray avm_sha256(ByteArray data) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_sha256);
        return blockchainRuntime.avm_sha256(data);
    }

    public static ByteArray avm_keccak256(ByteArray data){
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_keccak256);
        return blockchainRuntime.avm_keccak256(data);
    }

    public static void avm_revert() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_revert);
        blockchainRuntime.avm_revert();
    }

    public static void avm_invalid() {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_invalid);
        blockchainRuntime.avm_invalid();
    }

    public static void avm_print(String message) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_print);
        blockchainRuntime.avm_print(message);
    }

    public static void avm_println(String message) {
        IInstrumentation.attachedThreadInstrumentation.get().chargeEnergy(RuntimeMethodFeeSchedule.BlockchainRuntime_avm_println);
        blockchainRuntime.avm_println(message);
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

    public static java.math.BigInteger getValue() {
        return avm_getValue().getUnderlying();
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

    public static java.math.BigInteger getBlockDifficulty() {
        return avm_getBlockDifficulty().getUnderlying();
    }


    public static java.math.BigInteger getBalance(Address address) {
        return avm_getBalance(address).getUnderlying();
    }

    public static int getCodeSize(Address address) {
        return avm_getCodeSize(address);
    }


    public static long getRemainingEnergy() {
        return avm_getRemainingEnergy();
    }

    public static Result call(Address targetAddress, java.math.BigInteger value, byte[] data, long energyLimit) {
        return avm_call(targetAddress, new BigInteger(value), new ByteArray(data), energyLimit);
    }

    public static Result create(java.math.BigInteger value, byte[] data, long energyLimit) {
        return avm_create(new BigInteger(value), new ByteArray(data), energyLimit);
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

    public static byte[] sha256(byte[] data) {
        return avm_sha256(new ByteArray(data)).getUnderlying();
    }

    public static byte[] keccak256(byte[] data) {
        return avm_keccak256(new ByteArray(data)).getUnderlying();
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

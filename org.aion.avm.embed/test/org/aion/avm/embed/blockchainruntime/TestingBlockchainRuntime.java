package org.aion.avm.embed.blockchainruntime;

import org.aion.avm.core.IExternalState;
import org.aion.types.AionAddress;
import p.avm.Address;
import p.avm.Result;
import i.IBlockchainRuntime;
import a.ByteArray;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.crypto.CryptoUtil;
import org.aion.avm.embed.hash.HashUtils;

import i.InvalidException;
import i.RevertException;
import s.java.lang.String;
import s.java.math.BigInteger;
import org.aion.kernel.TestingState;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * A minimal implementation of IBlockchainRuntime sufficient for our current class of tests.
 * These provide only the direct inputs, none of the interactive data layer.
 */
public class TestingBlockchainRuntime implements IBlockchainRuntime {

    private byte[] transactionHash = Helpers.randomBytes(32);
    private AionAddress address = Helpers.address(1);
    private AionAddress caller = Helpers.address(2);
    private AionAddress origin = Helpers.address(2);
    private BigInteger value = BigInteger.avm_ZERO;
    private byte[] data = new byte[0];
    private long energyLimit = 1_000_000L;
    private long energyPrice = 1L;

    private long blockNumber = 1;
    private long blockTimstamp = System.currentTimeMillis();
    private AionAddress blockCoinbase = Helpers.address(3);
    private long blockEnergyLimit = 10_000_000L;
    private java.math.BigInteger blockDifficulty = java.math.BigInteger.valueOf(1000L);


    private IExternalState kernel = new TestingState();
    private Map<java.lang.String, Integer> eventCounter = new HashMap<>();

    public TestingBlockchainRuntime() {
    }

    public TestingBlockchainRuntime withAddress(byte[] address) {
        this.address = new AionAddress(address);
        return this;
    }

    public TestingBlockchainRuntime withCaller(byte[] caller) {
        this.caller = new AionAddress(caller);
        return this;
    }

    public TestingBlockchainRuntime withData(byte[] data) {
        this.data = data;
        return this;
    }

    public TestingBlockchainRuntime withEnergyLimit(long energyLimit) {
        this.energyLimit = energyLimit;
        return this;
    }

    public TestingBlockchainRuntime withEventCounter(Map<java.lang.String, Integer> eventCounter) {
        this.eventCounter = eventCounter;
        return this;
    }

    public TestingBlockchainRuntime withKernel(IExternalState kernel) {
        this.kernel = kernel;
        return this;
    }

    public TestingBlockchainRuntime withTransactionHash(byte[] hash) {
        this.transactionHash = hash.clone();
        return this;
    }

    @Override
    public ByteArray avm_getTransactionHash() {
        return new ByteArray(this.transactionHash.clone());
    }

    @Override
    public Address avm_getAddress() {
        return new Address(address.toByteArray());
    }

    @Override
    public Address avm_getCaller() {
        return new Address(caller.toByteArray());
    }

    @Override
    public Address avm_getOrigin() {
        return new Address(origin.toByteArray());
    }

    @Override
    public long avm_getEnergyLimit() {
        return energyLimit;
    }

    @Override
    public long avm_getEnergyPrice() {
        return energyPrice;
    }

    @Override
    public BigInteger avm_getValue() {
        return value;
    }

    @Override
    public ByteArray avm_getData() {
        return new ByteArray(data);
    }

    @Override
    public void avm_selfDestruct(Address beneficiary) {
        throw new AssertionError("avm_selfDestruct");
    }

    @Override
    public long avm_getBlockTimestamp() {
        return blockTimstamp;
    }

    @Override
    public long avm_getBlockNumber() {
        return blockNumber;
    }

    @Override
    public long avm_getBlockEnergyLimit() {
        return blockEnergyLimit;
    }

    @Override
    public Address avm_getBlockCoinbase() {
        return new Address(blockCoinbase.toByteArray());
    }

    @Override
    public void avm_putStorage(ByteArray key, ByteArray value, boolean requiresRefund) {
        Objects.requireNonNull(address);
        if (value == null) {
            kernel.removeStorage(address, key.getUnderlying());
        } else {
            kernel.putStorage(address, key.getUnderlying(), value.getUnderlying());
        }
    }

    @Override
    public ByteArray avm_getStorage(ByteArray key) {
        Objects.requireNonNull(key);
        byte[] data = this.kernel.getStorage(address, key.getUnderlying());
        return (null != data)
            ? new ByteArray(data)
            : null;
    }

    @Override
    public BigInteger avm_getBalance(Address address) {
        Objects.requireNonNull(address);
        return new BigInteger(kernel.getBalance(new AionAddress(address.toByteArray())));
    }

    @Override
    public BigInteger avm_getBalanceOfThisContract() {
        return new BigInteger(kernel.getBalance(address));
    }

    @Override
    public int avm_getCodeSize(Address address) {
        Objects.requireNonNull(address);
        byte[] vc = kernel.getTransformedCode(new AionAddress(address.toByteArray()));
        return vc == null ? 0 : vc.length;
    }

    @Override
    public BigInteger avm_getBlockDifficulty() {
        return new BigInteger(blockDifficulty);
    }

    @Override
    public long avm_getRemainingEnergy() {
        throw new AssertionError("avm_getRemainingEnergy");
    }

    @Override
    public Result avm_call(Address targetAddress, BigInteger value, ByteArray payload, long energyLimit) {
        // We will just bounce back the input, so that the caller can see "something".
        return new Result(true, payload);
    }

    @Override
    public Result avm_create(BigInteger value, ByteArray data, long energyLimit) {
        return new Result(true, null);
    }

    @Override
    public void avm_log(ByteArray data) {
    }

    @Override
    public void avm_log(ByteArray index0, ByteArray data) {
        java.lang.String reconstituted = new java.lang.String(index0.getUnderlying(), StandardCharsets.UTF_8);
        System.out.println(reconstituted);
        Integer oldCount = this.eventCounter.get(reconstituted);
        int newCount = ((null != oldCount) ? oldCount : 0) + 1;
        this.eventCounter.put(reconstituted, newCount);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {
    }

    @Override
    public ByteArray avm_blake2b(ByteArray data) {
        Objects.requireNonNull(data);
        return new ByteArray(HashUtils.blake2b(data.getUnderlying()));
    }

    @Override
    public ByteArray avm_sha256(ByteArray data) {
        Objects.requireNonNull(data);
        return new ByteArray(HashUtils.sha256(data.getUnderlying()));
    }

    @Override
    public ByteArray avm_keccak256(ByteArray data) {
        Objects.requireNonNull(data);
        return new ByteArray(HashUtils.keccak256(data.getUnderlying()));
    }

    @Override
    public void avm_revert() {
        throw new RevertException();
    }

    @Override
    public void avm_invalid() {
        throw new InvalidException();
    }

    @Override
    public void avm_require(boolean condition) {
        if (!condition) {
            throw new RevertException();
        }
    }

    @Override
    public void avm_print(String message) {
        System.out.print(message);
    }

    @Override
    public void avm_println(String message) {
        System.out.println(message);
    }

    @Override
    public boolean avm_edVerify(ByteArray data, ByteArray signature, ByteArray publicKey) throws IllegalArgumentException {
        Objects.requireNonNull(data);
        Objects.requireNonNull(signature);
        Objects.requireNonNull(publicKey);

        return CryptoUtil.verifyEdDSA(data.getUnderlying(), signature.getUnderlying(), publicKey.getUnderlying());
    }

    public int getEventCount(java.lang.String event) {
        Integer count = this.eventCounter.get(event);
        return (null != count) ? count : 0;
    }
}

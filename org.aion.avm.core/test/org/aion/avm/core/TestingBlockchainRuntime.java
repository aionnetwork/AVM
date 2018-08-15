package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.internal.IBlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.util.Assert;
import org.aion.avm.core.util.HashUtils;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.InvalidException;
import org.aion.avm.internal.RevertException;
import org.aion.avm.shadow.java.math.BigInteger;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.VersionedCode;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * A minimal implementation of IBlockchainRuntime sufficient for our current class of tests.
 * These provide only the direct inputs, none of the interactive data layer.
 */
public class TestingBlockchainRuntime implements IBlockchainRuntime {

    private byte[] address = Helpers.address(1);
    private byte[] caller = Helpers.address(2);
    private byte[] origin = Helpers.address(2);
    private long value = 0;
    private byte[] data = new byte[0];
    private long energyLimit = 1_000_000L;
    private long energyPrice = 1L;

    private long blockNumber = 1;
    private long blockTimstamp = System.currentTimeMillis();
    private byte[] blockCoinbase = Helpers.address(3);
    private byte[] blockPrevHash = new byte[32];
    private long blockEnergyLimit = 10_000_000L;
    private java.math.BigInteger blockDifficulty = java.math.BigInteger.valueOf(1000L);


    private KernelInterface kernel = new KernelInterfaceImpl();
    private Map<String, Integer> eventCounter = new HashMap<>();

    public TestingBlockchainRuntime() {
    }

    public TestingBlockchainRuntime(TransactionContext ctx) {
        this.address = ctx.getAddress();
        this.caller = ctx.getCaller();
        this.origin = ctx.getOrigin();
        this.value = ctx.getValue();
        this.data = ctx.getData();
        this.energyLimit = ctx.getEnergyLimit();
        this.energyPrice = ctx.getEneryPrice();
        this.blockNumber = ctx.getBlockNumber();
        this.blockTimstamp = ctx.getBlockTimestamp();
        this.blockDifficulty = ctx.getBlockDifficulty();
    }

    public TestingBlockchainRuntime withAddress(byte[] address) {
        this.address = address;
        return this;
    }

    public TestingBlockchainRuntime withCaller(byte[] caller) {
        this.caller = caller;
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

    public TestingBlockchainRuntime withEventCounter(Map<String, Integer> eventCounter) {
        this.eventCounter = eventCounter;
        return this;
    }

    public TestingBlockchainRuntime withKernel(KernelInterface kernel) {
        this.kernel = kernel;
        return this;
    }

    @Override
    public Address avm_getAddress() {
        return new Address(address);
    }

    @Override
    public Address avm_getCaller() {
        return new Address(caller);
    }

    @Override
    public Address avm_getOrigin() {
        return new Address(origin);
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
    public long avm_getValue() {
        return value;
    }

    @Override
    public ByteArray avm_getData() {
        return new ByteArray(data);
    }

    @Override
    public void avm_selfDestruct(Address beneficiary) {
        Assert.unimplemented("avm_selfDestruct");
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
        return new Address(blockCoinbase);
    }

    @Override
    public ByteArray avm_getBlockPreviousHash() {
        return new ByteArray(blockPrevHash);
    }


    @Override
    public ByteArray avm_getStorage(ByteArray key) {
        Objects.requireNonNull(key);
        byte[] value = kernel.getStorage(address, key.getUnderlying());
        return value == null ? null : new ByteArray(value);
    }

    @Override
    public void avm_putStorage(ByteArray key, ByteArray value) {
        Objects.requireNonNull(key);
        kernel.putStorage(address, key.getUnderlying(), value == null ? null : value.getUnderlying());
    }

    @Override
    public long avm_getBalance(Address address) {
        Objects.requireNonNull(address);
        return kernel.getBalance(address.unwrap());
    }

    @Override
    public int avm_getCodeSize(Address address) {
        Objects.requireNonNull(address);
        VersionedCode vc = kernel.getCode(address.unwrap());
        return vc == null ? 0 : vc.getCode().length;
    }

    @Override
    public BigInteger avm_getBlockDifficulty() {
        return new BigInteger(blockDifficulty);
    }

    @Override
    public long avm_getRemainingEnergy() {
        Assert.unimplemented("avm_getRemainingEnergy");
        return 0;
    }

    @Override
    public ByteArray avm_call(Address targetAddress, long value, ByteArray payload, long energyToSend) {
        return payload;
    }

    @Override
    public Address avm_create(long value, ByteArray data, long energyToSend) {
        Assert.unimplemented("avm_create");
        return null;
    }

    @Override
    public void avm_log(ByteArray data) {
    }

    @Override
    public void avm_log(ByteArray index0, ByteArray data) {
        String reconstituted = new String(index0.getUnderlying(), StandardCharsets.UTF_8);
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
    public void avm_revert() {
        throw new RevertException();
    }

    @Override
    public void avm_invalid() {
        throw new InvalidException();
    }

    public int getEventCount(String event) {
        Integer count = this.eventCounter.get(event);
        return (null != count) ? count : 0;
    }
}
package org.aion.avm.core.blockchainruntime;

import org.aion.avm.shadowapi.avm.Address;
import org.aion.avm.shadowapi.avm.Result;
import org.aion.avm.internal.IBlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.InvalidException;
import org.aion.avm.internal.RevertException;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.math.BigInteger;
import org.aion.kernel.Block;
import org.aion.kernel.TestingKernel;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.aion.kernel.Transaction.Type;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionContext;


/**
 * A minimal implementation of IBlockchainRuntime sufficient for our current class of tests.
 * These provide only the direct inputs, none of the interactive data layer.
 */
public class TestingBlockchainRuntime implements IBlockchainRuntime {
    private final IExternalCapabilities capabilities;
    private org.aion.types.Address address = Helpers.address(1);
    private org.aion.types.Address caller = Helpers.address(2);
    private org.aion.types.Address origin = Helpers.address(2);
    private BigInteger value = BigInteger.avm_ZERO;
    private byte[] data = new byte[0];
    private long energyLimit = 1_000_000L;
    private long energyPrice = 1L;

    private long blockNumber = 1;
    private long blockTimstamp = System.currentTimeMillis();
    private org.aion.types.Address blockCoinbase = Helpers.address(3);
    private long blockEnergyLimit = 10_000_000L;
    private java.math.BigInteger blockDifficulty = java.math.BigInteger.valueOf(1000L);

    private Block block = new Block(new byte[32], blockNumber, blockCoinbase, blockTimstamp, new byte[0]);

    private KernelInterface kernel = new TestingKernel(block);
    private Map<java.lang.String, Integer> eventCounter = new HashMap<>();

    public TestingBlockchainRuntime(IExternalCapabilities capabilities) {
        this.capabilities = capabilities;
    }

    public TestingBlockchainRuntime(IExternalCapabilities capabilities, TransactionContext ctx) {
        this.capabilities = capabilities;
        this.address = (ctx.getTransactionKind() == Type.CREATE.toInt())
            ? capabilities.generateContractAddress(ctx.getTransaction())
            : ctx.getDestinationAddress();
        this.caller = ctx.getSenderAddress();
        this.origin = ctx.getOriginAddress();
        this.value = new BigInteger(ctx.getTransferValue());
        this.data = this.address.toBytes();
        this.energyLimit = ctx.getTransaction().getEnergyLimit();
        this.energyPrice = ctx.getTransactionEnergyPrice();
        this.blockNumber = ctx.getBlockNumber();
        this.blockTimstamp = ctx.getBlockTimestamp();
        this.blockDifficulty = java.math.BigInteger.valueOf(ctx.getBlockDifficulty());
    }

    public TestingBlockchainRuntime withAddress(byte[] address) {
        this.address = org.aion.types.Address.wrap(address);
        return this;
    }

    public TestingBlockchainRuntime withCaller(byte[] caller) {
        this.caller = org.aion.types.Address.wrap(caller);
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

    public TestingBlockchainRuntime withKernel(KernelInterface kernel) {
        this.kernel = kernel;
        return this;
    }

    @Override
    public Address avm_getAddress() {
        return new Address(address.toBytes());
    }

    @Override
    public Address avm_getCaller() {
        return new Address(caller.toBytes());
    }

    @Override
    public Address avm_getOrigin() {
        return new Address(origin.toBytes());
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
        throw RuntimeAssertionError.unimplemented("avm_selfDestruct");
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
        return new Address(blockCoinbase.toBytes());
    }

    @Override
    public BigInteger avm_getBalance(Address address) {
        Objects.requireNonNull(address);
        return new BigInteger(kernel.getBalance(org.aion.types.Address.wrap(address.unwrap())));
    }

    @Override
    public BigInteger avm_getBalanceOfThisContract() {
        return new BigInteger(kernel.getBalance(address));
    }

    @Override
    public int avm_getCodeSize(Address address) {
        Objects.requireNonNull(address);
        byte[] vc = kernel.getTransformedCode(org.aion.types.Address.wrap(address.unwrap()));
        return vc == null ? 0 : vc.length;
    }

    @Override
    public BigInteger avm_getBlockDifficulty() {
        return new BigInteger(blockDifficulty);
    }

    @Override
    public long avm_getRemainingEnergy() {
        throw RuntimeAssertionError.unimplemented("avm_getRemainingEnergy");
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
        return new ByteArray(this.capabilities.blake2b(data.getUnderlying()));
    }

    @Override
    public ByteArray avm_sha256(ByteArray data) {
        Objects.requireNonNull(data);
        return new ByteArray(this.capabilities.sha256(data.getUnderlying()));
    }

    @Override
    public ByteArray avm_keccak256(ByteArray data) {
        Objects.requireNonNull(data);
        return new ByteArray(this.capabilities.keccak256(data.getUnderlying()));
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

        return this.capabilities.verifyEdDSA(data.getUnderlying(), signature.getUnderlying(), publicKey.getUnderlying());
    }

    public int getEventCount(java.lang.String event) {
        Integer count = this.eventCounter.get(event);
        return (null != count) ? count : 0;
    }
}

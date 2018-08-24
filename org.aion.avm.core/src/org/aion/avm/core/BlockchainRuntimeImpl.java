package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.internal.IBlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.types.InternalTransaction;
import org.aion.avm.core.util.HashUtils;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.InvalidException;
import org.aion.avm.internal.RevertException;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.lang.String;
import org.aion.avm.shadow.java.math.BigInteger;
import org.aion.kernel.*;

import java.util.List;
import java.util.Objects;


/**
 * The implementation of IBlockchainRuntime which is appropriate for exposure as a shadow Object instance within a DApp.
 */
public class BlockchainRuntimeImpl implements IBlockchainRuntime {
    private final KernelInterface kernel;
    private final Avm avm;
    private final ReentrantDAppStack.ReentrantState reentrantState;

    private IHelper helper;
    private TransactionContext ctx;
    private final byte[] dAppData;
    private TransactionResult result;

    public BlockchainRuntimeImpl(KernelInterface kernel, Avm avm, ReentrantDAppStack.ReentrantState reentrantState,
                                 IHelper helper, TransactionContext ctx, byte[] dAppData, TransactionResult result) {
        this.kernel = kernel;
        this.avm = avm;
        this.reentrantState = reentrantState;
        this.helper = helper;
        this.ctx = ctx;
        this.dAppData = dAppData;
        this.result = result;
    }

    @Override
    public Address avm_getAddress() {
        return new Address(ctx.getAddress());
    }

    @Override
    public Address avm_getCaller() {
        return new Address(ctx.getCaller());
    }

    @Override
    public Address avm_getOrigin() {
        return new Address(ctx.getOrigin());
    }

    @Override
    public long avm_getEnergyLimit() {
        return ctx.getEnergyLimit();
    }

    @Override
    public long avm_getEnergyPrice() {
        return ctx.getEneryPrice();
    }

    @Override
    public long avm_getValue() {
        return ctx.getValue();
    }

    @Override
    public ByteArray avm_getData() {
        return (null != this.dAppData)
                ? new ByteArray(this.dAppData)
                : null;
    }


    @Override
    public long avm_getBlockTimestamp() {
        return ctx.getBlockTimestamp();
    }

    @Override
    public long avm_getBlockNumber() {
        return ctx.getBlockNumber();
    }

    @Override
    public long avm_getBlockEnergyLimit() {
        return ctx.getBlockEnergyLimit();
    }

    @Override
    public Address avm_getBlockCoinbase() {
        return new Address(ctx.getBlockCoinbase());
    }

    @Override
    public ByteArray avm_getBlockPreviousHash() {
        return new ByteArray(ctx.getBlockPreviousHash());
    }

    @Override
    public BigInteger avm_getBlockDifficulty() {
        return new BigInteger(ctx.getBlockDifficulty());
    }

    @Override
    public ByteArray avm_getStorage(ByteArray key) {
        Objects.requireNonNull(key);

        return new ByteArray(this.kernel.getStorage(ctx.getAddress(), key.getUnderlying()));
    }

    @Override
    public void avm_putStorage(ByteArray key, ByteArray value) {
        Objects.requireNonNull(key);

        this.kernel.putStorage(ctx.getAddress(), key.getUnderlying(), value == null ? null : value.getUnderlying());
    }

    @Override
    public long avm_getBalance(Address address) {
        Objects.requireNonNull(address);

        return this.kernel.getBalance(address.unwrap());
    }

    @Override
    public int avm_getCodeSize(Address address) {
        Objects.requireNonNull(address);

        VersionedCode vc = this.kernel.getCode(address.unwrap());
        return vc == null ? 0 : vc.getCode().length;
    }

    @Override
    public long avm_getRemainingEnergy() {
        return helper.externalGetEnergyRemaining();
    }

    @Override
    public ByteArray avm_call(Address targetAddress, long value, ByteArray data, long energyLimit) {
        if (null != this.reentrantState) {
            // Save our current state into the reentrant container (since this call might be reentrant).
            this.reentrantState.updateEnvironment(helper.captureSnapshotAndNextHashCode());
        }
        // Clear the thread-local helper (since we need to reset it after the call and this should be balanced).
        RuntimeAssertionError.assertTrue(helper == IHelper.currentContractHelper.get());
        IHelper.currentContractHelper.remove();

        // construct the internal transaction
        InternalTransaction internalTx = new InternalTransaction(Transaction.Type.CALL,
                ctx.getAddress(),
                targetAddress.unwrap(),
                value,
                data.getUnderlying(),
                energyLimit,
                ctx.getEneryPrice());
        result.addInternalTransaction(internalTx);

        // execute the internal transaction
        TransactionResult newResult = this.avm.run(new TransactionContextImpl(this.ctx, internalTx));

        // merge the results
        result.merge(newResult);

        // reset the thread-local helper instance
        IHelper.currentContractHelper.set(helper);

        if (null != this.reentrantState) {
            // Update the next hashcode counter, in case this was a reentrant call and it was changed.
            helper.applySpanshotAndNextHashCode(this.reentrantState.getEnvironment().nextHashCode);
        }

        // charge energy consumed
        helper.externalChargeEnergy(newResult.getEnergyUsed());

        byte[] returnData = newResult.getReturnData();
        return (null != returnData)
                ? new ByteArray(returnData)
                : null;
    }

    @Override
    public Address avm_create(long value, ByteArray data, long energyToSend) {
        // TODO: implement CREATE
        return null;
    }

    @Override
    public void avm_selfDestruct(Address beneficiary) {
        // TODO: add value transfer here
        this.kernel.deleteAccount(ctx.getAddress());
    }

    @Override
    public void avm_log(ByteArray data) {
        Log log = new Log(ctx.getAddress(), List.of(), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray data) {
        Log log = new Log(ctx.getAddress(), List.of(topic1.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {
        Log log = new Log(ctx.getAddress(), List.of(topic1.getUnderlying(), topic2.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {
        Log log = new Log(ctx.getAddress(), List.of(topic1.getUnderlying(), topic2.getUnderlying(), topic3.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {
        Log log = new Log(ctx.getAddress(), List.of(topic1.getUnderlying(), topic2.getUnderlying(), topic3.getUnderlying(), topic4.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public ByteArray avm_blake2b(ByteArray data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data can't be NULL");
        }

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

    @Override
    public void avm_print(String message) {
        System.out.print(message);
    }

    @Override
    public void avm_println(String message) {
        System.out.println(message);
    }
}

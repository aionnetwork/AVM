package org.aion.avm.core;

import org.aion.avm.api.Address;
import org.aion.avm.api.Result;
import org.aion.avm.internal.*;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.types.InternalTransaction;
import org.aion.avm.core.util.HashUtils;
import org.aion.kernel.*;
import org.aion.parallel.TransactionTask;

import java.util.List;


/**
 * The implementation of IBlockchainRuntime which is appropriate for exposure as a shadow Object instance within a DApp.
 */
public class BlockchainRuntimeImpl implements IBlockchainRuntime {
    private final KernelInterface kernel;
    private final AvmInternal avm;
    private final ReentrantDAppStack.ReentrantState reentrantState;

    private IHelper helper;
    private TransactionContext ctx;
    private final byte[] dAppData;
    private TransactionResult result;
    private TransactionTask task;

    public BlockchainRuntimeImpl(KernelInterface kernel, AvmInternal avm, ReentrantDAppStack.ReentrantState reentrantState,
                                 IHelper helper, TransactionTask task, TransactionContext ctx, byte[] dAppData, TransactionResult result) {
        this.kernel = kernel;
        this.avm = avm;
        this.reentrantState = reentrantState;
        this.helper = helper;
        this.ctx = ctx;
        this.dAppData = dAppData;
        this.result = result;
        this.task = task;
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
    public org.aion.avm.shadow.java.math.BigInteger avm_getBlockDifficulty() {
        return new org.aion.avm.shadow.java.math.BigInteger(ctx.getBlockDifficulty());
    }

    @Override
    public long avm_getBalance(Address address) {
        require(null != address, "Address can't be NULL");

        return this.kernel.getBalance(address.unwrap());
    }

    @Override
    public int avm_getCodeSize(Address address) {
        require(null != address, "Address can't be NULL");

        byte[] vc = this.kernel.getCode(address.unwrap());
        return vc == null ? 0 : vc.length;
    }

    @Override
    public long avm_getRemainingEnergy() {
        return helper.externalGetEnergyRemaining();
    }

    @Override
    public Result avm_call(Address targetAddress, long value, ByteArray data, long energyLimit) {
        require(targetAddress != null, "Destination can't be NULL");
        require(value >= 0 , "Value can't be negative");
        require(value <= kernel.getBalance(ctx.getAddress()), "Insufficient balance");
        require(data != null, "Data can't be NULL");
        require(energyLimit >= 0, "Energy limit can't be negative");

        if (ctx.getInternalCallDepth() == 10) {
            throw new CallDepthLimitExceededException("Internal call depth cannot be more than 10");
        }

        // construct the internal transaction
        InternalTransaction internalTx = new InternalTransaction(Transaction.Type.CALL,
                ctx.getAddress(),
                targetAddress.unwrap(),
                this.kernel.getNonce(ctx.getAddress()),
                value,
                data.getUnderlying(),
                restrictEnergyLimit(energyLimit),
                ctx.getEneryPrice());
        
        // Call the common run helper.
        return runInternalCall(internalTx);
    }

    @Override
    public Result avm_create(long value, ByteArray data, long energyLimit) {
        require(value >= 0 , "Value can't be negative");
        require(value <= kernel.getBalance(ctx.getAddress()), "Insufficient balance");
        require(data != null, "Data can't be NULL");
        require(energyLimit >= 0, "Energy limit can't be negative");

        if (ctx.getInternalCallDepth() == 10) {
            throw new CallDepthLimitExceededException("Internal call depth cannot be more than 10");
        }

        // construct the internal transaction
        InternalTransaction internalTx = new InternalTransaction(Transaction.Type.CREATE,
                ctx.getAddress(),
                null,
                this.kernel.getNonce(ctx.getAddress()),
                value,
                data.getUnderlying(),
                restrictEnergyLimit(energyLimit),
                ctx.getEneryPrice());
        
        // Call the common run helper.
        return runInternalCall(internalTx);
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void avm_selfDestruct(Address beneficiary) {
        require(null != beneficiary, "Beneficiary can't be NULL");

        // TODO: add value transfer here
        this.kernel.deleteAccount(ctx.getAddress());
    }

    @Override
    public void avm_log(ByteArray data) {
        require(null != data, "data can't be NULL");

        Log log = new Log(ctx.getAddress(), List.of(), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(ctx.getAddress(), List.of(topic1.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != topic2, "topic2 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(ctx.getAddress(), List.of(topic1.getUnderlying(), topic2.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != topic2, "topic2 can't be NULL");
        require(null != topic3, "topic3 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(ctx.getAddress(), List.of(topic1.getUnderlying(), topic2.getUnderlying(), topic3.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != topic2, "topic2 can't be NULL");
        require(null != topic3, "topic3 can't be NULL");
        require(null != topic4, "topic4 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(ctx.getAddress(), List.of(topic1.getUnderlying(), topic2.getUnderlying(), topic3.getUnderlying(), topic4.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public ByteArray avm_blake2b(ByteArray data) {
        require(null != data, "Input data can't be NULL");

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

    // TODO: Create new buffers for print and println, so they can be flushed after parallel transactions
    @Override
    public void avm_print(org.aion.avm.shadow.java.lang.String message) {
        System.out.print(message.toString());
    }

    @Override
    public void avm_println(org.aion.avm.shadow.java.lang.String message) {
        System.out.println(message.toString());
    }

    private long restrictEnergyLimit(long energyLimit) {
        long remainingEnergy = helper.externalGetEnergyRemaining();
        long maxAllowed = remainingEnergy - (remainingEnergy >> 6);
        return Math.min(maxAllowed, energyLimit);
    }

    private Result runInternalCall(InternalTransaction internalTx) {
        // add the internal transaction to result
        result.addInternalTransaction(internalTx);

        if (null != this.reentrantState) {
            // Save our current state into the reentrant container (since this call might be reentrant).
            this.reentrantState.updateEnvironment(helper.captureSnapshotAndNextHashCode());
        }
        // Clear the thread-local helper (since we need to reset it after the call and this should be balanced).
        RuntimeAssertionError.assertTrue(helper == IHelper.currentContractHelper.get());
        IHelper.currentContractHelper.remove();

        // execute the internal transaction
        TransactionResult newResult = this.avm.runInternalTransaction(this.kernel, this.task, new TransactionContextImpl(this.ctx, internalTx));

        // merge the results
        result.merge(newResult);

        // reset the thread-local helper instance
        IHelper.currentContractHelper.set(helper);

        if (null != this.reentrantState) {
            // Update the next hashcode counter, in case this was a reentrant call and it was changed.
            helper.applySnapshotAndNextHashCode(this.reentrantState.getEnvironment().nextHashCode);
        }

        // charge energy consumed
        helper.externalChargeEnergy(newResult.getEnergyUsed());

        return new Result(newResult.getStatusCode().isSuccess(),
                newResult.getReturnData() == null ? null : new ByteArray(newResult.getReturnData()));
    }
}

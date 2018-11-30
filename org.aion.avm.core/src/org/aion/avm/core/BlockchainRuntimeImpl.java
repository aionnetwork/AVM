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

    private TransactionContext ctx;
    private final byte[] dAppData;
    private TransactionResult result;
    private TransactionTask task;
    private final IRuntimeSetup thisDAppSetup;

    public BlockchainRuntimeImpl(KernelInterface kernel, AvmInternal avm, ReentrantDAppStack.ReentrantState reentrantState, TransactionTask task, TransactionContext ctx, byte[] dAppData, TransactionResult result, IRuntimeSetup thisDAppSetup) {
        this.kernel = kernel;
        this.avm = avm;
        this.reentrantState = reentrantState;
        this.ctx = ctx;
        this.dAppData = dAppData;
        this.result = result;
        this.task = task;
        this.thisDAppSetup = thisDAppSetup;
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
        return ctx.getEnergyPrice();
    }

    @Override
    public org.aion.avm.shadow.java.math.BigInteger avm_getValue() {
        return new org.aion.avm.shadow.java.math.BigInteger(ctx.getValue());
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
    public org.aion.avm.shadow.java.math.BigInteger avm_getBalance(Address address) {
        require(null != address, "Address can't be NULL");

        // Acquire resource before reading
        avm.getResourceMonitor().acquire(address.unwrap(), this.task);
        return new org.aion.avm.shadow.java.math.BigInteger(this.kernel.getBalance(address.unwrap()));
    }

    @Override
    public int avm_getCodeSize(Address address) {
        require(null != address, "Address can't be NULL");

        // Acquire resource before reading
        avm.getResourceMonitor().acquire(address.unwrap(), this.task);
        byte[] vc = this.kernel.getCode(address.unwrap());
        return vc == null ? 0 : vc.length;
    }

    @Override
    public long avm_getRemainingEnergy() {
        return IInstrumentation.attachedThreadInstrumentation.get().energyLeft();
    }

    @Override
    public Result avm_call(Address targetAddress, org.aion.avm.shadow.java.math.BigInteger value, ByteArray data, long energyLimit) {
        java.math.BigInteger underlyingValue = value.getUnderlying();
        require(targetAddress != null, "Destination can't be NULL");
        require(underlyingValue.compareTo(java.math.BigInteger.ZERO) >= 0 , "Value can't be negative");
        require(underlyingValue.compareTo(kernel.getBalance(ctx.getAddress())) <= 0, "Insufficient balance");
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
                underlyingValue,
                data.getUnderlying(),
                restrictEnergyLimit(energyLimit),
                ctx.getEnergyPrice());
        
        // Call the common run helper.
        return runInternalCall(internalTx);
    }

    @Override
    public Result avm_create(org.aion.avm.shadow.java.math.BigInteger value, ByteArray data, long energyLimit) {
        java.math.BigInteger underlyingValue = value.getUnderlying();
        require(underlyingValue.compareTo(java.math.BigInteger.ZERO) >= 0 , "Value can't be negative");
        require(underlyingValue.compareTo(kernel.getBalance(ctx.getAddress())) <= 0, "Insufficient balance");
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
                underlyingValue,
                data.getUnderlying(),
                restrictEnergyLimit(energyLimit),
                ctx.getEnergyPrice());
        
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

        byte[] contractAddr = ctx.getAddress();

        // Acquire beneficiary address, the address of current contract is already locked at this stage.
        this.avm.getResourceMonitor().acquire(beneficiary.unwrap(), this.task);

        // Value transfer
        java.math.BigInteger balanceToTransfer = this.kernel.getBalance(contractAddr);
        this.kernel.adjustBalance(contractAddr, balanceToTransfer.negate());
        this.kernel.adjustBalance(beneficiary.unwrap(), balanceToTransfer);

        // Delete Account
        // Note that the account being deleted means it will still run but no DApp which sees this delete
        // (the current one and any callers, or any later transactions, assuming this commits) will be able
        // to invoke it (the code will be missing).
        this.kernel.deleteAccount(contractAddr);
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
    public ByteArray avm_sha256(ByteArray data){
        require(null != data, "Input data can't be NULL");

        return new ByteArray(HashUtils.sha256(data.getUnderlying()));
    }

    @Override
    public ByteArray avm_keccak256(ByteArray data){
        require(null != data, "Input data can't be NULL");

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
    public void avm_print(org.aion.avm.shadow.java.lang.String message) {
        task.outputPrint(message.toString());
    }

    @Override
    public void avm_println(org.aion.avm.shadow.java.lang.String message) {
        task.outputPrintln(message.toString());
    }

    private long restrictEnergyLimit(long energyLimit) {
        long remainingEnergy = IInstrumentation.attachedThreadInstrumentation.get().energyLeft();
        long maxAllowed = remainingEnergy - (remainingEnergy >> 6);
        return Math.min(maxAllowed, energyLimit);
    }

    private Result runInternalCall(InternalTransaction internalTx) {
        // add the internal transaction to result
        result.addInternalTransaction(internalTx);

        IInstrumentation currentThreadInstrumentation = IInstrumentation.attachedThreadInstrumentation.get();
        if (null != this.reentrantState) {
            // Note that we want to save out the current nextHashCode.
            int nextHashCode = currentThreadInstrumentation.peekNextHashCode();
            this.reentrantState.updateEnvironment(nextHashCode);
        }
        // Temporarily detach from the DApp we were in.
        InstrumentationHelpers.temporarilyExitFrame(this.thisDAppSetup);

        TransactionContext internalCTX = new TransactionContextImpl(this.ctx, internalTx);

        // Acquire the target of the internal transaction
        avm.getResourceMonitor().acquire(internalCTX.getAddress(), task);

        // execute the internal transaction
        TransactionResult newResult = null;
        try {
            newResult = this.avm.runInternalTransaction(this.kernel, this.task, internalCTX);
            
            // merge the results
            result.merge(newResult);
        } finally {
            // Re-attach.
            InstrumentationHelpers.returnToExecutingFrame(this.thisDAppSetup);
        }
        
        if (null != this.reentrantState) {
            // Update the next hashcode counter, in case this was a reentrant call and it was changed.
            currentThreadInstrumentation.forceNextHashCode(this.reentrantState.getEnvironment().nextHashCode);
        }

        // charge energy consumed
        currentThreadInstrumentation.chargeEnergy(newResult.getEnergyUsed());

        return new Result(newResult.getStatusCode().isSuccess(),
                newResult.getReturnData() == null ? null : new ByteArray(newResult.getReturnData()));
    }
}

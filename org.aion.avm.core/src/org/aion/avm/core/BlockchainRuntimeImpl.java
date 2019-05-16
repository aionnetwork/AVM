package org.aion.avm.core;

import java.util.Arrays;
import s.java.math.BigInteger;
import p.avm.Address;
import p.avm.Result;
import i.*;
import a.ByteArray;
import org.aion.avm.core.types.InternalTransaction;
import org.aion.avm.core.util.LogSizeUtils;
import org.aion.kernel.*;
import org.aion.parallel.TransactionTask;

import java.util.List;

import org.aion.vm.api.interfaces.KernelInterface;


/**
 * The implementation of IBlockchainRuntime which is appropriate for exposure as a shadow Object instance within a DApp.
 */
public class BlockchainRuntimeImpl implements IBlockchainRuntime {
    private final IExternalCapabilities capabilities;
    private final KernelInterface kernel;
    private final AvmInternal avm;
    private final ReentrantDAppStack.ReentrantState reentrantState;

    private AvmTransaction tx;
    private final byte[] dAppData;
    private TransactionTask task;
    private final IRuntimeSetup thisDAppSetup;

    private ByteArray dAppDataCache;
    private Address addressCache;
    private Address callerCache;
    private Address originCache;
    private BigInteger valueCache;
    private Address blockCoinBaseCache;
    private BigInteger blockDifficultyCache;


    public BlockchainRuntimeImpl(IExternalCapabilities capabilities, KernelInterface kernel, AvmInternal avm, ReentrantDAppStack.ReentrantState reentrantState, TransactionTask task, AvmTransaction tx, byte[] dAppData, IRuntimeSetup thisDAppSetup) {
        this.capabilities = capabilities;
        this.kernel = kernel;
        this.avm = avm;
        this.reentrantState = reentrantState;
        this.tx = tx;
        this.dAppData = dAppData;
        this.task = task;
        this.thisDAppSetup = thisDAppSetup;

        this.dAppDataCache = null;
        this.addressCache = null;
        this.callerCache = null;
        this.originCache = null;
        this.valueCache = null;
        this.blockCoinBaseCache = null;
        this.blockDifficultyCache = null;
    }

    @Override
    public Address avm_getAddress() {
        if (null == this.addressCache) {
            org.aion.types.Address address = tx.destinationAddress;
            this.addressCache = new Address(address.toBytes().clone());
        }

        return this.addressCache;
    }

    @Override
    public Address avm_getCaller() {
        if (null == this.callerCache) {
            this.callerCache = new Address(tx.senderAddress.toBytes().clone());
        }

        return this.callerCache;
    }

    @Override
    public Address avm_getOrigin() {
        if (null == this.originCache) {
            this.originCache = new Address(task.getOriginAddress().toByteArray());
        }

        return this.originCache;
    }

    @Override
    public long avm_getEnergyLimit() {
        return tx.energyLimit;
    }

    @Override
    public long avm_getEnergyPrice() {
        return tx.energyPrice;
    }

    @Override
    public s.java.math.BigInteger avm_getValue() {
        if (null == this.valueCache) {
            java.math.BigInteger value = tx.value;
            this.valueCache = new s.java.math.BigInteger(value);
        }

        return this.valueCache;
    }

    @Override
    public ByteArray avm_getData() {
        if (null == this.dAppDataCache) {
            this.dAppDataCache = (null != this.dAppData)
                    ? new ByteArray(this.dAppData.clone())
                    : null;
        }

        return this.dAppDataCache;
    }


    @Override
    public long avm_getBlockTimestamp() {
        return kernel.getBlockTimestamp();
    }

    @Override
    public long avm_getBlockNumber() {
        return kernel.getBlockNumber();
    }

    @Override
    public long avm_getBlockEnergyLimit() {
        return kernel.getBlockEnergyLimit();
    }

    @Override
    public Address avm_getBlockCoinbase() {
        if (null == this.blockCoinBaseCache) {
            this.blockCoinBaseCache = new Address(kernel.getMinerAddress().toBytes().clone());
        }

        return this.blockCoinBaseCache;
    }

    @Override
    public s.java.math.BigInteger avm_getBlockDifficulty() {
        if (null == this.blockDifficultyCache) {
            this.blockDifficultyCache = s.java.math.BigInteger.avm_valueOf(kernel.getBlockDifficulty());
        }

        return this.blockDifficultyCache;
    }

    @Override
    public void avm_putStorage(ByteArray key, ByteArray value) {
        require(key != null, "Key can't be NULL");
        require(key.getUnderlying().length == 32, "Key must be 32 bytes");

        byte[] keyCopy = Arrays.copyOf(key.getUnderlying(), key.getUnderlying().length);
        byte[] valueCopy = (value == null) ? null : Arrays.copyOf(value.getUnderlying(), value.getUnderlying().length);

        org.aion.types.Address contractAddress = this.tx.destinationAddress;
        if (value == null) {
            kernel.removeStorage(contractAddress, keyCopy);
        } else {
            kernel.putStorage(contractAddress, keyCopy, valueCopy);
        }
    }

    @Override
    public ByteArray avm_getStorage(ByteArray key) {
        require(key != null, "Key can't be NULL");
        require(key.getUnderlying().length == 32, "Key must be 32 bytes");

        org.aion.types.Address contractAddress = this.tx.destinationAddress;
        byte[] data = this.kernel.getStorage(contractAddress, key.getUnderlying());
        return (null != data)
            ? new ByteArray(Arrays.copyOf(data, data.length))
            : null;
    }

    @Override
    public s.java.math.BigInteger avm_getBalance(Address address) {
        require(null != address, "Address can't be NULL");

        // Acquire resource before reading
        avm.getResourceMonitor().acquire(address.toByteArray(), this.task);
        return new s.java.math.BigInteger(this.kernel.getBalance(org.aion.types.Address.wrap(address.toByteArray())));
    }

    @Override
    public s.java.math.BigInteger avm_getBalanceOfThisContract() {
        // This method can be called inside clinit so CREATE is a valid context.
        org.aion.types.Address contractAddress = this.tx.destinationAddress;

        // Acquire resource before reading
        avm.getResourceMonitor().acquire(contractAddress.toBytes(), this.task);
        return new s.java.math.BigInteger(this.kernel.getBalance(contractAddress));
    }

    @Override
    public int avm_getCodeSize(Address address) {
        require(null != address, "Address can't be NULL");

        // Acquire resource before reading
        avm.getResourceMonitor().acquire(address.toByteArray(), this.task);
        byte[] vc = this.kernel.getCode(org.aion.types.Address.wrap(address.toByteArray()));
        return vc == null ? 0 : vc.length;
    }

    @Override
    public long avm_getRemainingEnergy() {
        return IInstrumentation.attachedThreadInstrumentation.get().energyLeft();
    }

    @Override
    public Result avm_call(Address targetAddress, s.java.math.BigInteger value, ByteArray data, long energyLimit) {
        org.aion.types.Address internalSender = this.tx.destinationAddress;

        java.math.BigInteger underlyingValue = value.getUnderlying();
        require(targetAddress != null, "Destination can't be NULL");
        require(underlyingValue.compareTo(java.math.BigInteger.ZERO) >= 0 , "Value can't be negative");
        require(underlyingValue.compareTo(kernel.getBalance(internalSender)) <= 0, "Insufficient balance");
        require(data != null, "Data can't be NULL");
        require(energyLimit >= 0, "Energy limit can't be negative");

        if (task.getTransactionStackDepth() == 9) {
            // since we increase depth in the upcoming call to runInternalCall(),
            // a current depth of 9 means we're about to go up to 10, so we fail
            throw new CallDepthLimitExceededException("Internal call depth cannot be more than 10");
        }

        org.aion.types.Address target = org.aion.types.Address.wrap(targetAddress.toByteArray());
        if (!kernel.destinationAddressIsSafeForThisVM(target)) {
            throw new IllegalArgumentException("Attempt to execute code using a foreign virtual machine");
        }

        // construct the internal transaction
        InternalTransaction internalTx = InternalTransaction.buildTransactionOfTypeCall(
                internalSender,
                target,
                this.kernel.getNonce(internalSender),
                underlyingValue,
                data.getUnderlying(),
                restrictEnergyLimit(energyLimit),
                tx.energyPrice);
        
        // Call the common run helper.
        return runInternalCall(internalTx);
    }

    @Override
    public Result avm_create(s.java.math.BigInteger value, ByteArray data, long energyLimit) {
        org.aion.types.Address internalSender = this.tx.destinationAddress;

        java.math.BigInteger underlyingValue = value.getUnderlying();
        require(underlyingValue.compareTo(java.math.BigInteger.ZERO) >= 0 , "Value can't be negative");
        require(underlyingValue.compareTo(kernel.getBalance(internalSender)) <= 0, "Insufficient balance");
        require(data != null, "Data can't be NULL");
        require(energyLimit >= 0, "Energy limit can't be negative");

        if (task.getTransactionStackDepth() == 9) {
            // since we increase depth in the upcoming call to runInternalCall(),
            // a current depth of 9 means we're about to go up to 10, so we fail
            throw new CallDepthLimitExceededException("Internal call depth cannot be more than 10");
        }

        // construct the internal transaction
        InternalTransaction internalTx = InternalTransaction.buildTransactionOfTypeCreate(
                internalSender,
                this.kernel.getNonce(internalSender),
                underlyingValue,
                data.getUnderlying(),
                restrictEnergyLimit(energyLimit),
                tx.energyPrice);
        
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

        org.aion.types.Address contractAddr = this.tx.destinationAddress;

        // Acquire beneficiary address, the address of current contract is already locked at this stage.
        this.avm.getResourceMonitor().acquire(beneficiary.toByteArray(), this.task);

        // Value transfer
        java.math.BigInteger balanceToTransfer = this.kernel.getBalance(contractAddr);
        this.kernel.adjustBalance(contractAddr, balanceToTransfer.negate());
        this.kernel.adjustBalance(org.aion.types.Address.wrap(beneficiary.toByteArray()), balanceToTransfer);

        // Delete Account
        // Note that the account being deleted means it will still run but no DApp which sees this delete
        // (the current one and any callers, or any later transactions, assuming this commits) will be able
        // to invoke it (the code will be missing).
        this.kernel.deleteAccount(contractAddr);
        task.addSelfDestructAddress(contractAddr);
    }

    @Override
    public void avm_log(ByteArray data) {
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.destinationAddress.toBytes(),
                List.of(),
                data.getUnderlying()
        );
        task.peekSideEffects().addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.destinationAddress.toBytes(),
                List.of(LogSizeUtils.truncatePadTopic(topic1.getUnderlying())),
                data.getUnderlying()
        );
        task.peekSideEffects().addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != topic2, "topic2 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.destinationAddress.toBytes(),
                List.of(LogSizeUtils.truncatePadTopic(topic1.getUnderlying()), LogSizeUtils.truncatePadTopic(topic2.getUnderlying())),
                data.getUnderlying()
        );
        task.peekSideEffects().addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != topic2, "topic2 can't be NULL");
        require(null != topic3, "topic3 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.destinationAddress.toBytes(),
                List.of(LogSizeUtils.truncatePadTopic(topic1.getUnderlying()), LogSizeUtils.truncatePadTopic(topic2.getUnderlying()), LogSizeUtils.truncatePadTopic(topic3.getUnderlying())),
                data.getUnderlying()
        );
        task.peekSideEffects().addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != topic2, "topic2 can't be NULL");
        require(null != topic3, "topic3 can't be NULL");
        require(null != topic4, "topic4 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.destinationAddress.toBytes(),
                List.of(LogSizeUtils.truncatePadTopic(topic1.getUnderlying()), LogSizeUtils.truncatePadTopic(topic2.getUnderlying()), LogSizeUtils.truncatePadTopic(topic3.getUnderlying()), LogSizeUtils.truncatePadTopic(topic4.getUnderlying())),
                data.getUnderlying()
        );
        task.peekSideEffects().addLog(log);
    }

    @Override
    public ByteArray avm_blake2b(ByteArray data) {
        require(null != data, "Input data can't be NULL");

        return new ByteArray(this.capabilities.blake2b(data.getUnderlying()));
    }

    @Override
    public ByteArray avm_sha256(ByteArray data){
        require(null != data, "Input data can't be NULL");

        return new ByteArray(this.capabilities.sha256(data.getUnderlying()));
    }

    @Override
    public ByteArray avm_keccak256(ByteArray data){
        require(null != data, "Input data can't be NULL");

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
    public void avm_print(s.java.lang.String message) {
        task.outputPrint(message.toString());
    }

    @Override
    public void avm_println(s.java.lang.String message) {
        task.outputPrintln(message.toString());
    }

    @Override
    public boolean avm_edVerify(ByteArray data, ByteArray signature, ByteArray publicKey) throws IllegalArgumentException {
        require(null != data, "Input data can't be NULL");
        require(null != signature, "Input signature can't be NULL");
        require(null != publicKey, "Input public key can't be NULL");

        return this.capabilities.verifyEdDSA(data.getUnderlying(), signature.getUnderlying(), publicKey.getUnderlying());
    }

    private long restrictEnergyLimit(long energyLimit) {
        long remainingEnergy = IInstrumentation.attachedThreadInstrumentation.get().energyLeft();
        long maxAllowed = remainingEnergy - (remainingEnergy >> 6);
        return Math.min(maxAllowed, energyLimit);
    }

    private Result runInternalCall(InternalTransaction internalTx) {
        // add the internal transaction to result
        task.peekSideEffects().addInternalTransaction(internalTx);

        // we should never leave this method without decrementing this
        task.incrementTransactionStackDepth();

        IInstrumentation currentThreadInstrumentation = IInstrumentation.attachedThreadInstrumentation.get();
        if (null != this.reentrantState) {
            // Note that we want to save out the current nextHashCode.
            int nextHashCode = currentThreadInstrumentation.peekNextHashCode();
            this.reentrantState.updateNextHashCode(nextHashCode);
        }
        // Temporarily detach from the DApp we were in.
        InstrumentationHelpers.temporarilyExitFrame(this.thisDAppSetup);

        // Create the AvmTransaction.
        AvmTransaction avmTransaction = AvmTransaction.from(this.capabilities, internalTx);

        // Acquire the target of the internal transaction
        org.aion.types.Address target = avmTransaction.destinationAddress;
        avm.getResourceMonitor().acquire(target.toBytes(), task);

        // execute the internal transaction
        AvmTransactionResult newResult = null;
        try {
            newResult = this.avm.runInternalTransaction(this.kernel, this.task, avmTransaction);
        } finally {
            // Re-attach.
            InstrumentationHelpers.returnToExecutingFrame(this.thisDAppSetup);
        }
        
        if (null != this.reentrantState) {
            // Update the next hashcode counter, in case this was a reentrant call and it was changed.
            currentThreadInstrumentation.forceNextHashCode(this.reentrantState.getNextHashCode());
        }

        // charge energy consumed
        currentThreadInstrumentation.chargeEnergy(newResult.getEnergyUsed());

        task.decrementTransactionStackDepth();

        return new Result(newResult.getResultCode().isSuccess(),
                newResult.getReturnData() == null ? null : new ByteArray(Arrays.copyOf(newResult.getReturnData(), newResult.getReturnData().length)));
    }
}

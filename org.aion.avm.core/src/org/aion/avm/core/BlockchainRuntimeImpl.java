package org.aion.avm.core;

import java.math.BigInteger;
import java.util.Arrays;
import org.aion.avm.core.util.TransactionResultUtil;
import org.aion.types.AionAddress;
import p.avm.Address;
import p.avm.Result;
import i.*;
import a.ByteArray;
import org.aion.types.InternalTransaction;
import org.aion.avm.core.util.LogSizeUtils;
import org.aion.kernel.*;
import org.aion.parallel.TransactionTask;
import org.aion.types.Log;

import java.util.List;

/**
 * The implementation of IBlockchainRuntime which is appropriate for exposure as a shadow Object instance within a DApp.
 */
public class BlockchainRuntimeImpl implements IBlockchainRuntime {
    private final IExternalCapabilities capabilities;
    private final IExternalState externalState;
    private final AvmInternal avm;
    private final ReentrantDAppStack.ReentrantState reentrantState;

    private final TransactionTask task;
    private final AionAddress transactionSender;
    private final AionAddress transactionDestination;
    private final AionAddress effectiveTransactionOrigin;
    private final byte[] dAppData;
    private final byte[] effectiveTransactionHash;
    private final long energyLimit;
    private final long energyPrice;
    private final BigInteger transactionValue;
    private final IRuntimeSetup thisDAppSetup;
    private final boolean enablePrintln;

    private ByteArray dAppDataCache;
    private ByteArray transactionHashCache;
    private Address addressCache;
    private Address callerCache;
    private Address originCache;
    private s.java.math.BigInteger valueCache;
    private Address blockCoinBaseCache;
    private s.java.math.BigInteger blockDifficultyCache;


    public BlockchainRuntimeImpl(IExternalCapabilities capabilities
            , IExternalState externalState
            , AvmInternal avm
            , ReentrantDAppStack.ReentrantState reentrantState
            , TransactionTask task
            , AionAddress transactionSender
            , AionAddress transactionDestination
            , AionAddress effectiveTransactionOrigin
            , byte[] dAppData
            , byte[] effectiveTransactionHash
            , long energyLimit
            , long energyPrice
            , BigInteger transactionValue
            , IRuntimeSetup thisDAppSetup
            , boolean enablePrintln
    ) {
        this.capabilities = capabilities;
        this.externalState = externalState;
        this.avm = avm;
        this.reentrantState = reentrantState;
        this.task = task;
        this.transactionSender = transactionSender;
        // Note that transactionDestination will be the address of the deployed contract, if this is a create.
        this.transactionDestination = transactionDestination;
        // (the "effective" origin is so named since it might be the origin address of an external transaction or the sender of an invokable)
        this.effectiveTransactionOrigin = effectiveTransactionOrigin;
        this.dAppData = dAppData;
        this.effectiveTransactionHash = effectiveTransactionHash;
        this.energyLimit = energyLimit;
        this.energyPrice = energyPrice;
        this.transactionValue = transactionValue;
        this.thisDAppSetup = thisDAppSetup;
        this.enablePrintln = enablePrintln;

        this.dAppDataCache = null;
        this.addressCache = null;
        this.callerCache = null;
        this.originCache = null;
        this.valueCache = null;
        this.blockCoinBaseCache = null;
        this.blockDifficultyCache = null;
    }

    @Override
    public ByteArray avm_getTransactionHash() {
        if (null == this.transactionHashCache) {
            this.transactionHashCache = new ByteArray(this.effectiveTransactionHash.clone());
        }
        return this.transactionHashCache;
    }

    @Override
    public Address avm_getAddress() {
        if (null == this.addressCache) {
            this.addressCache = new Address(this.transactionDestination.toByteArray());
        }

        return this.addressCache;
    }

    @Override
    public Address avm_getCaller() {
        if (null == this.callerCache) {
            this.callerCache = new Address(this.transactionSender.toByteArray());
        }

        return this.callerCache;
    }

    @Override
    public Address avm_getOrigin() {
        if (null == this.originCache) {
            this.originCache = new Address(this.effectiveTransactionOrigin.toByteArray());
        }

        return this.originCache;
    }

    @Override
    public long avm_getEnergyLimit() {
        return this.energyLimit;
    }

    @Override
    public long avm_getEnergyPrice() {
        return this.energyPrice;
    }

    @Override
    public s.java.math.BigInteger avm_getValue() {
        if (null == this.valueCache) {
            this.valueCache = new s.java.math.BigInteger(this.transactionValue);
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
        return externalState.getBlockTimestamp();
    }

    @Override
    public long avm_getBlockNumber() {
        return externalState.getBlockNumber();
    }

    @Override
    public long avm_getBlockEnergyLimit() {
        return externalState.getBlockEnergyLimit();
    }

    @Override
    public Address avm_getBlockCoinbase() {
        if (null == this.blockCoinBaseCache) {
            this.blockCoinBaseCache = new Address(externalState.getMinerAddress().toByteArray());
        }

        return this.blockCoinBaseCache;
    }

    @Override
    public s.java.math.BigInteger avm_getBlockDifficulty() {
        if (null == this.blockDifficultyCache) {
            this.blockDifficultyCache = new s.java.math.BigInteger(externalState.getBlockDifficulty());
        }

        return this.blockDifficultyCache;
    }

    @Override
    public void avm_putStorage(ByteArray key, ByteArray value, boolean requiresRefund) {
        require(key != null, "Key can't be NULL");
        require(key.getUnderlying().length == 32, "Key must be 32 bytes");

        byte[] keyCopy = Arrays.copyOf(key.getUnderlying(), key.getUnderlying().length);
        byte[] valueCopy = (value == null) ? null : Arrays.copyOf(value.getUnderlying(), value.getUnderlying().length);

        if (value == null) {
            externalState.removeStorage(this.transactionDestination, keyCopy);
        } else {
            externalState.putStorage(this.transactionDestination, keyCopy, valueCopy);
        }
        if(requiresRefund){
            task.addResetStoragekey(this.transactionDestination, keyCopy);
        }
    }

    @Override
    public ByteArray avm_getStorage(ByteArray key) {
        require(key != null, "Key can't be NULL");
        require(key.getUnderlying().length == 32, "Key must be 32 bytes");

        byte[] data = this.externalState.getStorage(this.transactionDestination, key.getUnderlying());
        return (null != data)
            ? new ByteArray(Arrays.copyOf(data, data.length))
            : null;
    }

    @Override
    public s.java.math.BigInteger avm_getBalance(Address address) {
        require(null != address, "Address can't be NULL");

        // Acquire resource before reading
        // Returned result of acquire is not checked, since an abort exception will be thrown by IInstrumentation during chargeEnergy if the task has been aborted
        avm.getResourceMonitor().acquire(address.toByteArray(), this.task);
        return new s.java.math.BigInteger(this.externalState.getBalance(new AionAddress(address.toByteArray())));
    }

    @Override
    public s.java.math.BigInteger avm_getBalanceOfThisContract() {
        // This method can be called inside clinit so CREATE is a valid context.
        // Acquire resource before reading
        // Returned result of acquire is not checked, since an abort exception will be thrown by IInstrumentation during chargeEnergy if the task has been aborted
        avm.getResourceMonitor().acquire(this.transactionDestination.toByteArray(), this.task);
        return new s.java.math.BigInteger(this.externalState.getBalance(this.transactionDestination));
    }

    @Override
    public int avm_getCodeSize(Address address) {
        require(null != address, "Address can't be NULL");

        // Acquire resource before reading
        // Returned result of acquire is not checked, since an abort exception will be thrown by IInstrumentation during chargeEnergy if the task has been aborted
        avm.getResourceMonitor().acquire(address.toByteArray(), this.task);
        byte[] vc = this.externalState.getCode(new AionAddress(address.toByteArray()));
        return vc == null ? 0 : vc.length;
    }

    @Override
    public long avm_getRemainingEnergy() {
        return IInstrumentation.attachedThreadInstrumentation.get().energyLeft();
    }

    @Override
    public Result avm_call(Address targetAddress, s.java.math.BigInteger value, ByteArray data, long energyLimit) {
        java.math.BigInteger underlyingValue = value.getUnderlying();
        require(targetAddress != null, "Destination can't be NULL");
        require(underlyingValue.compareTo(java.math.BigInteger.ZERO) >= 0 , "Value can't be negative");
        require(underlyingValue.compareTo(externalState.getBalance(this.transactionDestination)) <= 0, "Insufficient balance");
        require(data != null, "Data can't be NULL");
        require(energyLimit >= 0, "Energy limit can't be negative");

        if (task.getTransactionStackDepth() == 9) {
            // since we increase depth in the upcoming call to runInternalCall(),
            // a current depth of 9 means we're about to go up to 10, so we fail
            throw new CallDepthLimitExceededException("Internal call depth cannot be more than 10");
        }

        AionAddress target = new AionAddress(targetAddress.toByteArray());
        if (!externalState.destinationAddressIsSafeForThisVM(target)) {
            throw new IllegalArgumentException("Attempt to execute code using a foreign virtual machine");
        }

        // construct the internal transaction
        BigInteger senderNonce = this.externalState.getNonce(this.transactionDestination);
        long restrictedLimit = restrictEnergyLimit(energyLimit);
        InternalTransaction internalTx = InternalTransaction.contractCallTransaction(
                InternalTransaction.RejectedStatus.NOT_REJECTED,
                this.transactionDestination,
                target,
                senderNonce,
                underlyingValue,
                data.getUnderlying(),
                restrictedLimit,
                this.energyPrice);
        
        // Call the common run helper.
        return runInternalCall(internalTx, this.transactionDestination, false, target, this.effectiveTransactionOrigin, data.getUnderlying(), this.effectiveTransactionHash, restrictedLimit, this.energyPrice, underlyingValue, senderNonce);
    }

    @Override
    public Result avm_create(s.java.math.BigInteger value, ByteArray data, long energyLimit) {
        java.math.BigInteger underlyingValue = value.getUnderlying();
        require(underlyingValue.compareTo(java.math.BigInteger.ZERO) >= 0 , "Value can't be negative");
        require(underlyingValue.compareTo(externalState.getBalance(this.transactionDestination)) <= 0, "Insufficient balance");
        require(data != null, "Data can't be NULL");
        require(energyLimit >= 0, "Energy limit can't be negative");

        if (task.getTransactionStackDepth() == 9) {
            // since we increase depth in the upcoming call to runInternalCall(),
            // a current depth of 9 means we're about to go up to 10, so we fail
            throw new CallDepthLimitExceededException("Internal call depth cannot be more than 10");
        }

        // construct the internal transaction
        BigInteger senderNonce = this.externalState.getNonce(this.transactionDestination);
        long restrictedLimit = restrictEnergyLimit(energyLimit);
        InternalTransaction internalTx = InternalTransaction.contractCreateTransaction(
                InternalTransaction.RejectedStatus.NOT_REJECTED,
                this.transactionDestination,
                senderNonce,
                underlyingValue,
                data.getUnderlying(),
                restrictedLimit,
                this.energyPrice);
        
        // Call the common run helper.
        return runInternalCall(internalTx, this.transactionDestination, true, null, this.effectiveTransactionOrigin, data.getUnderlying(), this.effectiveTransactionHash, restrictedLimit, this.energyPrice, underlyingValue, senderNonce);
    }

    @Override
    public Result avm_invokeTransaction(ByteArray transactionPayload, long energyLimit) throws IllegalArgumentException {
        // Static checks.
        require(transactionPayload != null, "Payload can't be NULL");
        require(energyLimit >= 0, "Energy limit can't be negative");

        if (task.getTransactionStackDepth() == 9) {
            // since we increase depth in the upcoming call to runInternalCall(),
            // a current depth of 9 means we're about to go up to 10, so we fail
            throw new CallDepthLimitExceededException("Internal call depth cannot be more than 10");
        }
        
        // Get the information we need.
        byte[] underlyingTransactionPayload = transactionPayload.getUnderlying();
        long effectiveEnergyLimit = restrictEnergyLimit(energyLimit);
        AionAddress currentContract = this.transactionDestination;
        
        // Ask the kernel to decode this transaction - returns null on failure.
        InternalTransaction deserializedTransaction = this.capabilities.decodeSerializedTransaction(underlyingTransactionPayload, currentContract, this.energyPrice, effectiveEnergyLimit);
        require(null != deserializedTransaction, "Transaction static requirements violated");
        
        if (!deserializedTransaction.isCreate) {
            require(this.externalState.destinationAddressIsSafeForThisVM(deserializedTransaction.destination), "Attempt to execute code using a foreign virtual machine");
        }
        
        // At this point, we know the transaction was well-formed and no static requirements were validated.
        // This means we can verify the context-dependent rules:
        // -value
        // -nonce
        // This is effectively an application of the rules we normally apply in the avm_call and avm_create helpers, just applied
        // to the deserialized transaction.  Nonce is the only new check here, since it is normally applied on the external call.
        require(deserializedTransaction.value.compareTo(java.math.BigInteger.ZERO) >= 0 , "Value can't be negative");
        require(deserializedTransaction.value.compareTo(externalState.getBalance(deserializedTransaction.sender)) <= 0, "Insufficient balance");
        require(deserializedTransaction.senderNonce.equals(this.externalState.getNonce(deserializedTransaction.sender)), "Incorrect nonce");
        
        // Call the common run helper, passing in the deserialized transaction.
        return runInternalCall(deserializedTransaction, deserializedTransaction.sender, deserializedTransaction.isCreate, deserializedTransaction.destination, deserializedTransaction.sender, deserializedTransaction.copyOfData(), deserializedTransaction.copyOfInvokableHash(), deserializedTransaction.energyLimit, deserializedTransaction.energyPrice, deserializedTransaction.value, deserializedTransaction.senderNonce);
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    @Override
    public void avm_selfDestruct(Address beneficiary) {
        require(null != beneficiary, "Beneficiary can't be NULL");

        // Acquire beneficiary address, the address of current contract is already locked at this stage.
        // Returned result of acquire is not checked, since an abort exception will be thrown by IInstrumentation during chargeEnergy if the task has been aborted
        this.avm.getResourceMonitor().acquire(beneficiary.toByteArray(), this.task);

        // Value transfer
        java.math.BigInteger balanceToTransfer = this.externalState.getBalance(this.transactionDestination);
        this.externalState.adjustBalance(this.transactionDestination, balanceToTransfer.negate());
        this.externalState
            .adjustBalance(new AionAddress(beneficiary.toByteArray()), balanceToTransfer);

        // Delete Account
        // Note that the account being deleted means it will still run but no DApp which sees this delete
        // (the current one and any callers, or any later transactions, assuming this commits) will be able
        // to invoke it (the code will be missing).
        this.externalState.deleteAccount(this.transactionDestination);
        task.addSelfDestructAddress(this.transactionDestination);
    }

    @Override
    public void avm_log(ByteArray data) {
        require(null != data, "data can't be NULL");

        Log log = Log.dataOnly(this.transactionDestination.toByteArray(), data.getUnderlying());
        task.peekSideEffects().addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = Log.topicsAndData(this.transactionDestination.toByteArray(),
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

        Log log = Log.topicsAndData(this.transactionDestination.toByteArray(),
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

        Log log = Log.topicsAndData(this.transactionDestination.toByteArray(),
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

        Log log = Log.topicsAndData(this.transactionDestination.toByteArray(),
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
        if (this.enablePrintln) {
            task.outputPrint(message.toString());
        }
    }

    @Override
    public void avm_println(s.java.lang.String message) {
        if (this.enablePrintln) {
            task.outputPrintln(message.toString());
        }
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
        // An internal transaction cannot take all the energy - 1/64th of remaining is reserved to get out of the parent transaction.
        long reservedForExisting = (remainingEnergy >> 6);
        long maxAllowed = remainingEnergy - reservedForExisting;
        return Math.min(maxAllowed, energyLimit);
    }

    private Result runInternalCall(InternalTransaction internalTx
            , AionAddress senderAddress
            , boolean isCreate
            , AionAddress normalCallTarget  // Null if this is a create.
            , AionAddress effectiveTransactionOrigin
            , byte[] transactionData
            , byte[] transactionHash
            , long energyLimit
            , long energyPrice
            , BigInteger transactionValue
            , BigInteger nonce
    ) {
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

        // Acquire the target of the internal transaction
        // Note that we calculate the target, in the case of a create, only to acquire the resource lock and then discard it (will be logically recreated later when required).
        AionAddress destination = (isCreate) ? this.capabilities.generateContractAddress(senderAddress, nonce) : normalCallTarget;
        boolean isAcquired = avm.getResourceMonitor().acquire(destination.toByteArray(), task);

        // execute the internal transaction
        AvmWrappedTransactionResult newResult = null;
        try {
            if(isAcquired) {
                newResult = this.avm.runInternalTransaction(this.externalState, this.task, senderAddress, isCreate, normalCallTarget, effectiveTransactionOrigin, transactionData, transactionHash, energyLimit, energyPrice, transactionValue, nonce);
            } else {
                // Unsuccessful acquire means transaction task has been aborted.
                // In abort case, internal transaction will not be executed.
                newResult = TransactionResultUtil.newAbortedResultWithZeroEnergyUsed();
            }
        } finally {
            // Re-attach.
            InstrumentationHelpers.returnToExecutingFrame(this.thisDAppSetup);
        }
        
        if (null != this.reentrantState) {
            // Update the next hashcode counter, in case this was a reentrant call and it was changed.
            currentThreadInstrumentation.forceNextHashCode(this.reentrantState.getNextHashCode());
        }

        // Note that we can only meaningfully charge energy if the transaction was NOT aborted and it actually ran something (balance transfers report zero energy used, here).
        if (isAcquired) {
            // charge energy consumed
            long energyUsed = newResult.energyUsed();
            if (0L != energyUsed) {
                // We know that this must be a positive integer.
                RuntimeAssertionError.assertTrue(energyUsed > 0L);
                RuntimeAssertionError.assertTrue(energyUsed <= (long)Integer.MAX_VALUE);
                currentThreadInstrumentation.chargeEnergy((int)energyUsed);
            }
        }

        task.decrementTransactionStackDepth();

        byte[] output = newResult.output();
        return new Result(newResult.isSuccess(), output == null ? null : new ByteArray(output));
    }
}

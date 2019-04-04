package org.aion.avm.core;

import org.aion.avm.shadowapi.avm.Address;
import org.aion.avm.shadowapi.avm.Result;
import org.aion.avm.internal.*;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.types.InternalTransaction;
import org.aion.avm.core.util.LogSizeUtils;
import org.aion.kernel.*;
import org.aion.kernel.Transaction.Type;
import org.aion.parallel.TransactionTask;

import java.util.List;
import org.aion.vm.api.interfaces.KernelInterface;
import org.aion.vm.api.interfaces.TransactionInterface;


/**
 * The implementation of IBlockchainRuntime which is appropriate for exposure as a shadow Object instance within a DApp.
 */
public class BlockchainRuntimeImpl implements IBlockchainRuntime {
    private final IExternalCapabilities capabilities;
    private final KernelInterface kernel;
    private final AvmInternal avm;
    private final ReentrantDAppStack.ReentrantState reentrantState;

    private TransactionInterface tx;
    private final byte[] dAppData;
    private TransactionTask task;
    private final IRuntimeSetup thisDAppSetup;

    public BlockchainRuntimeImpl(IExternalCapabilities capabilities, KernelInterface kernel, AvmInternal avm, ReentrantDAppStack.ReentrantState reentrantState, TransactionTask task, TransactionInterface tx, byte[] dAppData, IRuntimeSetup thisDAppSetup) {
        this.capabilities = capabilities;
        this.kernel = kernel;
        this.avm = avm;
        this.reentrantState = reentrantState;
        this.tx = tx;
        this.dAppData = dAppData;
        this.task = task;
        this.thisDAppSetup = thisDAppSetup;
    }

    @Override
    public Address avm_getAddress() {
        org.aion.types.Address address = (tx.getKind() == Type.CREATE.toInt()) ? this.capabilities.generateContractAddress(this.tx) : tx.getDestinationAddress();
        return new Address(address.toBytes());
    }

    @Override
    public Address avm_getCaller() {
        return new Address(tx.getSenderAddress().toBytes());
    }

    @Override
    public Address avm_getOrigin() {
        return new Address(task.getOriginAddress().unwrap());
    }

    @Override
    public long avm_getEnergyLimit() {
        return tx.getEnergyLimit();
    }

    @Override
    public long avm_getEnergyPrice() {
        return tx.getEnergyPrice();
    }

    @Override
    public org.aion.avm.shadow.java.math.BigInteger avm_getValue() {
        java.math.BigInteger value = new java.math.BigInteger(tx.getValue());
        return new org.aion.avm.shadow.java.math.BigInteger(value);
    }

    @Override
    public ByteArray avm_getData() {
        return (null != this.dAppData)
                ? new ByteArray(this.dAppData)
                : null;
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
        return new Address(kernel.getMinerAddress().toBytes());
    }

    @Override
    public org.aion.avm.shadow.java.math.BigInteger avm_getBlockDifficulty() {
        return org.aion.avm.shadow.java.math.BigInteger.avm_valueOf(kernel.getBlockDifficulty());
    }

    @Override
    public org.aion.avm.shadow.java.math.BigInteger avm_getBalance(Address address) {
        require(null != address, "Address can't be NULL");

        // Acquire resource before reading
        avm.getResourceMonitor().acquire(address.unwrap(), this.task);
        return new org.aion.avm.shadow.java.math.BigInteger(this.kernel.getBalance(org.aion.types.Address.wrap(address.unwrap())));
    }

    @Override
    public org.aion.avm.shadow.java.math.BigInteger avm_getBalanceOfThisContract() {
        // This method can be called inside clinit so CREATE is a valid context.
        org.aion.types.Address contractAddress = (tx.isContractCreationTransaction())
            ? this.capabilities.generateContractAddress(this.tx)
            : tx.getDestinationAddress();

        // Acquire resource before reading
        avm.getResourceMonitor().acquire(contractAddress.toBytes(), this.task);
        return new org.aion.avm.shadow.java.math.BigInteger(this.kernel.getBalance(contractAddress));
    }

    @Override
    public int avm_getCodeSize(Address address) {
        require(null != address, "Address can't be NULL");

        // Acquire resource before reading
        avm.getResourceMonitor().acquire(address.unwrap(), this.task);
        byte[] vc = this.kernel.getTransformedCode(org.aion.types.Address.wrap(address.unwrap()));
        return vc == null ? 0 : vc.length;
    }

    @Override
    public long avm_getRemainingEnergy() {
        return IInstrumentation.attachedThreadInstrumentation.get().energyLeft();
    }

    @Override
    public Result avm_call(Address targetAddress, org.aion.avm.shadow.java.math.BigInteger value, ByteArray data, long energyLimit) {
        org.aion.types.Address internalSender = (tx.getKind() == Type.CREATE.toInt()) ? this.capabilities.generateContractAddress(this.tx) : tx.getDestinationAddress();

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

        org.aion.types.Address target = org.aion.types.Address.wrap(targetAddress.unwrap());
        if (!kernel.destinationAddressIsSafeForThisVM(target)) {
            throw new IllegalArgumentException("Attempt to execute code using a foreign virtual machine");
        }

        // construct the internal transaction
        InternalTransaction internalTx = new InternalTransaction(Type.CALL,
                internalSender,
                target,
                this.kernel.getNonce(internalSender),
                underlyingValue,
                data.getUnderlying(),
                restrictEnergyLimit(energyLimit),
                tx.getEnergyPrice());
        
        // Call the common run helper.
        return runInternalCall(internalTx);
    }

    @Override
    public Result avm_create(org.aion.avm.shadow.java.math.BigInteger value, ByteArray data, long energyLimit) {
        org.aion.types.Address internalSender = (tx.getKind() == Type.CREATE.toInt()) ? this.capabilities.generateContractAddress(this.tx) : tx.getDestinationAddress();

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
        InternalTransaction internalTx = new InternalTransaction(Type.CREATE,
                internalSender,
                null,
                this.kernel.getNonce(internalSender),
                underlyingValue,
                data.getUnderlying(),
                restrictEnergyLimit(energyLimit),
                tx.getEnergyPrice());
        
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

        org.aion.types.Address contractAddr = (tx.getKind() == Type.CREATE.toInt())
            ? this.capabilities.generateContractAddress(this.tx)
            : tx.getDestinationAddress();

        // Acquire beneficiary address, the address of current contract is already locked at this stage.
        this.avm.getResourceMonitor().acquire(beneficiary.unwrap(), this.task);

        // Value transfer
        java.math.BigInteger balanceToTransfer = this.kernel.getBalance(contractAddr);
        this.kernel.adjustBalance(contractAddr, balanceToTransfer.negate());
        this.kernel.adjustBalance(org.aion.types.Address.wrap(beneficiary.unwrap()), balanceToTransfer);

        // Delete Account
        // Note that the account being deleted means it will still run but no DApp which sees this delete
        // (the current one and any callers, or any later transactions, assuming this commits) will be able
        // to invoke it (the code will be missing).
        this.kernel.deleteAccount(contractAddr);
    }

    @Override
    public void avm_log(ByteArray data) {
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.getDestinationAddress().toBytes(),
                List.of(),
                data.getUnderlying()
        );
        task.getSideEffects().addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.getDestinationAddress().toBytes(),
                List.of(LogSizeUtils.truncatePadTopic(topic1.getUnderlying())),
                data.getUnderlying()
        );
        task.getSideEffects().addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != topic2, "topic2 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.getDestinationAddress().toBytes(),
                List.of(LogSizeUtils.truncatePadTopic(topic1.getUnderlying()), LogSizeUtils.truncatePadTopic(topic2.getUnderlying())),
                data.getUnderlying()
        );
        task.getSideEffects().addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != topic2, "topic2 can't be NULL");
        require(null != topic3, "topic3 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.getDestinationAddress().toBytes(),
                List.of(LogSizeUtils.truncatePadTopic(topic1.getUnderlying()), LogSizeUtils.truncatePadTopic(topic2.getUnderlying()), LogSizeUtils.truncatePadTopic(topic3.getUnderlying())),
                data.getUnderlying()
        );
        task.getSideEffects().addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {
        require(null != topic1, "topic1 can't be NULL");
        require(null != topic2, "topic2 can't be NULL");
        require(null != topic3, "topic3 can't be NULL");
        require(null != topic4, "topic4 can't be NULL");
        require(null != data, "data can't be NULL");

        Log log = new Log(this.capabilities, tx.getDestinationAddress().toBytes(),
                List.of(LogSizeUtils.truncatePadTopic(topic1.getUnderlying()), LogSizeUtils.truncatePadTopic(topic2.getUnderlying()), LogSizeUtils.truncatePadTopic(topic3.getUnderlying()), LogSizeUtils.truncatePadTopic(topic4.getUnderlying())),
                data.getUnderlying()
        );
        task.getSideEffects().addLog(log);
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
    public void avm_print(org.aion.avm.shadow.java.lang.String message) {
        task.outputPrint(message.toString());
    }

    @Override
    public void avm_println(org.aion.avm.shadow.java.lang.String message) {
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
        task.getSideEffects().addInternalTransaction(internalTx);

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
        org.aion.types.Address target = (internalTx.getKind() == Type.CREATE.toInt())
            ? this.capabilities.generateContractAddress(internalTx)
            : internalTx.getDestinationAddress();
        avm.getResourceMonitor().acquire(target.toBytes(), task);

        // execute the internal transaction
        AvmTransactionResult newResult = null;
        try {
            newResult = this.avm.runInternalTransaction(this.kernel, this.task, internalTx);
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
                newResult.getReturnData() == null ? null : new ByteArray(newResult.getReturnData()));
    }
}

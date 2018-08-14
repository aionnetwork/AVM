package org.aion.avm.core;

import java.util.List;

import org.aion.avm.api.Address;
import org.aion.avm.api.IBlockchainRuntime;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.core.types.InternalTransaction;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.shadow.java.math.BigInteger;
import org.aion.kernel.*;


/**
 * The implementation of IBlockchainRuntime which is appropriate for exposure as a shadow Object instance within a DApp.
 */
public class BlockchainRuntimeImpl extends org.aion.avm.shadow.java.lang.Object implements IBlockchainRuntime {
    private final KernelInterface kernel;
    private final Avm avm;
    private final ReentrantDAppStack.ReentrantState reentrantState;
    private IHelper helper;
    private TransactionResult result;

    private Transaction tx;
    private Block block;

    public BlockchainRuntimeImpl(KernelInterface kernel, Avm avm, ReentrantDAppStack.ReentrantState reentrantState, TransactionContext context, IHelper helper, TransactionResult result) {
        this.kernel = kernel;
        this.avm = avm;
        this.reentrantState = reentrantState;
        this.helper = helper;
        this.result = result;

        this.tx = context.getTransaction();
        this.block = context.getBlock();
    }

    @Override
    public Address avm_getAddress() {
        // TODO: handle CREATE transaction
        return new Address(tx.getTo());
    }

    @Override
    public Address avm_getSender() {
        return new Address(tx.getFrom());
    }

    @Override
    public Address avm_getOrigin() {
        return null;
    }

    @Override
    public long avm_getEnergyLimit() {
        return tx.getEnergyLimit();
    }

    @Override
    public BigInteger avm_getEnergyPrice() {
        return null;
    }

    @Override
    public BigInteger avm_getValue() {
        return null;
    }

    @Override
    public ByteArray avm_getData() {
        if (tx.getType() == Transaction.Type.CREATE) {
            if (Helpers.decodeCodeAndData(tx.getData())[1] == null) {
                return null;
            }
            else {
                return new ByteArray(Helpers.decodeCodeAndData(tx.getData())[1]);
            }
        }
        else {
            return new ByteArray(tx.getData());
        }
    }

    @Override
    public ByteArray avm_getStorage(ByteArray key) {
        return new ByteArray(this.kernel.getStorage(tx.getTo(), key.getUnderlying()));
    }

    @Override
    public void avm_putStorage(ByteArray key, ByteArray value) {
        this.kernel.putStorage(tx.getTo(), key.getUnderlying(), value.getUnderlying());
    }

    @Override
    public BigInteger avm_getBalance(Address address) {
        return null;
    }

    @Override
    public int avm_getCodeSize(Address address) {
        return 0;
    }


    @Override
    public long avm_getBlockTimestamp() {
        return block.getTimestamp();
    }

    @Override
    public long avm_getBlockNumber() {
        return block.getNumber();
    }

    @Override
    public long avm_getBlockEnergyLimit() {
        return 0;
    }

    @Override
    public Address avm_getBlockCoinbase() {
        return null;
    }

    @Override
    public ByteArray avm_getBlockPreviousHash() {
        return null;
    }

    @Override
    public BigInteger avm_getBlockDifficulty() {
        return null;
    }


    @Override
    public long avm_getRemainingEnergy() {
        return 0;
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
                tx.getTo(),
                targetAddress.unwrap(),
                value,
                data.getUnderlying(),
                energyLimit,
                tx);
        result.addInternalTransaction(internalTx);

        // execute the internal transaction
        TransactionResult newResult = this.avm.run(new TransactionContextImpl(internalTx, this.block));

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
        this.kernel.deleteAccount(tx.getTo());
    }

    @Override
    public void avm_log(ByteArray data) {
        Log log = new Log(tx.getTo(), List.of(), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray data) {
        Log log = new Log(tx.getTo(), List.of(topic1.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray data) {
        Log log = new Log(tx.getTo(), List.of(topic1.getUnderlying(), topic2.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray data) {
        Log log = new Log(tx.getTo(), List.of(topic1.getUnderlying(), topic2.getUnderlying(), topic3.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public void avm_log(ByteArray topic1, ByteArray topic2, ByteArray topic3, ByteArray topic4, ByteArray data) {
        Log log = new Log(tx.getTo(), List.of(topic1.getUnderlying(), topic2.getUnderlying(), topic3.getUnderlying(), topic4.getUnderlying()), data.getUnderlying());
        result.addLog(log);
    }

    @Override
    public ByteArray avm_blake2b(ByteArray data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data can't be NULL");
        }

        // TODO: implement blake2b

        byte[] result = new byte[32];
        System.arraycopy(data.getUnderlying(),0, result, 0, Math.min(result.length, data.length()));

        return new ByteArray(result);
    }
}

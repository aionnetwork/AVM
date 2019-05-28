package org.aion.avm.tooling.blockchainruntime;

import avm.Address;
import org.aion.avm.RuntimeMethodFeeSchedule;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

public class StorageEnergyTest {

    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private Address from = avmRule.getPreminedAccount();
    private long energyLimit = 2_000_000L;
    private long energyPrice = 1;
    private Address dappAddr;

    @Before
    public void setup() {
        dappAddr = deploy();
    }

    /**
     * tests setting multiple storage values in one transaction and deleting them after for a refund
     */
    @Test
    public void setAndThenDeleteStorage() {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments("putStorage");
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());

        txDataMethodArguments = ABIUtil.encodeMethodArguments("resetStorage");
        resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        Assert.assertEquals(70923 - 70923 / 2, energyLimit - resultWrapper.getTransactionResult().getEnergyRemaining());
    }

    /**
     * tests setting multiple storage values in one transaction. then deleting them and self destructing the contract after for a refund
     */
    @Test
    public void setAndThenDeleteStorageAndSelfDestruct() {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments("putStorage");
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());

        txDataMethodArguments = ABIUtil.encodeMethodArguments("resetStorageSelfDestruct");
        resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        Assert.assertEquals(77788 - 77788 / 2, energyLimit - resultWrapper.getTransactionResult().getEnergyRemaining());
    }

    /**
     * tests putting the same storage key multiple times
     */
    @Test
    public void putStorageSameKey() {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments("putStorageSameKey");
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        Assert.assertEquals(47901 + 74 * 5 +
                RuntimeMethodFeeSchedule.BlockchainRuntime_avm_setStorage +
                4 * RuntimeMethodFeeSchedule.BlockchainRuntime_avm_resetStorage + 500, energyLimit - resultWrapper.getTransactionResult().getEnergyRemaining());
    }

    /**
     * tests resetting the same storage key's value to null multiple times
     */
    @Test
    public void resetStorageSameKey() {
        //put first
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments("putStorage");
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());

        //delete values
        txDataMethodArguments = ABIUtil.encodeMethodArguments("resetStorageSameKey");
        resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        // cost before the refund is processed
        Assert.assertEquals(49891 + 29 * 5
                + 5 * RuntimeMethodFeeSchedule.BlockchainRuntime_avm_resetStorage
                - RuntimeMethodFeeSchedule.BlockchainRuntime_avm_deleteStorage_refund, energyLimit - resultWrapper.getTransactionResult().getEnergyRemaining());
    }

    /**
     * Tests setting storage values to null in a reentrant call.
     * Values have been set in a previous transaction
     */
    @Test
    public void reentrantCallSelf() {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments("putStorage");
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());

        byte[] data = ABIUtil.encodeMethodArguments("resetStorage");
        txDataMethodArguments = ABIUtil.encodeMethodArguments("reentrantCall", dappAddr, data, new byte[0]);
        resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        // cost before the refund is processed
        long executionCost = 87174 + 29 * 5 +
                5 * RuntimeMethodFeeSchedule.BlockchainRuntime_avm_resetStorage;
        Assert.assertEquals(executionCost - executionCost / 2, energyLimit - resultWrapper.getTransactionResult().getEnergyRemaining());
    }

    /**
     * tests putting values in storage and then setting storage values to null in a reentrant call
     */
    @Test
    public void reentrantCallAfterPut() {
        byte[] data = ABIUtil.encodeMethodArguments("resetStorage");
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments("reentrantCallAfterPut", dappAddr, data);
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        long costWithoutResettingStorage = 189942;
        long executionCost = costWithoutResettingStorage + 29 * 5 +
                5 * RuntimeMethodFeeSchedule.BlockchainRuntime_avm_resetStorage;
        Assert.assertEquals(executionCost - 5 * RuntimeMethodFeeSchedule.BlockchainRuntime_avm_deleteStorage_refund, energyLimit - resultWrapper.getTransactionResult().getEnergyRemaining());

        byte[] key = new byte[32];
        key[0] = 0x1;
        txDataMethodArguments = ABIUtil.encodeMethodArguments("getStorage", key);
        resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        Assert.assertArrayEquals(null, (byte[]) resultWrapper.getDecodedReturnData());
    }

    @Test
    public void putResetStorage() {
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments("checkStorage");
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
    }

    @Test
    public void reentrantReset() {
        Address newContract = deploy();
        //put storage for both contracts
        byte[] txDataMethodArguments = ABIUtil.encodeMethodArguments("putStorage");
        AvmRule.ResultWrapper resultWrapper = avmRule.call(from, newContract, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());

        txDataMethodArguments = ABIUtil.encodeMethodArguments("putStorage");
        resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());

        byte[] data = ABIUtil.encodeMethodArguments("resetStorage");
        txDataMethodArguments = ABIUtil.encodeMethodArguments("reentrantReset", newContract, data);
        resultWrapper = avmRule.call(from, dappAddr, BigInteger.ZERO, txDataMethodArguments, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getTransactionResult().getResultCode().isSuccess());
        Assert.assertEquals(140072 - 140072 / 2, energyLimit - resultWrapper.getTransactionResult().getEnergyRemaining());
    }

    @Test
    public void clinitTest() {
        byte[] jar = avmRule.getDappBytes(StorageEnergyClinitTarget.class, new byte[0]);
        AvmRule.ResultWrapper resultWrapper = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        Assert.assertEquals(269827 +
                3 * RuntimeMethodFeeSchedule.BlockchainRuntime_avm_setStorage +
                3 * RuntimeMethodFeeSchedule.BlockchainRuntime_avm_resetStorage -
                RuntimeMethodFeeSchedule.BlockchainRuntime_avm_deleteStorage_refund, energyLimit - resultWrapper.getTransactionResult().getEnergyRemaining());
    }

    private Address deploy() {
        byte[] jar = avmRule.getDappBytes(StorageEnergyTarget.class, new byte[0]);
        AvmRule.ResultWrapper resultWrapper = avmRule.deploy(from, BigInteger.ZERO, jar, energyLimit, energyPrice);
        Assert.assertTrue(resultWrapper.getReceiptStatus().isSuccess());
        return resultWrapper.getDappAddress();
    }
}

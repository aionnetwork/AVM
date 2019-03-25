package org.aion.avm.tooling.collection;

import java.math.BigInteger;

import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class AionCollectionInterfaceTest {

    @Rule
    public AvmRule avmRule = new AvmRule(false);
    private Address from = avmRule.getPreminedAccount();
    private long energyLimit = 10_000_000L;
    private long energyPrice = 1;

    private TransactionResult deploy(){
        byte[] dappBytes = avmRule.getDappBytes(AionCollectionInterfaceContract.class, new byte[0], AionList.class, AionSet.class, AionMap.class);
        TransactionResult createResult = avmRule.deploy(from,  BigInteger.ZERO, dappBytes, energyLimit, energyPrice).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return createResult;
    }

    private TransactionResult call(Address contract, byte[] args) {
        TransactionResult callResult = avmRule.call(from, contract, BigInteger.ZERO, args, energyLimit, energyPrice).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
        return callResult;
    }

    @Test
    public void testList() {
        TransactionResult deployRes = deploy();
        Address contract = new Address(deployRes.getReturnData());

        byte[] args = ABIEncoder.encodeMethodArguments("testList");
        TransactionResult testResult = call(contract, args);
	Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, testResult.getResultCode());
    }

    @Test
    public void testSet() {
        TransactionResult deployRes = deploy();
        Address contract = new Address(deployRes.getReturnData());

        byte[] args = ABIEncoder.encodeMethodArguments("testSet");
        TransactionResult testResult = call(contract, args);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, testResult.getResultCode());
    }

    @Test
    public void testMap() {
        TransactionResult deployRes = deploy();
        Address contract = new Address(deployRes.getReturnData());

        byte[] args = ABIEncoder.encodeMethodArguments("testMap");
        TransactionResult testResult = call(contract, args);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, testResult.getResultCode());
    }

}

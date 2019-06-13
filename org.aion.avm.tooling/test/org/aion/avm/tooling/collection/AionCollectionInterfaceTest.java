package org.aion.avm.tooling.collection;

import java.math.BigInteger;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class AionCollectionInterfaceTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
    private Address from = avmRule.getPreminedAccount();
    private long energyLimit = 10_000_000L;
    private long energyPrice = 1;

    private AvmTransactionResult deploy(){
        byte[] dappBytes = avmRule.getDappBytes(AionCollectionInterfaceContract.class, new byte[0], AionList.class, AionSet.class, AionMap.class);
        AvmTransactionResult createResult = avmRule.deploy(from,  BigInteger.ZERO, dappBytes, energyLimit, energyPrice).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return createResult;
    }

    private AvmTransactionResult call(Address contract, byte[] args) {
        AvmTransactionResult callResult = avmRule.call(from, contract, BigInteger.ZERO, args, energyLimit, energyPrice).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, callResult.getResultCode());
        return callResult;
    }

    @Test
    public void testList() {
        AvmTransactionResult deployRes = deploy();
        Address contract = new Address(deployRes.getReturnData());

        byte[] args = ABIUtil.encodeMethodArguments("testList");
        AvmTransactionResult testResult = call(contract, args);
	Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, testResult.getResultCode());
    }

    @Test
    public void testSet() {
        AvmTransactionResult deployRes = deploy();
        Address contract = new Address(deployRes.getReturnData());

        byte[] args = ABIUtil.encodeMethodArguments("testSet");
        AvmTransactionResult testResult = call(contract, args);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, testResult.getResultCode());
    }

    @Test
    public void testMap() {
        AvmTransactionResult deployRes = deploy();
        Address contract = new Address(deployRes.getReturnData());

        byte[] args = ABIUtil.encodeMethodArguments("testMap");
        AvmTransactionResult testResult = call(contract, args);
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, testResult.getResultCode());
    }

}

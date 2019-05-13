package org.aion.avm.tooling.shadowing.testStringBufferBuilder;

import avm.Address;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;

public class StringBuilderTest {
    @Rule
    public AvmRule avmRule = new AvmRule(true);

    private Address from = avmRule.getPreminedAccount();
    private Address dappAddr;

    private long energyLimit = 5_000_000L;
    private long energyPrice = 1;

    @Before
    public void setup() {
        byte[] txData = avmRule.getDappBytes (StringBuilderResource.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void stringBuilderInsertNull(){
        callStatic("stringBuilderInsertNull");
    }

    @Test
    public void stringBuilderAppendNull(){
        callStatic("stringBuilderAppendNull");
    }

    @Test
    public void stringBuilderAddObject(){
        callStatic("stringBuilderAddObject");
    }

    @Test
    public void stringBuilderConstructor(){
        callStatic("stringBuilderConstructor");
    }

    private void callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, data, 2_000_000, 1);
        Assert.assertTrue(result.getTransactionResult().getResultCode().isSuccess());
    }
}

package org.aion.avm.tooling.shadowing.testStringBufferBuilder;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.*;

import java.math.BigInteger;

public class StringBuilderTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address from = avmRule.getPreminedAccount();
    private static Address dappAddr;

    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;

    @BeforeClass
    public static void setup() {
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
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
    }
}

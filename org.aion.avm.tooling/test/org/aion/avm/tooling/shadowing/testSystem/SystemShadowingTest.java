package org.aion.avm.tooling.shadowing.testSystem;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.*;

import java.math.BigInteger;


public class SystemShadowingTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
    private static Address from = avmRule.getPreminedAccount();
    private static Address dappAddr;

    @BeforeClass
    public static void setup() {
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, avmRule.getDappBytes(TestResource.class, null)).getDappAddress();
    }

    @Test
    public void testArrayCopy() {
        byte[] txData = ABIUtil.encodeMethodArguments("testArrayCopy");
        Object result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayCopyNullPointerException() {
        byte[] txData = ABIUtil.encodeMethodArguments("testArrayCopyNullPointerException");
        Object result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayCopyIndexOutOfBoundsException() {
        byte[] txData = ABIUtil.encodeMethodArguments("testArrayCopyIndexOutOfBoundsException");
        Object result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }

    @Test
    public void testArrayCopyArrayStoreException() {
        byte[] txData = ABIUtil.encodeMethodArguments("testArrayCopyArrayStoreException");
        Object result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(true, result);
    }

    @Test
    public void invalidArrayCopyLength() {
        byte[] txData = ABIUtil.encodeMethodArguments("invalidArrayCopyLength");
        AvmRule.ResultWrapper result = avmRule.call(avmRule.getPreminedAccount(), dappAddr, BigInteger.ZERO, txData);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
    }
}
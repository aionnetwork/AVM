package org.aion.avm.tooling.shadowing.testPrimitive;

import avm.Address;
import java.math.BigInteger;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.aion.avm.tooling.AvmRule.ResultWrapper;
import org.junit.*;

public class PrimitiveShadowingTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(true);

    private static Address from = avmRule.getPreminedAccount();
    private static Address dappAddr;

    private static long energyLimit = 600_000_00000L;
    private static long energyPrice = 1;

    @BeforeClass
    public static void setup() {
        byte[] txData = avmRule.getDappBytes (TestResource.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testBoolean() {
        byte[] txData = ABIUtil.encodeMethodArguments("testBoolean");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testByte() {
        byte[] txData = ABIUtil.encodeMethodArguments("testByte");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testDouble() {
        byte[] txData = ABIUtil.encodeMethodArguments("testDouble");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testFloat() {
        byte[] txData = ABIUtil.encodeMethodArguments("testFloat");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testInteger() {
        byte[] txData = ABIUtil.encodeMethodArguments("testInteger");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testLong() {
        byte[] txData = ABIUtil.encodeMethodArguments("testLong");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testShort() {
        byte[] txData = ABIUtil.encodeMethodArguments("testShort");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testCharacter() {
        byte[] txData = ABIUtil.encodeMethodArguments("testCharacter");
        ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData,
            energyLimit, energyPrice);

        Assert.assertTrue(result.getTransactionResult().getResultCode().isSuccess());
        Assert.assertEquals(true, result.getDecodedReturnData());

    }

    @Test
    public void testAutoboxing() {
        byte[] txData = ABIUtil.encodeMethodArguments("testAutoboxing");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }
}

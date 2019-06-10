package org.aion.avm.tooling.shadowing.testEnum;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.*;

import java.math.BigInteger;

public class EnumShadowingTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static Address from = avmRule.getPreminedAccount();
    private static Address dappAddr;

    private static long energyLimit = 600_000_00000L;
    private static long energyPrice = 1;

    @BeforeClass
    public static void setup() {
        byte[] txData = avmRule.getDappBytes (TestResource.class, null, TestEnum.class);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testEnumAccess() {
        byte[] txData = ABIUtil.encodeMethodArguments("testEnumAccess");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testEnumValues() {
        byte[] txData = ABIUtil.encodeMethodArguments("testEnumValues");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testShadowJDKEnum() {
        byte[] txData = ABIUtil.encodeMethodArguments("testShadowJDKEnum");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testTimeUnitEnum() {
        byte[] txData = ABIUtil.encodeMethodArguments("testTimeUnitEnum");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void testInvalidRoundingModeEnum() {
        byte[] txData = ABIUtil.encodeMethodArguments("testInvalidRoundingModeEnum");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

    @Test
    public void EnumHashcode() {
        byte[] txData = ABIUtil.encodeMethodArguments("EnumHashcode");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice).getDecodedReturnData();

        Assert.assertEquals(true, result);
    }

}

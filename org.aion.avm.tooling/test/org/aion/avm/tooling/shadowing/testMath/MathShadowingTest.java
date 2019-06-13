package org.aion.avm.tooling.shadowing.testMath;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import org.junit.*;

import java.math.BigInteger;


public class MathShadowingTest {
    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);
    
    private static Address from = avmRule.getPreminedAccount();
    private static Address dappAddr;

    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;


    @BeforeClass
    public static void setup() {
        byte[] txData = avmRule.getDappBytes (TestResource.class, null);
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, txData, energyLimit, energyPrice).getDappAddress();
    }

    @Test
    public void testMaxMin() {
        AvmRule.ResultWrapper result = callStatic("testMaxMin");
        Assert.assertEquals(true, result.getDecodedReturnData());
    }

    /**
     * Creates a basic MathContext, just to prove that it is correctly formed.
     */
    @Test
    public void createSimpleContext() {
        AvmRule.ResultWrapper result = callStatic("testMathContext");
        Assert.assertEquals(5, result.getDecodedReturnData());
    }

    @Test
    public void getRoundingMode() {
        callStatic("getRoundingMode");
    }

    @Test
    public void testCosh() {
        callStatic("testCosh");
    }

    @Test
    public void testSinh() {
        callStatic("testSinh");
    }

    @Test
    public void testPow2() {
        callStatic("testPow2");
    }

    @Test
    public void testAtan() {
        callStatic("testAtan");
    }

    @Test
    public void testTan() {
        callStatic("testTan");
    }

    @Test
    public void testAcos() {
        callStatic("testAcos");
    }

    @Test
    public void testCos() {
        callStatic("testCos");
    }

    @Test
    public void testAsin() {
        callStatic("testCos");
    }

    @Test
    public void testSin() {
        callStatic("testSin");
    }

    @Test
    public void testLog() {
        callStatic("testLog");
    }

    @Test
    public void testExp() {
        callStatic("testExp");
    }

    private AvmRule.ResultWrapper callStatic(String methodName){
        byte[] txData = ABIUtil.encodeMethodArguments(methodName);
        AvmRule.ResultWrapper result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData, energyLimit, energyPrice);
        Assert.assertTrue(result.getTransactionResult().getResultCode().isSuccess());
        return result;
    }
}

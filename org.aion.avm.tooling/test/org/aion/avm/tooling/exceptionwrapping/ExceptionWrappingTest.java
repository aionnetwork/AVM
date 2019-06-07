package org.aion.avm.tooling.exceptionwrapping;

import avm.Address;

import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.tooling.AvmRule;
import i.PackageConstants;
import org.aion.kernel.AvmTransactionResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


public class ExceptionWrappingTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);
    private Address from = avmRule.getPreminedAccount();
    private Address dappAddr;

    @Before
    public void setup() {
        dappAddr = avmRule.deploy(from, BigInteger.ZERO, avmRule.getDappBytes(TestExceptionResource.class, null)).getDappAddress();
    }

    /**
     * Tests that a multi-catch, using only java/lang/* exception types, works correctly.
     */
    @Test
    public void testSimpleTryMultiCatchFinally() {
        byte[] txData = ABIUtil.encodeMethodArguments("tryMultiCatchFinally");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(3, result);
    }

    /**
     * Tests that a manually creating and throwing a java/lang/* exception type works correctly.
     */
    @Test
    public void testmSimpleManuallyThrowNull() {
        byte[] txData = ABIUtil.encodeMethodArguments("manuallyThrowNull");
        AvmTransactionResult result = (AvmTransactionResult) avmRule.call(from, dappAddr, BigInteger.ZERO, txData).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
        Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + PackageConstants.kShadowDotPrefix + NullPointerException.class.getName()).equals(result.getUncaughtException().getClass().getName()));
    }

    /**
     * Tests that we can correctly interact with exceptions from the java/lang/* hierarchy from within the catch block.
     */
    @Test
    public void testSimpleTryMultiCatchInteraction() {
        byte[] txData = ABIUtil.encodeMethodArguments("tryMultiCatch");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(2, result);
    }

    /**
     * Tests that we can re-throw VM-generated exceptions and re-catch them.
     */
    @Test
    public void testRecatchCoreException() {
        byte[] txData = ABIUtil.encodeMethodArguments("outerCatch");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals(2, result);
    }

    /**
     * Make sure that we are handling all these cases in the common pipeline, not just the unit test.
     */
    @Test
    public void testUserDefinedThrowCatch_commonPipeline() {
        byte[] txData = ABIUtil.encodeMethodArguments("userDefinedCatch");
        Object result = avmRule.call(from, dappAddr, BigInteger.ZERO, txData).getDecodedReturnData();
        Assert.assertEquals("two", result);
    }

    /**
     * issue-141:  The case where we see the original exception, since nobody tries to catch it.
     */
    @Test
    public void testOriginalNull_commonPipeline() {
        byte[] txData = ABIUtil.encodeMethodArguments("originalNull");
        AvmTransactionResult result = (AvmTransactionResult) avmRule.call(from, dappAddr, BigInteger.ZERO, txData).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
        Assert.assertTrue(NullPointerException.class.getName().equals(result.getUncaughtException().getClass().getName()));
    }

    /**
     * issue-141:  The case where we see the remapped exception, since the user caught and re-threw it.
     */
    @Test
    public void testInnerCatch_commonPipeline() {
        byte[] txData = ABIUtil.encodeMethodArguments("innerCatch");
        AvmTransactionResult result = (AvmTransactionResult) avmRule.call(from, dappAddr, BigInteger.ZERO, txData).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
        Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + PackageConstants.kShadowDotPrefix + NullPointerException.class.getName()).equals(result.getUncaughtException().getClass().getName()));
    }

    @Test
    public void testTryCatchLoop() {
        byte[] txData = ABIUtil.encodeMethodArguments("tryCatchLoop", 20000);
        AvmTransactionResult result = (AvmTransactionResult) avmRule.call(from, dappAddr, BigInteger.ZERO, txData).getTransactionResult();
        Assert.assertTrue(result.getResultCode().isSuccess());
        System.out.println(2_000_000 - result.getEnergyRemaining());
    }
}

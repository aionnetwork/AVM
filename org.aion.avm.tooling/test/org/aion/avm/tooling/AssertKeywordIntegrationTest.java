package org.aion.avm.tooling;

import org.aion.avm.userlib.abi.ABIDecoder;

import avm.Address;
import org.aion.kernel.AvmTransactionResult;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Tests how we handle the "assert" keyword in Java source.
 * Currently, we handle this as though assertions were always enabled.
 */
public class AssertKeywordIntegrationTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;
    // This constant must be whatever is returned by Class#avm_desiredAssertionStatus().
    private static final boolean ASSERTIONS_ENABLED = true;


    @Test
    public void testEmptyPass() throws Exception {
        Address dapp = installTestDApp(AssertKeywordIntegrationTestTarget.class);
        
        // Do the call.
        int length = callStaticInteger(dapp, "runEmptyCheck");
        // -1 is pass.
        Assert.assertEquals(-1, length);
        // Make sure that we checked (same as Class#avm_desiredAssertionStatus()).
        Assert.assertEquals(ASSERTIONS_ENABLED, callStaticBoolean(dapp, "getAndClearState"));
    }

    @Test
    public void testArgPass() throws Exception {
        Address dapp = installTestDApp(AssertKeywordIntegrationTestTarget.class);
        
        // Do the call.
        int length = callStaticInteger(dapp, "runIntCheck", 5);
        // -1 is pass.
        Assert.assertEquals(-1, length);
        // Make sure that we checked (same as Class#avm_desiredAssertionStatus()).
        Assert.assertEquals(ASSERTIONS_ENABLED, callStaticBoolean(dapp, "getAndClearState"));
    }

    @Test
    public void testEmptyFail() throws Exception {
        Address dapp = installTestDApp(AssertKeywordIntegrationTestTarget.class);
        
        // Setup failure.
        boolean wasFail = callStaticBoolean(dapp, "setShouldFail", true);
        Assert.assertFalse(wasFail);
        
        // Do the call.
        int length = callStaticInteger(dapp, "runEmptyCheck");
        // Empty cause so this is a 0-length message.
        Assert.assertEquals(0, length);
        // Make sure that we checked (same as Class#avm_desiredAssertionStatus()).
        Assert.assertEquals(ASSERTIONS_ENABLED, callStaticBoolean(dapp, "getAndClearState"));
    }

    @Test
    public void testArgFail() throws Exception {
        Address dapp = installTestDApp(AssertKeywordIntegrationTestTarget.class);
        
        // Setup failure.
        boolean wasFail = callStaticBoolean(dapp, "setShouldFail", true);
        Assert.assertFalse(wasFail);
        
        // Do the call.
        int length = callStaticInteger(dapp, "runIntCheck", 5);
        // The cause will be the string of "5", so 1.
        Assert.assertEquals(1, length);
        // Make sure that we checked (same as Class#avm_desiredAssertionStatus()).
        Assert.assertEquals(ASSERTIONS_ENABLED, callStaticBoolean(dapp, "getAndClearState"));
    }


    private Address installTestDApp(Class<?> testClass) {
        byte[] txData = avmRule.getDappBytes(testClass, new byte[0]);
        
        // Deploy.
        TransactionResult createResult = avmRule.deploy(avmRule.getPreminedAccount(), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return new Address(createResult.getReturnData());
    }

    private int callStaticInteger(Address dapp, String methodName, Object... arguments) {
        byte[] result = callStaticResult(dapp, methodName, arguments);
        return new ABIDecoder(result).decodeOneInteger();
    }

    private boolean callStaticBoolean(Address dapp, String methodName, Object... arguments) {
        byte[] result = callStaticResult(dapp, methodName, arguments);
        return new ABIDecoder(result).decodeOneBoolean();
    }

    private byte[] callStaticResult(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, arguments);
        TransactionResult result = avmRule.call(avmRule.getPreminedAccount(), dapp, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return result.getReturnData();
    }
}

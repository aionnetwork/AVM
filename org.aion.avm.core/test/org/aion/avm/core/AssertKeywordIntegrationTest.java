package org.aion.avm.core;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.util.AvmRule;
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
        int length = ((Integer)callStatic(dapp, "runEmptyCheck")).intValue();
        // -1 is pass.
        Assert.assertEquals(-1, length);
        // Make sure that we checked (same as Class#avm_desiredAssertionStatus()).
        Assert.assertEquals(ASSERTIONS_ENABLED, ((Boolean)callStatic(dapp, "getAndClearState")).booleanValue());
    }

    @Test
    public void testArgPass() throws Exception {
        Address dapp = installTestDApp(AssertKeywordIntegrationTestTarget.class);
        
        // Do the call.
        int length = ((Integer)callStatic(dapp, "runIntCheck", 5)).intValue();
        // -1 is pass.
        Assert.assertEquals(-1, length);
        // Make sure that we checked (same as Class#avm_desiredAssertionStatus()).
        Assert.assertEquals(ASSERTIONS_ENABLED, ((Boolean)callStatic(dapp, "getAndClearState")).booleanValue());
    }

    @Test
    public void testEmptyFail() throws Exception {
        Address dapp = installTestDApp(AssertKeywordIntegrationTestTarget.class);
        
        // Setup failure.
        boolean wasFail = ((Boolean)callStatic(dapp, "setShouldFail", true)).booleanValue();
        Assert.assertFalse(wasFail);
        
        // Do the call.
        int length = ((Integer)callStatic(dapp, "runEmptyCheck")).intValue();
        // Empty cause so this is a 0-length message.
        Assert.assertEquals(0, length);
        // Make sure that we checked (same as Class#avm_desiredAssertionStatus()).
        Assert.assertEquals(ASSERTIONS_ENABLED, ((Boolean)callStatic(dapp, "getAndClearState")).booleanValue());
    }

    @Test
    public void testArgFail() throws Exception {
        Address dapp = installTestDApp(AssertKeywordIntegrationTestTarget.class);
        
        // Setup failure.
        boolean wasFail = ((Boolean)callStatic(dapp, "setShouldFail", true)).booleanValue();
        Assert.assertFalse(wasFail);
        
        // Do the call.
        int length = ((Integer)callStatic(dapp, "runIntCheck", 5)).intValue();
        // The cause will be the string of "5", so 1.
        Assert.assertEquals(1, length);
        // Make sure that we checked (same as Class#avm_desiredAssertionStatus()).
        Assert.assertEquals(ASSERTIONS_ENABLED, ((Boolean)callStatic(dapp, "getAndClearState")).booleanValue());
    }


    private Address installTestDApp(Class<?> testClass) {
        byte[] txData = avmRule.getDappBytes(testClass, new byte[0]);
        
        // Deploy.
        TransactionResult createResult = avmRule.deploy(avmRule.getPreminedAccount(), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return new Address(createResult.getReturnData());
    }

    private Object callStatic(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, arguments);
        TransactionResult result = avmRule.call(avmRule.getPreminedAccount(), dapp, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return ABIDecoder.decodeOneObject(result.getReturnData());
    }
}

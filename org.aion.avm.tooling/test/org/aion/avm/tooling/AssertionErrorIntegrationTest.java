package org.aion.avm.tooling;

import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.kernel.AvmTransactionResult;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Tests how we handle AssertionError's special constructors.
 */
public class AssertionErrorIntegrationTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    @Test
    public void testEmpty() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationTestTarget.class);
        
        // Do the call.
        String result = callStaticString(dapp, "emptyError");
        Assert.assertEquals(null, result);
    }

    @Test
    public void testThrowable() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationTestTarget.class);
        
        // Do the call.
        String result = callStaticString(dapp, "throwableError");
        Assert.assertEquals(AssertionError.class.getName(), result);
    }

    @Test
    public void testBool() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationTestTarget.class);
        
        // Do the call.
        String result = callStaticString(dapp, "boolError", true);
        Assert.assertEquals("true", result);
    }

    @Test
    public void testChar() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationTestTarget.class);
        
        // Do the call.
        String result = callStaticString(dapp, "charError", 'a');
        Assert.assertEquals("a", result);
    }

    @Test
    public void testInt() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationTestTarget.class);
        
        // Do the call.
        String result = callStaticString(dapp, "intError", 5);
        Assert.assertEquals("5", result);
    }

    @Test
    public void testLong() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationTestTarget.class);
        
        // Do the call.
        String result = callStaticString(dapp, "longError", 5L);
        Assert.assertEquals("5", result);
    }

    @Test
    public void testFloat() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationTestTarget.class);
        
        // Do the call.
        String result = callStaticString(dapp, "floatError", 5.0f);
        Assert.assertEquals("5.0", result);
    }

    @Test
    public void testDouble() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationTestTarget.class);
        
        // Do the call.
        String result = callStaticString(dapp, "doubleError", 5.0d);
        Assert.assertEquals("5.0", result);
    }

    @Test
    public void testNormal() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationTestTarget.class);
        
        // Do the call.
        String result = callStaticString(dapp, "normalError", new String("test").getBytes());
        Assert.assertEquals("test", result);
    }


    private Address installTestDApp(Class<?> testClass) {
        byte[] txData = avmRule.getDappBytes(testClass, new byte[0]);
        
        // Deploy.
        TransactionResult createResult = avmRule.deploy(avmRule.getPreminedAccount(), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return new Address(createResult.getReturnData());
    }

    private String callStaticString(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName, arguments);
        TransactionResult result = avmRule.call(avmRule.getPreminedAccount(), dapp, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        byte[] resultData = result.getReturnData();
        if(null == resultData) {
            return null;
        }
        byte[] utf8 = (byte[])ABIDecoder.decodeOneObject(result.getReturnData());
        return (null != utf8)
                ? new String(utf8)
                : null;
    }
}

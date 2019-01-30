package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.avm.core.util.AvmRule;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.AvmTransactionResult;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.*;


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
        Assert.assertEquals(null, result);
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
        TransactionResult createResult = avmRule.deploy(KernelInterfaceImpl.PREMINED_ADDRESS, BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return TestingHelper.buildAddress(createResult.getReturnData());
    }

    private String callStaticString(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, arguments);
        TransactionResult result = avmRule.call(KernelInterfaceImpl.PREMINED_ADDRESS, AvmAddress.wrap(dapp.unwrap()), BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        byte[] utf8 = (byte[])TestingHelper.decodeResult(result);
        return (null != utf8)
                ? new String(utf8)
                : null;
    }
}

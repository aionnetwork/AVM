package org.aion.avm.tooling;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Tests how we handle AssertionError's special constructors.
 */
public class AssertionErrorIntegrationNoMainTest {
    @Rule
    public AvmRule avmRule = new AvmRule(false);

    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    @Test
    public void testEmpty() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationNoMainTestTarget.class);

        // Do the call.
        String result = callStaticString(dapp, "emptyError", Code.FAILED_EXCEPTION);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testThrowable() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationNoMainTestTarget.class);

        // Do the call.
        String result = callStaticString(dapp, "throwableError", Code.FAILED_EXCEPTION);
        Assert.assertEquals(null, result);
    }

    @Test
    public void testBool() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationNoMainTestTarget.class);

        // Do the call.
        String result = callStaticString(dapp, "boolError", Code.SUCCESS,true);
        Assert.assertEquals("true", result);
    }

    @Test
    public void testChar() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationNoMainTestTarget.class);

        // Do the call.
        String result = callStaticString(dapp, "charError", Code.SUCCESS,'a');
        Assert.assertEquals("a", result);
    }

    @Test
    public void testInt() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationNoMainTestTarget.class);

        // Do the call.
        String result = callStaticString(dapp, "intError", Code.SUCCESS, 5);
        Assert.assertEquals("5", result);
    }

    @Test
    public void testLong() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationNoMainTestTarget.class);

        // Do the call.
        String result = callStaticString(dapp, "longError", Code.SUCCESS, 5L);
        Assert.assertEquals("5", result);
    }

    @Test
    public void testFloat() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationNoMainTestTarget.class);

        // Do the call.
        String result = callStaticString(dapp, "floatError", Code.SUCCESS,5.0f);
        Assert.assertEquals("5.0", result);
    }

    @Test
    public void testDouble() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationNoMainTestTarget.class);

        // Do the call.
        String result = callStaticString(dapp, "doubleError",Code.SUCCESS,5.0d);
        Assert.assertEquals("5.0", result);
    }

    @Test
    public void testNormal() throws Exception {
        Address dapp = installTestDApp(AssertionErrorIntegrationNoMainTestTarget.class);

        // Do the call.
        String result = callStaticString(dapp, "normalError",Code.SUCCESS,
            new String("test").getBytes());
        Assert.assertEquals("test", result);
    }


    private Address installTestDApp(Class<?> testClass) {
        byte[] txData = avmRule.getDappBytes(testClass, new byte[0]);

        // Deploy.
        TransactionResult createResult = avmRule.deploy(avmRule.getPreminedAccount(), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return new Address(createResult.getReturnData());
    }

    private String callStaticString(Address dapp, String methodName,
        AvmTransactionResult.Code expectedCode, Object... arguments) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, arguments);
        TransactionResult result = avmRule.call(avmRule.getPreminedAccount(), dapp, BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE).getTransactionResult();
        Assert.assertEquals(expectedCode, result.getResultCode());
        if(result.getReturnData() != null) {
            byte[] utf8 = (byte[]) ABIDecoder.decodeOneObject(result.getReturnData());
            return (null != utf8)
                ? new String(utf8)
                : null;
        } else {
            return null;
        }
    }
}

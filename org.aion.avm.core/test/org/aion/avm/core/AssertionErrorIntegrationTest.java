package org.aion.avm.core;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.internal.PackageConstants;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests how we handle AssertionError's special constructors.
 */
public class AssertionErrorIntegrationTest {
    private static final Block BLOCK = new Block(new byte[32], 1L, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;

    private KernelInterface kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = NodeEnvironment.singleton.buildAvmInstance(this.kernel);
    }

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
        Assert.assertEquals(PackageConstants.kShadowDotPrefix + "java.lang.AssertionError: null", result);
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
        byte[] jar = JarBuilder.buildJarForMainAndClasses(testClass);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        Transaction create = new Transaction(Transaction.Type.CREATE, KernelInterfaceImpl.PREMINED_ADDRESS, null, this.kernel.getNonce(KernelInterfaceImpl.PREMINED_ADDRESS), 0, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult createResult = this.avm.run(new TransactionContextImpl(create, BLOCK));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        return TestingHelper.buildAddress(createResult.getReturnData());
    }

    private String callStaticString(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, arguments);
        Transaction call = new Transaction(Transaction.Type.CALL, KernelInterfaceImpl.PREMINED_ADDRESS, dapp.unwrap(), this.kernel.getNonce(KernelInterfaceImpl.PREMINED_ADDRESS), 0, argData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result = this.avm.run(new TransactionContextImpl(call, BLOCK));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        byte[] utf8 = (byte[])TestingHelper.decodeResult(result);
        return (null != utf8)
                ? new String(utf8)
                : null;
    }
}

package org.aion.avm.core;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterface;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests how we handle the "assert" keyword in Java source.
 * Currently, we handle this as though assertions were always enabled.
 */
public class AssertKeywordIntegrationTest {
    private static final Block BLOCK = new Block(new byte[32], 1L, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;
    // This constant must be whatever is returned by Class#avm_desiredAssertionStatus().
    private static final boolean ASSERTIONS_ENABLED = true;

    private KernelInterface kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = NodeEnvironment.singleton.buildAvmInstance(this.kernel);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

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
        byte[] jar = JarBuilder.buildJarForMainAndClasses(testClass);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        
        // Deploy.
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, this.kernel.getNonce(KernelInterfaceImpl.PREMINED_ADDRESS), 0L, txData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult createResult = this.avm.run(new TransactionContextImpl(create, BLOCK));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, createResult.getStatusCode());
        return TestingHelper.buildAddress(createResult.getReturnData());
    }

    private Object callStatic(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, arguments);
        Transaction call = Transaction.call(KernelInterfaceImpl.PREMINED_ADDRESS, dapp.unwrap(), this.kernel.getNonce(KernelInterfaceImpl.PREMINED_ADDRESS), 0L, argData, ENERGY_LIMIT, ENERGY_PRICE);
        TransactionResult result = this.avm.run(new TransactionContextImpl(call, BLOCK));
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        return TestingHelper.decodeResult(result);
    }
}

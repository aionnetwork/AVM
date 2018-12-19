package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests how we handle the "assert" keyword in Java source.
 * Currently, we handle this as though assertions were always enabled.
 */
public class AssertKeywordIntegrationTest {
    private static final Block BLOCK = new Block(new byte[32], 1L, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private static final long ENERGY_LIMIT = 10_000_000L;
    private static final long ENERGY_PRICE = 1L;
    // This constant must be whatever is returned by Class#avm_desiredAssertionStatus().
    private static final boolean ASSERTIONS_ENABLED = true;

    private KernelInterface kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
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
        Transaction create = Transaction.create(KernelInterfaceImpl.PREMINED_ADDRESS, this.kernel.getNonce(KernelInterfaceImpl.PREMINED_ADDRESS).longValue(), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult createResult = this.avm.run(new TransactionContext[] {new TransactionContextImpl(create, BLOCK)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
        return TestingHelper.buildAddress(createResult.getReturnData());
    }

    private Object callStatic(Address dapp, String methodName, Object... arguments) {
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName, arguments);
        Transaction call = Transaction.call(KernelInterfaceImpl.PREMINED_ADDRESS, AvmAddress.wrap(dapp.unwrap()), this.kernel.getNonce(KernelInterfaceImpl.PREMINED_ADDRESS).longValue(), BigInteger.ZERO, argData, ENERGY_LIMIT, ENERGY_PRICE);
        AvmTransactionResult result = this.avm.run(new TransactionContext[] {new TransactionContextImpl(call, BLOCK)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
        return TestingHelper.decodeResult(result);
    }
}

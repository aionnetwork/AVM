package org.aion.avm.core.exceptionwrapping;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.TestingHelper;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.internal.PackageConstants;
import org.aion.kernel.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.aion.avm.core.util.Helpers.randomBytes;


public class ExceptionWrappingTest {
    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddr;

    private Block block = new Block(new byte[32], 1, randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 6_000_0000;
    private long energyPrice = 1;

    private KernelInterfaceImpl kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = NodeEnvironment.singleton.buildAvmInstance(this.kernel);
        
        // Deploy the Dapp
        byte[] testExceptionJar = JarBuilder.buildJarForMainAndClasses(TestExceptionResource.class);

        byte[] txData = new CodeAndArguments(testExceptionJar, null).encodeToBytes();
        Transaction tx = Transaction.create(from, kernel.getNonce(from), 0L, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = avm.run(context).getReturnData();
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    /**
     * Tests that a multi-catch, using only java/lang/* exception types, works correctly.
     */
    @Test
    public void testSimpleTryMultiCatchFinally() {
        byte[] txData = ABIEncoder.encodeMethodArguments("tryMultiCatchFinally");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult result = avm.run(context);
        Assert.assertEquals(3, TestingHelper.decodeResult(result));
    }

    /**
     * Tests that a manually creating and throwing a java/lang/* exception type works correctly.
     */
    @Test
    public void testmSimpleManuallyThrowNull() {
        byte[] txData = ABIEncoder.encodeMethodArguments("manuallyThrowNull");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult result = avm.run(context);
        Assert.assertEquals(TransactionResult.Code.FAILED_EXCEPTION, result.getStatusCode());
        Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + NullPointerException.class.getName()).equals(result.getUncaughtException().getClass().getName()));
    }

    /**
     * Tests that we can correctly interact with exceptions from the java/lang/* hierarchy from within the catch block.
     */
    @Test
    public void testSimpleTryMultiCatchInteraction() {
        byte[] txData = ABIEncoder.encodeMethodArguments("tryMultiCatch");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult result = avm.run(context);
        Assert.assertEquals(2, TestingHelper.decodeResult(result));
    }

    /**
     * Tests that we can re-throw VM-generated exceptions and re-catch them.
     */
    @Test
    public void testRecatchCoreException() {
        byte[] txData = ABIEncoder.encodeMethodArguments("outerCatch");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult result = avm.run(context);
        Assert.assertEquals(2, TestingHelper.decodeResult(result));
    }

    /**
     * Make sure that we are handling all these cases in the common pipeline, not just the unit test.
     */
    @Test
    public void testUserDefinedThrowCatch_commonPipeline() {
        byte[] txData = ABIEncoder.encodeMethodArguments("userDefinedCatch");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult result = avm.run(context);
        Assert.assertEquals("two", TestingHelper.decodeResult(result));
    }

    /**
     * issue-141:  The case where we see the original exception, since nobody tries to catch it.
     */
    @Test
    public void testOriginalNull_commonPipeline() {
        byte[] txData = ABIEncoder.encodeMethodArguments("originalNull");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult result = avm.run(context);
        Assert.assertEquals(TransactionResult.Code.FAILED_EXCEPTION, result.getStatusCode());
        Assert.assertTrue(NullPointerException.class.getName().equals(result.getUncaughtException().getClass().getName()));
    }

    /**
     * issue-141:  The case where we see the remapped exception, since the user caught and re-threw it.
     */
    @Test
    public void testInnerCatch_commonPipeline() {
        byte[] txData = ABIEncoder.encodeMethodArguments("innerCatch");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), 0, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        TransactionResult result = avm.run(context);
        Assert.assertEquals(TransactionResult.Code.FAILED_EXCEPTION, result.getStatusCode());
        Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + NullPointerException.class.getName()).equals(result.getUncaughtException().getClass().getName()));
    }
}

package org.aion.avm.core.exceptionwrapping;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.internal.PackageConstants;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.TransactionContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.aion.avm.core.util.Helpers.randomAddress;


public class ExceptionWrappingTest {
    private org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private org.aion.vm.api.interfaces.Address dappAddr;

    private Block block = new Block(new byte[32], 1, randomAddress(), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 6_000_0000;
    private long energyPrice = 1;

    private KernelInterfaceImpl kernel;
    private Avm avm;

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        
        // Deploy the Dapp
        byte[] testExceptionJar = JarBuilder.buildJarForMainAndClasses(TestExceptionResource.class);

        byte[] txData = new CodeAndArguments(testExceptionJar, null).encodeToBytes();
        Transaction tx = Transaction.create(from, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = AvmAddress.wrap(avm.run(new TransactionContext[] {context})[0].get().getReturnData());
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
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        AvmTransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals(3, TestingHelper.decodeResult(result));
    }

    /**
     * Tests that a manually creating and throwing a java/lang/* exception type works correctly.
     */
    @Test
    public void testmSimpleManuallyThrowNull() {
        byte[] txData = ABIEncoder.encodeMethodArguments("manuallyThrowNull");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        AvmTransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
        Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + NullPointerException.class.getName()).equals(result.getUncaughtException().getClass().getName()));
    }

    /**
     * Tests that we can correctly interact with exceptions from the java/lang/* hierarchy from within the catch block.
     */
    @Test
    public void testSimpleTryMultiCatchInteraction() {
        byte[] txData = ABIEncoder.encodeMethodArguments("tryMultiCatch");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        AvmTransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals(2, TestingHelper.decodeResult(result));
    }

    /**
     * Tests that we can re-throw VM-generated exceptions and re-catch them.
     */
    @Test
    public void testRecatchCoreException() {
        byte[] txData = ABIEncoder.encodeMethodArguments("outerCatch");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        AvmTransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals(2, TestingHelper.decodeResult(result));
    }

    /**
     * Make sure that we are handling all these cases in the common pipeline, not just the unit test.
     */
    @Test
    public void testUserDefinedThrowCatch_commonPipeline() {
        byte[] txData = ABIEncoder.encodeMethodArguments("userDefinedCatch");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        AvmTransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals("two", TestingHelper.decodeResult(result));
    }

    /**
     * issue-141:  The case where we see the original exception, since nobody tries to catch it.
     */
    @Test
    public void testOriginalNull_commonPipeline() {
        byte[] txData = ABIEncoder.encodeMethodArguments("originalNull");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        AvmTransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
        Assert.assertTrue(NullPointerException.class.getName().equals(result.getUncaughtException().getClass().getName()));
    }

    /**
     * issue-141:  The case where we see the remapped exception, since the user caught and re-threw it.
     */
    @Test
    public void testInnerCatch_commonPipeline() {
        byte[] txData = ABIEncoder.encodeMethodArguments("innerCatch");
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);

        AvmTransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, result.getResultCode());
        Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + NullPointerException.class.getName()).equals(result.getUncaughtException().getClass().getName()));
    }
}

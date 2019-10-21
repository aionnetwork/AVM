package org.aion.avm.embed.exceptionwrapping;

import i.PackageConstants;
import org.aion.avm.core.*;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.StandardCapabilities;
import org.aion.avm.tooling.ABIUtil;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.*;

import java.math.BigInteger;


public class ExceptionWrappingTest {
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static long energyLimit = 10_000_000L;
    private static long energyPrice = 1L;
    private static TestingState kernel;
    private static AvmImpl avm;
    private static AionAddress dappAddress;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());

        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(TestExceptionResource.class);
        byte[] arguments = null;
        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        dappAddress = new AionAddress(txResult.copyOfTransactionOutput().orElseThrow());
    }

    /**
     * Tests that a multi-catch, using only java/lang/* exception types, works correctly.
     */
    @Test
    public void testSimpleTryMultiCatchFinally() {
        byte[] txData = ABIUtil.encodeMethodArguments("tryMultiCatchFinally");
        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);

        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        Object decodedOutput = ABIUtil.decodeOneObject(result.copyOfTransactionOutput().orElseThrow());
        Assert.assertEquals(3, decodedOutput);
    }

    /**
     * Tests that a manually creating and throwing a java/lang/* exception type works correctly.
     */
    @Test
    public void testmSimpleManuallyThrowNull() {
        byte[] txData = ABIUtil.encodeMethodArguments("manuallyThrowNull");
        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);

        FutureResult future = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0];
        Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, future.getResult().transactionStatus.causeOfError);
        Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + PackageConstants.kShadowDotPrefix + NullPointerException.class.getName()).equals(future.getException().getClass().getName()));
    }

    /**
     * Tests that we can correctly interact with exceptions from the java/lang/* hierarchy from within the catch block.
     */
    @Test
    public void testSimpleTryMultiCatchInteraction() {
        byte[] txData = ABIUtil.encodeMethodArguments("tryMultiCatch");
        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);

        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        Object decodedOutput = ABIUtil.decodeOneObject(result.copyOfTransactionOutput().orElseThrow());
        Assert.assertEquals(2, decodedOutput);
    }

    /**
     * Tests that we can re-throw VM-generated exceptions and re-catch them.
     */
    @Test
    public void testRecatchCoreException() {
        byte[] txData = ABIUtil.encodeMethodArguments("outerCatch");
        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);

        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        Object decodedOutput = ABIUtil.decodeOneObject(result.copyOfTransactionOutput().orElseThrow());
        Assert.assertEquals(2, decodedOutput);
    }

    /**
     * Make sure that we are handling all these cases in the common pipeline, not just the unit test.
     */
    @Test
    public void testUserDefinedThrowCatch_commonPipeline() {
        byte[] txData = ABIUtil.encodeMethodArguments("userDefinedCatch");
        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);

        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        Object decodedOutput = ABIUtil.decodeOneObject(result.copyOfTransactionOutput().orElseThrow());
        Assert.assertEquals("two", decodedOutput);
    }

    /**
     * issue-141:  The case where we see the original exception, since nobody tries to catch it.
     */
    @Test
    public void testOriginalNull_commonPipeline() {
        byte[] txData = ABIUtil.encodeMethodArguments("originalNull");
        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);

        FutureResult future = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0];
        Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, future.getResult().transactionStatus.causeOfError);
        Assert.assertTrue(NullPointerException.class.getName().equals(future.getException().getClass().getName()));
    }

    /**
     * issue-141:  The case where we see the remapped exception, since the user caught and re-threw it.
     */
    @Test
    public void testInnerCatch_commonPipeline() {
        byte[] txData = ABIUtil.encodeMethodArguments("innerCatch");
        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);

        FutureResult future = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0];
        Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, future.getResult().transactionStatus.causeOfError);
        Assert.assertTrue((PackageConstants.kExceptionWrapperDotPrefix + PackageConstants.kShadowDotPrefix + NullPointerException.class.getName()).equals(future.getException().getClass().getName()));
    }

    @Test
    public void testTryCatchLoop() {
        byte[] txData = ABIUtil.encodeMethodArguments("tryCatchLoop", 20000);
        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);

        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, 0)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(1_986_373L, result.energyUsed);
    }
}

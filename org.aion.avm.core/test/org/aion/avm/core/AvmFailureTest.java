package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.InternalTransaction;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;
import org.aion.types.TransactionResult;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class AvmFailureTest {
    // transaction
    private static long energyLimit = 10_000_000L;
    private static long energyPrice = 1L;

    // kernel & vm
    private static TestingState kernel;
    private static AvmImpl avm;

    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static AionAddress dappAddress;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(AvmFailureTestResource.class);
        byte[] arguments = null;
        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();

        dappAddress = new AionAddress(txResult.copyOfTransactionOutput().orElseThrow());
        assertTrue(null != dappAddress);
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testFailedTransaction() {
        byte[] data = new ABIStreamingEncoder()
                .encodeOneString("reentrantCall")
                .encodeOneInteger(5)
                .toBytes();
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();

        assertTrue(txResult.transactionStatus.isReverted());
        assertEquals(5, txResult.internalTransactions.size());
        assertEquals(0, txResult.logs.size());

        for (InternalTransaction i : txResult.internalTransactions) {
            assertTrue(i.isRejected);
        }
    }

    @Test
    public void testOutOfEnergy() {
        byte[] data = encodeNoArgCall("testOutOfEnergy");
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();

        assertEquals(AvmInternalError.FAILED_OUT_OF_ENERGY.error, txResult.transactionStatus.causeOfError);
    }

    @Test
    public void testOutOfStack() {
        byte[] data = encodeNoArgCall("testOutOfStack");
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();

        assertEquals(AvmInternalError.FAILED_OUT_OF_STACK.error, txResult.transactionStatus.causeOfError);
    }

    @Test
    public void testRevert() {
        byte[] data = encodeNoArgCall("testRevert");
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();

        assertTrue(txResult.transactionStatus.isReverted());
        assertNotEquals(energyLimit, txResult.energyUsed);
    }

    @Test
    public void testInvalid() {
        byte[] data = encodeNoArgCall("testInvalid");
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionResult txResult = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();

        assertEquals(AvmInternalError.FAILED_INVALID.error, txResult.transactionStatus.causeOfError);
        assertEquals(energyLimit, txResult.energyUsed);
    }

    @Test
    public void testUncaughtException() {
        byte[] data = encodeNoArgCall("testUncaughtException");
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        FutureResult future = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0];
        TransactionResult txResult = future.getResult();

        assertEquals(AvmInternalError.FAILED_EXCEPTION.error, txResult.transactionStatus.causeOfError);
        assertTrue(future.getException() instanceof RuntimeException);
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidTransaction() {
        // We will encode a transaction with invalid data (null data).
        AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, null, energyLimit, energyPrice);
    }

    private static byte[] encodeNoArgCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }
}

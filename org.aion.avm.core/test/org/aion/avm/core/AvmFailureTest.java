package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.types.AionAddress;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.TestingTransaction;
import org.aion.vm.api.interfaces.InternalTransactionInterface;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class AvmFailureTest {
    // transaction
    private static long energyLimit = 10_000_000L;
    private static long energyPrice = 1L;

    // kernel & vm
    private static TestingKernel kernel;
    private static AvmImpl avm;

    private static AionAddress deployer = TestingKernel.PREMINED_ADDRESS;
    private static AionAddress dappAddress;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingKernel(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(AvmFailureTestResource.class);
        byte[] arguments = null;
        TestingTransaction tx = TestingTransaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();

        dappAddress = new AionAddress(txResult.getReturnData());
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
        TestingTransaction tx = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = (AvmTransactionResult) avm.run(kernel, new TestingTransaction[] {tx})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_REVERT, txResult.getResultCode());
        assertEquals(5, txResult.getSideEffects().getInternalTransactions().size());
        assertEquals(0, txResult.getSideEffects().getExecutionLogs().size());

        for (InternalTransactionInterface i : txResult.getSideEffects().getInternalTransactions()) {
            assertTrue(i.isRejected());
        }
    }

    @Test
    public void testOutOfEnergy() {
        byte[] data = encodeNoArgCall("testOutOfEnergy");
        TestingTransaction tx = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_OUT_OF_ENERGY, txResult.getResultCode());
    }

    @Test
    public void testOutOfStack() {
        byte[] data = encodeNoArgCall("testOutOfStack");
        TestingTransaction tx = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_OUT_OF_STACK, txResult.getResultCode());
    }

    @Test
    public void testRevert() {
        byte[] data = encodeNoArgCall("testRevert");
        TestingTransaction tx = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_REVERT, txResult.getResultCode());
        assertNotEquals(energyLimit, txResult.getEnergyUsed());
        assertNotEquals(0, txResult.getEnergyRemaining());
    }

    @Test
    public void testInvalid() {
        byte[] data = encodeNoArgCall("testInvalid");
        TestingTransaction tx = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_INVALID, txResult.getResultCode());
        assertEquals(energyLimit, txResult.getEnergyUsed());
        assertEquals(0, txResult.getEnergyRemaining());
    }

    @Test
    public void testUncaughtException() {
        byte[] data = encodeNoArgCall("testUncaughtException");
        TestingTransaction tx = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();

        assertEquals(AvmTransactionResult.Code.FAILED_EXCEPTION, txResult.getResultCode());
        assertTrue(txResult.getUncaughtException() instanceof RuntimeException);
    }

    @Test
    public void testInvalidTransaction() {
        // We will encode a transaction with invalid data (null data).
        byte[] data = null;
        TestingTransaction falingTransaction = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        boolean didFail = false;
        // This next call should fail with IllegalArgumentException so catch it.
        try {
            avm.run(kernel, new TestingTransaction[] {falingTransaction})[0].get();
        } catch (IllegalArgumentException e) {
            didFail = true;
        }
        Assert.assertTrue(didFail);
        
        // Make sure that we haven't corrupted the system and can still deploy new things
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(AvmFailureTestResource.class);
        byte[] arguments = null;
        TestingTransaction tx = TestingTransaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), energyLimit, energyPrice);
        AvmTransactionResult txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, txResult.getResultCode());
        Assert.assertNotNull(new AionAddress(txResult.getReturnData()));
    }


    private static byte[] encodeNoArgCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }
}

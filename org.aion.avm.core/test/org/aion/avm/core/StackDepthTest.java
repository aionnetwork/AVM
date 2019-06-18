package org.aion.avm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
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

public class StackDepthTest {
    private static TestingState kernel;
    private static AvmImpl avm;
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static AionAddress dappAddress;


    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);

        AvmConfiguration avmConfig = new AvmConfiguration();
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), avmConfig);

        byte[] jar = new CodeAndArguments(JarBuilder.buildJarForMainAndClassesAndUserlib(StackDepthTarget.class), null).encodeToBytes();

        Transaction tx = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, jar, 5_000_000, 1);
        TransactionResult txResult = avm.run(kernel, new Transaction[] {tx})[0].getResult();
        assertTrue(txResult.transactionStatus.isSuccess());
        dappAddress = new AionAddress(txResult.copyOfTransactionOutput().orElseThrow());
    }

    @AfterClass
    public static void teardown() {
        avm.shutdown();
    }

    @Test
    public void testDeepestValidStackDepth() {
        kernel.generateBlock();
        byte[] data = encodeCall("recurse", 511);

        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{ transaction })[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testStackOverflow() {
        kernel.generateBlock();
        byte[] data = encodeCall("recurse", 512);

        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{ transaction })[0].getResult();
        assertEquals(AvmInternalError.FAILED_OUT_OF_STACK.error, result.transactionStatus.causeOfError);
    }

    @Test
    public void testLargestValidFibonacci() {
        kernel.generateBlock();
        byte[] data = encodeCall("fibonacci", 20);

        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{ transaction })[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testExpensiveFibonacci() {
        kernel.generateBlock();
        byte[] data = encodeCall("fibonacci", 21);

        Transaction transaction = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{ transaction })[0].getResult();
        assertEquals(AvmInternalError.FAILED_OUT_OF_ENERGY.error, result.transactionStatus.causeOfError);
    }


    private static byte[] encodeCall(String methodName, int arg) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .encodeOneInteger(arg)
                .toBytes();
    }
}

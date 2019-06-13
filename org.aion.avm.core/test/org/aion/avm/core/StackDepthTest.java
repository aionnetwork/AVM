package org.aion.avm.core;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.kernel.AvmTransactionResult;
import org.aion.types.AionAddress;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.TestingTransaction;
import org.junit.*;

public class StackDepthTest {
    private static TestingKernel kernel;
    private static AvmImpl avm;
    private static AionAddress deployer = TestingKernel.PREMINED_ADDRESS;
    private static AionAddress dappAddress;


    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingKernel(block);

        AvmConfiguration avmConfig = new AvmConfiguration();
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), avmConfig);

        byte[] jar = new CodeAndArguments(JarBuilder.buildJarForMainAndClassesAndUserlib(StackDepthTarget.class), null).encodeToBytes();

        TestingTransaction tx = TestingTransaction.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, jar, 5_000_000, 1);
        AvmTransactionResult txResult = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        dappAddress = new AionAddress(txResult.getReturnData());
    }

    @AfterClass
    public static void teardown() {
        avm.shutdown();
    }

    @Test
    public void testDeepestValidStackDepth() {
        kernel.generateBlock();
        byte[] data = encodeCall("recurse", 511);

        TestingTransaction transaction = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000, 1);
        AvmTransactionResult result = avm.run(kernel, new TestingTransaction[]{ transaction })[0].get();
        assertEquals(Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testStackOverflow() {
        kernel.generateBlock();
        byte[] data = encodeCall("recurse", 512);

        TestingTransaction transaction = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000, 1);
        AvmTransactionResult result = avm.run(kernel, new TestingTransaction[]{ transaction })[0].get();
        assertEquals(Code.FAILED_OUT_OF_STACK, result.getResultCode());
    }

    @Test
    public void testLargestValidFibonacci() {
        kernel.generateBlock();
        byte[] data = encodeCall("fibonacci", 20);

        TestingTransaction transaction = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000, 1);
        AvmTransactionResult result = avm.run(kernel, new TestingTransaction[]{ transaction })[0].get();
        assertEquals(Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testExpensiveFibonacci() {
        kernel.generateBlock();
        byte[] data = encodeCall("fibonacci", 21);

        TestingTransaction transaction = TestingTransaction.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, 2_000_000, 1);
        AvmTransactionResult result = avm.run(kernel, new TestingTransaction[]{ transaction })[0].get();
        assertEquals(Code.FAILED_OUT_OF_ENERGY, result.getResultCode());
    }


    private static byte[] encodeCall(String methodName, int arg) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .encodeOneInteger(arg)
                .toBytes();
    }
}

package org.aion.avm.core.shadowing.lambdas;

import java.math.BigInteger;

import java.util.Optional;
import org.aion.avm.core.*;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.*;


public class FunctionShadowTest {
    private static final long ENERGY_LIMIT = 6_000_000L;
    private static final long ERNGY_PRICE = 1L;
    private static final AionAddress FROM = TestingState.PREMINED_ADDRESS;

    private static TestingState kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        AvmConfiguration config = new AvmConfiguration();
        config.preserveDebuggability = true;
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testNonWhitelistFunction() {
        Class<?> testClass = FunctionShadowFailSupplierResource.class;
        AionAddress dappAddr = deployTest(testClass);
        // We expect a deployment failure.
        Assert.assertNull(dappAddr);
    }

    @Test
    public void testParameterFunction() {
        Class<?> testClass = FunctionShadowFailArgsResource.class;
        AionAddress dappAddr = deployTest(testClass);
        // We expect a deployment failure.
        Assert.assertNull(dappAddr);
    }

    @Test
    public void testSafeParameterFunction() {
        Class<?> testClass = FunctionShadowPassArgsResource.class;
        AionAddress dappAddr = deployTest(testClass);
        // This case should succeed.
        Assert.assertNotNull(dappAddr);
    }

    @Test
    public void testNoComparableFunction() {
        Class<?> testClass = FunctionShadowFailComparableResource.class;
        AionAddress dappAddr = deployTest(testClass);
        // We expect a deployment failure.
        Assert.assertNull(dappAddr);
    }

    @Test
    public void testLambdaRunnable() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        AionAddress dappAddr = deployTest(testClass);
        
        // call transactions and validate the results
        oneCall(dappAddr, 0);
    }

    @Test
    public void testLambdaFunction() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        AionAddress dappAddr = deployTest(testClass);
        
        // call transactions and validate the results
        oneCall(dappAddr, 1);
    }

    @Test
    public void testSerializedLambdaRunnable() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        AionAddress dappAddr = deployTest(testClass);
        
        // Call the setup routine (2)
        oneCall(dappAddr, 2);
        
        // Call the check routine (3)
        oneCall(dappAddr, 3);
    }

    @Test
    public void testSerializedLambdaFunction() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        AionAddress dappAddr = deployTest(testClass);
        
        // Call the setup routine (4)
        oneCall(dappAddr, 4);
        
        // Call the check routine (5)
        oneCall(dappAddr, 5);
    }

    @Test
    public void testReferenceFunction() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        AionAddress dappAddr = deployTest(testClass);
        
        // call transactions and validate the results
        oneCall(dappAddr, 6);
    }

    @Test
    public void testNonSharedLambdas() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        AionAddress dappAddr = deployTest(testClass);
        
        // call transactions and validate the results
        oneCall(dappAddr, 7);
    }

    @Test
    public void testExceptionInLambdas() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        AionAddress dappAddr = deployTest(testClass);
        
        // call transactions and validate the results
        oneCall(dappAddr, 8);
    }


    private AionAddress deployTest(Class<?> testClass) {
        byte[] testJar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(testClass);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = AvmTransactionUtil.create(FROM, kernel.getNonce(FROM), BigInteger.ZERO, txData, ENERGY_LIMIT, ERNGY_PRICE);
        Optional<byte[]> optionalOutput = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult().copyOfTransactionOutput();
        return (optionalOutput.isPresent())
                ? new AionAddress(optionalOutput.get())
                : null;
    }

    private void oneCall(AionAddress dappAddr, int transactionNumber) {
        Transaction tx = AvmTransactionUtil.call(FROM, dappAddr, kernel.getNonce(FROM), BigInteger.ZERO, new byte[] {(byte)transactionNumber}, ENERGY_LIMIT, ERNGY_PRICE);
        TransactionResult result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }
}

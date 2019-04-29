package org.aion.avm.core.shadowing.lambdas;

import java.math.BigInteger;

import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class FunctionShadowTest {
    private static final long ENERGY_LIMIT = 6_000_000L;
    private static final long ERNGY_PRICE = 1L;
    private static final org.aion.types.Address FROM = TestingKernel.PREMINED_ADDRESS;

    private Block block;
    private TestingKernel kernel;
    private AvmImpl avm;

    @Before
    public void setup() {
        this.block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingKernel(this.block);
        AvmConfiguration config = new AvmConfiguration();
        config.enableVerboseContractErrors = true;
        config.preserveDebuggability = true;
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testNonWhitelistFunction() {
        Class<?> testClass = FunctionShadowFailSupplierResource.class;
        org.aion.types.Address dappAddr = deployTest(testClass);
        // We expect a deployment failure.
        Assert.assertNull(dappAddr);
    }

    @Test
    public void testParameterFunction() {
        Class<?> testClass = FunctionShadowFailArgsResource.class;
        org.aion.types.Address dappAddr = deployTest(testClass);
        // We expect a deployment failure.
        Assert.assertNull(dappAddr);
    }

    @Test
    public void testSafeParameterFunction() {
        Class<?> testClass = FunctionShadowPassArgsResource.class;
        org.aion.types.Address dappAddr = deployTest(testClass);
        // This case should succeed.
        Assert.assertNotNull(dappAddr);
    }

    @Test
    public void testNoComparableFunction() {
        Class<?> testClass = FunctionShadowFailComparableResource.class;
        org.aion.types.Address dappAddr = deployTest(testClass);
        // We expect a deployment failure.
        Assert.assertNull(dappAddr);
    }

    @Test
    public void testLambdaRunnable() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        org.aion.types.Address dappAddr = deployTest(testClass);
        
        // call transactions and validate the results
        oneCall(dappAddr, 0);
    }

    @Test
    public void testLambdaFunction() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        org.aion.types.Address dappAddr = deployTest(testClass);
        
        // call transactions and validate the results
        oneCall(dappAddr, 1);
    }

    @Test
    public void testSerializedLambdaRunnable() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        org.aion.types.Address dappAddr = deployTest(testClass);
        
        // Call the setup routine (2)
        Transaction tx = Transaction.call(FROM, dappAddr, this.kernel.getNonce(FROM), BigInteger.ZERO, new byte[] {2}, ENERGY_LIMIT, ERNGY_PRICE);
        AvmTransactionResult result = (AvmTransactionResult) this.avm.run(this.kernel, new Transaction[] {tx})[0].get();
        // TODO(AKI-131): This exception will be caused by the attempt to serialize, which will pass once the serialized form is introduced.
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_OUT_OF_ENERGY, result.getResultCode());
    }

    @Test
    public void testSerializedLambdaFunction() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        org.aion.types.Address dappAddr = deployTest(testClass);
        
        // Call the setup routine (4)
        Transaction tx = Transaction.call(FROM, dappAddr, this.kernel.getNonce(FROM), BigInteger.ZERO, new byte[] {4}, ENERGY_LIMIT, ERNGY_PRICE);
        AvmTransactionResult result = (AvmTransactionResult) this.avm.run(this.kernel, new Transaction[] {tx})[0].get();
        // TODO(AKI-131): This exception will be caused by the attempt to serialize, which will pass once the serialized form is introduced.
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_OUT_OF_ENERGY, result.getResultCode());
    }

    @Test
    public void testReferenceFunction() {
        // deploy it
        Class<?> testClass = FunctionShadowResource.class;
        org.aion.types.Address dappAddr = deployTest(testClass);
        
        // call transactions and validate the results
        oneCall(dappAddr, 6);
    }


    private org.aion.types.Address deployTest(Class<?> testClass) {
        byte[] testJar = JarBuilder.buildJarForMainAndClassesAndUserlib(testClass);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = Transaction.create(FROM, this.kernel.getNonce(FROM), BigInteger.ZERO, txData, ENERGY_LIMIT, ERNGY_PRICE);
        byte[] returnData = this.avm.run(this.kernel, new Transaction[] {tx})[0].get().getReturnData();
        return (null != returnData)
                ? org.aion.types.Address.wrap(returnData)
                : null;
    }

    private void oneCall(org.aion.types.Address dappAddr, int transactionNumber) {
        Transaction tx = Transaction.call(FROM, dappAddr, this.kernel.getNonce(FROM), BigInteger.ZERO, new byte[] {(byte)transactionNumber}, ENERGY_LIMIT, ERNGY_PRICE);
        AvmTransactionResult result = (AvmTransactionResult) this.avm.run(this.kernel, new Transaction[] {tx})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }
}

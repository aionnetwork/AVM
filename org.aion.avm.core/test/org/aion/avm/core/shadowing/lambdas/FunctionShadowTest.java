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


    private org.aion.types.Address deployTest(Class<?> testClass) {
        byte[] testJar = JarBuilder.buildJarForMainAndClassesAndUserlib(testClass);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = Transaction.create(FROM, this.kernel.getNonce(FROM), BigInteger.ZERO, txData, ENERGY_LIMIT, ERNGY_PRICE);
        byte[] returnData = this.avm.run(this.kernel, new Transaction[] {tx})[0].get().getReturnData();
        return (null != returnData)
                ? org.aion.types.Address.wrap(returnData)
                : null;
    }
}

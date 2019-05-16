package org.aion.avm.core;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmTransactionResult.Code;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.TestingTransaction;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StackDepthTest {
    private TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    private TestingKernel kernel;
    private AvmImpl avm;
    private Address deployer = TestingKernel.PREMINED_ADDRESS;
    private Address dappAddress;


    @Before
    public void setup() {
        this.kernel = new TestingKernel(this.block);

        AvmConfiguration avmConfig = new AvmConfiguration();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), avmConfig);

        byte[] jar = new CodeAndArguments(JarBuilder.buildJarForMainAndClassesAndUserlib(StackDepthTarget.class), null).encodeToBytes();

        TestingTransaction tx = TestingTransaction.create(this.deployer, this.kernel.getNonce(this.deployer), BigInteger.ZERO, jar, 5_000_000, 1);
        TransactionResult txResult = this.avm.run(this.kernel, new TestingTransaction[] {tx})[0].get();
        assertEquals(Code.SUCCESS, txResult.getResultCode());
        this.dappAddress = Address.wrap(txResult.getReturnData());
    }

    @After
    public void teardown() {
        this.avm.shutdown();
    }

    @Test
    public void testDeepestValidStackDepth() {
        byte[] data = ABIUtil.encodeMethodArguments("recurse", 511);

        TestingTransaction transaction = TestingTransaction.call(this.deployer, this.dappAddress, this.kernel.getNonce(this.deployer), BigInteger.ZERO, data, 2_000_000, 1);
        TransactionResult result = this.avm.run(this.kernel, new TestingTransaction[]{ transaction })[0].get();
        assertEquals(Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testStackOverflow() {
        byte[] data = ABIUtil.encodeMethodArguments("recurse", 512);

        TestingTransaction transaction = TestingTransaction.call(this.deployer, this.dappAddress, this.kernel.getNonce(this.deployer), BigInteger.ZERO, data, 2_000_000, 1);
        TransactionResult result = this.avm.run(this.kernel, new TestingTransaction[]{ transaction })[0].get();
        assertEquals(Code.FAILED_OUT_OF_STACK, result.getResultCode());
    }

    @Test
    public void testLargestValidFibonacci() {
        byte[] data = ABIUtil.encodeMethodArguments("fibonacci", 20);

        TestingTransaction transaction = TestingTransaction.call(this.deployer, this.dappAddress, this.kernel.getNonce(this.deployer), BigInteger.ZERO, data, 2_000_000, 1);
        TransactionResult result = this.avm.run(this.kernel, new TestingTransaction[]{ transaction })[0].get();
        assertEquals(Code.SUCCESS, result.getResultCode());
    }

    @Test
    public void testExpensiveFibonacci() {
        byte[] data = ABIUtil.encodeMethodArguments("fibonacci", 21);

        TestingTransaction transaction = TestingTransaction.call(this.deployer, this.dappAddress, this.kernel.getNonce(this.deployer), BigInteger.ZERO, data, 2_000_000, 1);
        TransactionResult result = this.avm.run(this.kernel, new TestingTransaction[]{ transaction })[0].get();
        assertEquals(Code.FAILED_OUT_OF_ENERGY, result.getResultCode());
    }
}

package org.aion.avm.core.rejection.errors;

import org.aion.types.AionAddress;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.TestingTransaction;
import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;


/**
 * Implemented as part of issue-303 to demonstrate that user code is not able to catch, throw, sub-class, or even instantiate any
 * java.lang.VirtualMachineError type.
 * Since the rejection will be made at the class level, we need several test classes.
 *
 * We need to run these as integration tests since, although many are just testing the rejection phase, some have runtime considerations
 * since Throwable can be caught, but none of the VirtualMachineError instances should actually appear there.
 */
public class RejectVirtualMachineErrorIntegrationTest {
    private static AionAddress FROM = TestingKernel.PREMINED_ADDRESS;
    private static long ENERGY_LIMIT = 5_000_000L;
    private static long ENERGY_PRICE = 1L;

    private static KernelInterface kernel;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock BLOCK = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingKernel(BLOCK);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void rejectCatchError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectCatchError.class);

        // Deploy.
        AvmTransactionResult createResult = deployJar(jar);
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }

    @Test
    public void rejectInstantiateError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectInstantiateError.class);

        // Deploy.
        AvmTransactionResult createResult = deployJar(jar);
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }

    @Test
    public void rejectSubclassError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectSubclassError.class);

        // Deploy.
        AvmTransactionResult createResult = deployJar(jar);
        Assert.assertEquals(AvmTransactionResult.Code.FAILED_REJECTED, createResult.getResultCode());
    }


    private AvmTransactionResult deployJar(byte[] jar) {
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingTransaction transaction = TestingTransaction.create(FROM, kernel.getNonce(FROM), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        return avm.run(RejectVirtualMachineErrorIntegrationTest.kernel, new TestingTransaction[] {transaction})[0].get();
    }
}

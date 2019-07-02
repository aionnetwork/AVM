package org.aion.avm.core.rejection.errors;

import org.aion.avm.core.*;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.TransactionResult;
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
    private static AionAddress FROM = TestingState.PREMINED_ADDRESS;
    private static long ENERGY_LIMIT = 5_000_000L;
    private static long ENERGY_PRICE = 1L;

    private static IExternalState externalState;
    private static AvmImpl avm;

    @BeforeClass
    public static void setup() {
        TestingBlock BLOCK = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        externalState = new TestingState(BLOCK);
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
        TransactionResult createResult = deployJar(jar);
        Assert.assertEquals(AvmInternalError.FAILED_REJECTED_CLASS.error, createResult.transactionStatus.causeOfError);
    }

    @Test
    public void rejectInstantiateError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectInstantiateError.class);

        // Deploy.
        TransactionResult createResult = deployJar(jar);
        Assert.assertEquals(AvmInternalError.FAILED_REJECTED_CLASS.error, createResult.transactionStatus.causeOfError);
    }

    @Test
    public void rejectSubclassError() throws Exception {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(RejectSubclassError.class);

        // Deploy.
        TransactionResult createResult = deployJar(jar);
        Assert.assertEquals(AvmInternalError.FAILED_REJECTED_CLASS.error, createResult.transactionStatus.causeOfError);
    }


    private TransactionResult deployJar(byte[] jar) {
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        Transaction transaction = AvmTransactionUtil.create(FROM, externalState.getNonce(FROM), BigInteger.ZERO, txData, ENERGY_LIMIT, ENERGY_PRICE);
        return avm.run(RejectVirtualMachineErrorIntegrationTest.externalState, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber()-1)[0].getResult();
    }
}

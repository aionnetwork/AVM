package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;

import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.Test;


/**
 * This is a very basic performance test.  While it can be run as a standard unit test, it is designed to be easily tunable for more hands-on
 * measurement and profiling.
 * See BasicPerfContract for details of what the test application is doing.  In short, it is a graph traversal/manipulation algorithm, so it
 * demonstrates how our persistence model fits with this.
 */
public class BasicPerfTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private final static boolean REPORT = false;
    private final static int COUNT = 1000;
    private final static int THREAD_COUNT = 1;

    @Test
    public void testDeployAndRun() throws Throwable {
        TestRunnable[] threads = new TestRunnable[THREAD_COUNT];
        // Setup.
        byte[] jar = JarBuilder.buildJarForMainAndClasses(BasicPerfContract.class
                , AionList.class
                , AionMap.class
                , AionSet.class
        );
        
        // Deploy.
        for (int i = 0; i < THREAD_COUNT; ++i) {
            threads[i] = new TestRunnable();
            threads[i].deploy(jar, new byte[0]);
        }
        
        // Run.
        long start = System.nanoTime();
        for (int i = 0; i < THREAD_COUNT; ++i) {
            threads[i].start();
        }
        for (int i = 0; i < THREAD_COUNT; ++i) {
            threads[i].waitForSafeTermination();
        }
        long end = System.nanoTime();
        if (REPORT) {
            // Report - note that this doesn't take the thread count into account.
            System.out.println("NANOS PER RUN: " + ((end - start) / COUNT));
        }
    }


    private static class TestRunnable extends Thread {
        private AionAddress deployer = TestingState.PREMINED_ADDRESS;
        private IExternalState externalState;
        private AvmImpl avm;
        private AionAddress contractAddress;
        private Throwable backgroundThrowable;
        public void deploy(byte[] jar, byte[] arguments) {
            // Deploy.
            this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
            TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
            this.externalState = new TestingState(block);
            long transaction1EnergyLimit = 1_000_000_000l;
            Transaction tx1 = AvmTransactionUtil.create(deployer, externalState.getNonce(deployer), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), transaction1EnergyLimit, 1L);
            TransactionResult result1 = this.avm.run(this.externalState, new Transaction[] {tx1}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber() - 1)[0].getResult();
            Assert.assertTrue(result1.transactionStatus.isSuccess());
            this.contractAddress = new AionAddress(result1.copyOfTransactionOutput().orElseThrow());
        }
        public void waitForSafeTermination() throws Throwable {
            this.join();
            if (null != this.backgroundThrowable) {
                throw this.backgroundThrowable;
            }
            this.avm.shutdown();
        }
        @Override
        public void run() {
            try {
                safeRun();
            } catch (Throwable t) {
                this.backgroundThrowable = t;
            }
        }
        private void safeRun() {
            int blockStart = 2;
            for (int i = blockStart; i < (COUNT + blockStart); ++i) {
                long transaction1EnergyLimit = 1_000_000_000l;
                Transaction tx1 = AvmTransactionUtil.call(deployer, this.contractAddress, externalState.getNonce(deployer), BigInteger.ZERO, new byte[0], transaction1EnergyLimit, 1L);
                TransactionResult result1 = this.avm.run(this.externalState, new Transaction[] {tx1}, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber() - 1)[0].getResult();
                Assert.assertTrue(result1.transactionStatus.isSuccess());
            }
        }
    }
}

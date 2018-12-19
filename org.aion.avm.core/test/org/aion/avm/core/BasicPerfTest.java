package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionContextImpl;
import org.aion.kernel.TransactionResult;

import org.aion.vm.api.interfaces.KernelInterface;
import org.junit.Assert;
import org.junit.Test;


/**
 * This is a very basic performance test.  While it can be run as a standard unit test, it is designed to be easily tunable for more hands-on
 * measurement and profiling.
 * See BasicPerfContract for details of what the test application is doing.  In short, it is a graph traversal/manipulation algorithm, so it
 * demonstrates how our persistence model fits with this.
 */
public class BasicPerfTest {
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
        // Report - note that this doesn't take the thread count into account.
        System.out.println("NANOS PER RUN: " + ((end - start) / COUNT));
    }


    private static class TestRunnable extends Thread {
        private org.aion.vm.api.interfaces.Address deployer = KernelInterfaceImpl.PREMINED_ADDRESS;
        private KernelInterface kernel;
        private Avm avm;
        private org.aion.vm.api.interfaces.Address contractAddress;
        private Throwable backgroundThrowable;
        public void deploy(byte[] jar, byte[] arguments) {
            // Deploy.
            this.kernel = new KernelInterfaceImpl();
            this.avm = CommonAvmFactory.buildAvmInstance(kernel);
            Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
            long transaction1EnergyLimit = 1_000_000_000l;
            Transaction tx1 = Transaction.create(deployer, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, new CodeAndArguments(jar, arguments).encodeToBytes(), transaction1EnergyLimit, 1L);
            TransactionResult result1 = this.avm.run(new TransactionContext[] {new TransactionContextImpl(tx1, block)})[0].get();
            Assert.assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
            this.contractAddress = AvmAddress.wrap(result1.getReturnData());
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
                Block block = new Block(new byte[32], i, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
                long transaction1EnergyLimit = 1_000_000_000l;
                Transaction tx1 = Transaction.call(deployer, this.contractAddress, kernel.getNonce(deployer).longValue(), BigInteger.ZERO, new byte[0], transaction1EnergyLimit, 1L);
                TransactionResult result1 = this.avm.run(new TransactionContext[] {new TransactionContextImpl(tx1, block)})[0].get();
                Assert.assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
                
                // Every 100 iterations, we also want to run a GC, to verify that this doesn't break anything in a long-running test.
                if (0 == (i % 100)) {
                    Transaction gcCall = Transaction.garbageCollect(this.contractAddress, kernel.getNonce(this.contractAddress).longValue(), transaction1EnergyLimit, 1L);
                    TransactionResult gcResult = this.avm.run(new TransactionContext[] {new TransactionContextImpl(gcCall, block)})[0].get();
                    Assert.assertEquals(TransactionResult.Code.SUCCESS, gcResult.getStatusCode());
                    // Note that this GC never actually frees anything, since our workload never orphans objects.
                    Assert.assertEquals(0, gcResult.getEnergyUsed());
                }
            }
        }
    }
}
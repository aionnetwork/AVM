package org.aion.avm.core;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.SimpleFuture;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionResult;
import org.aion.parallel.TransactionTask;
import org.junit.Assert;
import org.junit.Test;


public class HandoffMonitorTest {
    @Test
    public void startupShutdown() {
        MonitorThread thread = new MonitorThread();
        Set<Thread> executorThreads = new HashSet<>();
        executorThreads.add(thread);
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);
        thread.startAgainstMonitor(monitor);
        monitor.stopAndWaitForShutdown();
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void failEnqueueBeforeFutureGet() {
        // Startup.
        MonitorThread thread = new MonitorThread();
        Set<Thread> executorThreads = new HashSet<>();
        executorThreads.add(thread);
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);
        thread.startAgainstMonitor(monitor);
        
        // Enqueue transaction.
        monitor.sendTransactionsAsynchronously(new FakeTransaction[] {new FakeTransaction()});
        // Second enqueue should fail with RuntimeAssertionError.
        boolean didFail = false;
        try {
            monitor.sendTransactionsAsynchronously(new FakeTransaction[] {new FakeTransaction()});
        } catch (RuntimeAssertionError e) {
            didFail = true;
        }
        
        monitor.stopAndWaitForShutdown();
        Assert.assertFalse(thread.isAlive());
        Assert.assertTrue(didFail);
    }

    @Test
    public void commonCallSequence() {
        // Startup.
        MonitorThread thread = new MonitorThread();
        Set<Thread> executorThreads = new HashSet<>();
        executorThreads.add(thread);
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);
        thread.startAgainstMonitor(monitor);
        
        // Enqueue transaction and process result.
        SimpleFuture<TransactionResult>[] results = monitor.sendTransactionsAsynchronously(new FakeTransaction[] {new FakeTransaction()});
        Assert.assertEquals(1, results.length);
        results[0].get();
        
        // Enqueue and process again.
        results = monitor.sendTransactionsAsynchronously(new FakeTransaction[] {new FakeTransaction()});
        Assert.assertEquals(1, results.length);
        results[0].get();
        
        monitor.stopAndWaitForShutdown();
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void batchingCallSequence() {
        // Startup.
        MonitorThread thread = new MonitorThread();
        Set<Thread> executorThreads = new HashSet<>();
        executorThreads.add(thread);
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);
        thread.startAgainstMonitor(monitor);
        
        // Enqueue 2 transactions and verify the result array length.
        SimpleFuture<TransactionResult>[] results = monitor.sendTransactionsAsynchronously(new FakeTransaction[] {new FakeTransaction(), new FakeTransaction()});
        Assert.assertEquals(2, results.length);
        results[0].get();
        results[1].get();
        
        // Enqueue another batch to make sure that we reset state correctly.
        results = monitor.sendTransactionsAsynchronously(new FakeTransaction[] {new FakeTransaction(), new FakeTransaction(), new FakeTransaction()});
        Assert.assertEquals(3, results.length);
        results[0].get();
        results[1].get();
        results[2].get();
        
        monitor.stopAndWaitForShutdown();
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void batchingCallMultithreaded() {
        // Startup.
        MonitorThread t1 = new MonitorThread("Executor 0");
        MonitorThread t2 = new MonitorThread("Executor 1");
        MonitorThread t3 = new MonitorThread("Executor 2");
        MonitorThread t4 = new MonitorThread("Executor 3");
        Set<Thread> executorThreads = new HashSet<>();
        executorThreads.add(t1);
        executorThreads.add(t2);
        executorThreads.add(t3);
        executorThreads.add(t4);
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);

        t1.startAgainstMonitor(monitor);
        t2.startAgainstMonitor(monitor);
        t3.startAgainstMonitor(monitor);
        t4.startAgainstMonitor(monitor);

        // Enqueue 2 transactions and verify the result array length.
        SimpleFuture<TransactionResult>[] results = monitor.sendTransactionsAsynchronously(new FakeTransaction[] {new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction()});
        Assert.assertEquals(8, results.length);

        for (int i = 0; i < 8; i++){
            results[i].get();
        }

        // Enqueue another batch to make sure that we reset state correctly.
        results = monitor.sendTransactionsAsynchronously(new FakeTransaction[] {new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction(), new FakeTransaction()});
        Assert.assertEquals(10, results.length);

        for (int i = 0; i < 10; i++){
            results[i].get();
        }

        monitor.stopAndWaitForShutdown();
        Assert.assertFalse(t1.isAlive());
        Assert.assertFalse(t2.isAlive());
        Assert.assertFalse(t3.isAlive());
        Assert.assertFalse(t4.isAlive());
    }

    @Test
    public void verifyCallMultithreaded() {
        // Startup.

        Set<Thread> executorThreads = new HashSet<>();
        for (int i = 0; i < 16; i++){
            executorThreads.add(new MonitorThread());
        }
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);

        for (Thread t: executorThreads){
            ((MonitorThread) t).startAgainstMonitor(monitor);
        }

        FakeTransaction[] transactions = new FakeTransaction[128];
        for (int i = 0; i < transactions.length; i++){
            transactions[i] = new FakeTransaction();
        }

        SimpleFuture<TransactionResult>[] results = monitor.sendTransactionsAsynchronously(transactions);
        Assert.assertEquals(128, results.length);

        Set<Thread> verifySet = new HashSet<>();
        for (int i = 0; i < 128; i++){
            verifySet.add(((FakeResult) results[i].get()).executor);
        }

        Assert.assertTrue(verifySet.size() > 1);
        Assert.assertTrue(verifySet.size() <= 128);

        monitor.stopAndWaitForShutdown();

        for (Thread t: executorThreads){
            Assert.assertFalse(t.isAlive());
        }
    }

    private class MonitorThread extends Thread {
        private HandoffMonitor monitor;

        public MonitorThread(){
            super();
        }

        public MonitorThread(String name){
            super(name);
        }

        public void startAgainstMonitor(HandoffMonitor monitor) {
            this.monitor = monitor;
            this.start();
        }
        @Override
        public void run() {
            TransactionTask task = this.monitor.blockingPollForTransaction(null, null);
            while (null != task) {
                // Fake up a result.
                TransactionResult result = new FakeResult(this);
                task = this.monitor.blockingPollForTransaction(result, task);
            }
        }
    }

    private class FakeResult extends TransactionResult{
        public Thread executor;

        public FakeResult(Thread t){
            this.executor = t;
        }
    }


    /**
     * No calls on this are expected.
     */
    private static class FakeTransaction implements TransactionContext {
        @Override
        public boolean isCreate() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public boolean isGarbageCollectionRequest() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public byte[] getAddress() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public byte[] getCaller() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public byte[] getOrigin() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public long getNonce() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public BigInteger getValue() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public byte[] getData() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public long getEnergyLimit() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public long getEneryPrice() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public int getBasicCost() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public long getTransactionTimestamp() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public long getBlockTimestamp() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public long getBlockNumber() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public long getBlockEnergyLimit() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public byte[] getBlockCoinbase() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public byte[] getBlockPreviousHash() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public BigInteger getBlockDifficulty() {
            throw new AssertionError("No calls expected");
        }
        @Override
        public int getInternalCallDepth() {
            throw new AssertionError("No calls expected");
        }
    }
}

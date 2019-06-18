package org.aion.avm.core;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.aion.types.Transaction;
import i.RuntimeAssertionError;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.AvmWrappedTransactionResult;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.parallel.TransactionTask;
import org.aion.types.TransactionResult;
import org.aion.types.TransactionStatus;
import org.junit.Assert;
import org.junit.Test;


public class HandoffMonitorTest {
    @Test
    public void startupShutdown() {
        MonitorThread thread = new MonitorThread(null, 0);
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
        MonitorThread thread = new MonitorThread(null, 0);
        Set<Thread> executorThreads = new HashSet<>();
        executorThreads.add(thread);
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);
        thread.startAgainstMonitor(monitor);
        
        // Enqueue transaction.
        monitor.sendTransactionsAsynchronously(wrapTransactionInTasks(new Transaction[] {newFakeTransaction()}));
        // Second enqueue should fail with RuntimeAssertionError.
        boolean didFail = false;
        try {
            monitor.sendTransactionsAsynchronously(wrapTransactionInTasks(new Transaction[] {newFakeTransaction()}));
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
        MonitorThread thread = new MonitorThread(null, 0);
        Set<Thread> executorThreads = new HashSet<>();
        executorThreads.add(thread);
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);
        thread.startAgainstMonitor(monitor);
        
        // Enqueue transaction and process result.
        FutureResult[] results = monitor.sendTransactionsAsynchronously(wrapTransactionInTasks(new Transaction[] {newFakeTransaction()}));
        Assert.assertEquals(1, results.length);
        results[0].getResult();
        
        // Enqueue and process again.
        results = monitor.sendTransactionsAsynchronously(wrapTransactionInTasks(new Transaction[] {newFakeTransaction()}));
        Assert.assertEquals(1, results.length);
        results[0].getResult();
        
        monitor.stopAndWaitForShutdown();
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void batchingCallSequence() {
        // Startup.
        MonitorThread thread = new MonitorThread(null, 0);
        Set<Thread> executorThreads = new HashSet<>();
        executorThreads.add(thread);
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);
        thread.startAgainstMonitor(monitor);
        
        // Enqueue 2 transactions and verify the result array length.
        FutureResult[] results = monitor.sendTransactionsAsynchronously(wrapTransactionInTasks(new Transaction[] {newFakeTransaction(), newFakeTransaction()}));
        Assert.assertEquals(2, results.length);
        results[0].getResult();
        results[1].getResult();
        
        // Enqueue another batch to make sure that we reset state correctly.
        results = monitor.sendTransactionsAsynchronously(wrapTransactionInTasks(new Transaction[] {newFakeTransaction(), newFakeTransaction(), newFakeTransaction()}));
        Assert.assertEquals(3, results.length);
        results[0].getResult();
        results[1].getResult();
        results[2].getResult();
        
        monitor.stopAndWaitForShutdown();
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void batchingCallMultithreaded() {
        // Startup.
        CyclicBarrier firstTaskBarrier = new CyclicBarrier(4);
        MonitorThread t1 = new MonitorThread(firstTaskBarrier, 1);
        MonitorThread t2 = new MonitorThread(firstTaskBarrier, 2);
        MonitorThread t3 = new MonitorThread(firstTaskBarrier, 3);
        MonitorThread t4 = new MonitorThread(firstTaskBarrier, 4);
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
        FutureResult[] results = monitor.sendTransactionsAsynchronously(wrapTransactionInTasks(new Transaction[] {newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction()}));
        Assert.assertEquals(8, results.length);

        for (int i = 0; i < 8; i++){
            results[i].getResult();
        }

        // Enqueue another batch to make sure that we reset state correctly.
        results = monitor.sendTransactionsAsynchronously(wrapTransactionInTasks(new Transaction[] {newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction(), newFakeTransaction()}));
        Assert.assertEquals(10, results.length);

        for (int i = 0; i < 10; i++){
            results[i].getResult();
        }

        monitor.stopAndWaitForShutdown();
        Assert.assertFalse(t1.isAlive());
        Assert.assertFalse(t2.isAlive());
        Assert.assertFalse(t3.isAlive());
        Assert.assertFalse(t4.isAlive());
    }

    @Test
    public void verifyCallMultithreaded() {
        final int threadCount = 16;
        // Note that want to force the tasks to spread across the threads so we will give them a barrier to synchronize after their first tasks.
        CyclicBarrier firstTaskBarrier = new CyclicBarrier(threadCount);
        
        // Startup.
        Set<Thread> executorThreads = new HashSet<>();
        for (int i = 0; i < threadCount; i++){
            // Each thread receives a unique int ID.
            executorThreads.add(new MonitorThread(firstTaskBarrier, i));
        }
        HandoffMonitor monitor = new HandoffMonitor(executorThreads);

        for (Thread t: executorThreads){
            ((MonitorThread) t).startAgainstMonitor(monitor);
        }

        Transaction[] transactions = new Transaction[128];
        for (int i = 0; i < transactions.length; i++){
            transactions[i] = newFakeTransaction();
        }

        FutureResult[] results = monitor.sendTransactionsAsynchronously(wrapTransactionInTasks(transactions));
        Assert.assertEquals(128, results.length);

        Set<Integer> verifySet = new HashSet<>();
        for (int i = 0; i < 128; i++){
            // Each thread converts its unique ID to a byte array stored in the result data, we grab that.
            TransactionResult transactionResult = results[i].getResult();
            verifySet.add(bytesToThreadID(transactionResult.copyOfTransactionOutput().orElseThrow()));
        }

        Assert.assertEquals(threadCount, verifySet.size());

        monitor.stopAndWaitForShutdown();

        for (Thread t: executorThreads){
            Assert.assertFalse(t.isAlive());
        }
    }

    private class MonitorThread extends Thread {
        private final CyclicBarrier firstTaskBarrier;
        private final int threadID;
        private HandoffMonitor monitor;

        public MonitorThread(CyclicBarrier firstTaskBarrier, int threadID){
            this.firstTaskBarrier = firstTaskBarrier;
            this.threadID = threadID;
        }

        public void startAgainstMonitor(HandoffMonitor monitor) {
            this.monitor = monitor;
            this.start();
        }
        @Override
        public void run() {
            TransactionTask task = this.monitor.blockingPollForTransaction(null, null);
            if (null != this.firstTaskBarrier) {
                // We want to wait until each thread has picked up a transaction before proceeding.
                try {
                    this.firstTaskBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    // We don't use interruption and shouldn't see broken barriers.
                    e.printStackTrace();
                    Assert.fail();
                }
            }
            while (null != task) {
                // Fake up a result.
                AvmWrappedTransactionResult result = newFakeResult(this.threadID);
                task = this.monitor.blockingPollForTransaction(result, task);
            }
        }
    }

    private static AvmWrappedTransactionResult newFakeResult(int threadID) {
        TransactionResult transactionResult = new TransactionResult(TransactionStatus.successful(), Collections.emptyList(), Collections.emptyList(), 0, threadIDtoBytes(threadID));
        return new AvmWrappedTransactionResult(transactionResult, null, null, AvmInternalError.NONE);
    }

    private static byte[] threadIDtoBytes(int threadID) {
        return ByteBuffer.allocate(4).putInt(threadID).array();
    }

    private static int bytesToThreadID(byte[] bytes) {
        return ByteBuffer.allocate(4).put(bytes).flip().getInt();
    }

    private static Transaction newFakeTransaction() {
        return AvmTransactionUtil.call(Helpers.ZERO_ADDRESS, Helpers.ZERO_ADDRESS, BigInteger.ZERO, BigInteger.ZERO, new byte[0], 1L, 1L);
    }

    private static TransactionTask[] wrapTransactionInTasks(Transaction[] transactions) {
        TransactionTask[] tasks = new TransactionTask[transactions.length];
        // (we don't consult the capabilities since there is no creation)
        IExternalCapabilities capabilities = null;
        for (int i = 0; i < transactions.length; ++i) {
            tasks[i] = new TransactionTask(null, transactions[i], i, Helpers.ZERO_ADDRESS);
        }
        return tasks;
    }
}

package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.SimpleFuture;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionResult;
import org.junit.Assert;
import org.junit.Test;


public class HandoffMonitorTest {
    @Test
    public void startupShutdown() {
        MonitorThread thread = new MonitorThread();
        HandoffMonitor monitor = new HandoffMonitor(thread);
        thread.startAgainstMonitor(monitor);
        monitor.stopAndWaitForShutdown();
        Assert.assertFalse(thread.isAlive());
    }

    @Test
    public void failEnqueueBeforeFutureGet() {
        // Startup.
        MonitorThread thread = new MonitorThread();
        HandoffMonitor monitor = new HandoffMonitor(thread);
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
        HandoffMonitor monitor = new HandoffMonitor(thread);
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
        HandoffMonitor monitor = new HandoffMonitor(thread);
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


    private static class MonitorThread extends Thread {
        private HandoffMonitor monitor;
        public void startAgainstMonitor(HandoffMonitor monitor) {
            this.monitor = monitor;
            this.start();
        }
        @Override
        public void run() {
            TransactionContext transaction = this.monitor.blockingPollForTransaction(null);
            while (null != transaction) {
                // Fake up a result.
                TransactionResult result = new TransactionResult();
                transaction = this.monitor.blockingPollForTransaction(result);
            }
        }
    };


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
        public long getValue() {
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

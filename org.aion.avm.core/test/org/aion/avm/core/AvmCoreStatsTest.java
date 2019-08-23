package org.aion.avm.core;

import java.math.BigInteger;

import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;

import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class AvmCoreStatsTest {
    private static final AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static final long ENERGY_LIMIT_CALL = 2_000_000L;
    private static final long ENERGY_LIMIT_DEPLOY = 5_000_000L;
    private static final long ENERGY_PRICE = 1L;

    private static TestingBlock block;

    @BeforeClass
    public static void setupClass() {
        block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    }

    /**
     * Run some calls, 1 transaction per batch, and observe that the stats are consistent.
     */
    @Test
    public void testSequentialReentrantCalls() {
        long startNanos = System.nanoTime();
        boolean shouldFail = false;
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // Capture the stats.
        AvmCoreStats stats = avm.getStats();
        Assert.assertEquals(0, stats.batchesConsumed);
        Assert.assertEquals(0, stats.transactionsConsumed);
        Assert.assertEquals(0L, maxTotalNanos(stats, 0L));
        
        // Deploy the test contract.
        Transaction create = createCreateTransaction(kernel, txData);
        TransactionResult createResult = runSuccessfulBatch(kernel, avm, new Transaction[] {create})[0];
        AionAddress contractAddress = new AionAddress(createResult.copyOfTransactionOutput().orElseThrow());
        Assert.assertEquals(1, stats.batchesConsumed);
        Assert.assertEquals(1, stats.transactionsConsumed);
        Assert.assertEquals(1, combineThreadTransactions(stats));
        
        // Clear the stats and then send a few single-transaction batches.
        long totalNanos = maxTotalNanos(stats, 0L);
        stats.clear();
        Transaction transaction = createCallTransaction(contractAddress, kernel.getNonce(deployer), "getFar", shouldFail);
        runSuccessfulBatch(kernel, avm, new Transaction[] {transaction});
        transaction = createCallTransaction(contractAddress, kernel.getNonce(deployer), "getFar", shouldFail);
        runSuccessfulBatch(kernel, avm, new Transaction[] {transaction});
        Assert.assertEquals(2, stats.batchesConsumed);
        Assert.assertEquals(2, stats.transactionsConsumed);
        Assert.assertEquals(2, combineThreadTransactions(stats));
        
        avm.shutdown();
        totalNanos = maxTotalNanos(stats, totalNanos);
        long endNanos = System.nanoTime();
        long runningNanos = endNanos - startNanos;
        Assert.assertTrue(runningNanos >= totalNanos);
    }

    /**
     * Run some calls, 2 transaction per batch, and observe that the stats are consistent.
     */
    @Test
    public void testConcurrentReentrantCalls() {
        long startNanos = System.nanoTime();
        boolean shouldFail = false;
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(ReentrantCrossCallResource.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // Capture the stats.
        AvmCoreStats stats = avm.getStats();
        Assert.assertEquals(0, stats.batchesConsumed);
        Assert.assertEquals(0, stats.transactionsConsumed);
        Assert.assertEquals(0L, maxTotalNanos(stats, 0L));
        
        // Deploy the test contract.
        Transaction create = createCreateTransaction(kernel, txData);
        TransactionResult createResult = runSuccessfulBatch(kernel, avm, new Transaction[] {create})[0];
        AionAddress contractAddress = new AionAddress(createResult.copyOfTransactionOutput().orElseThrow());
        Assert.assertEquals(1, stats.batchesConsumed);
        Assert.assertEquals(1, stats.transactionsConsumed);
        Assert.assertEquals(1, combineThreadTransactions(stats));
        
        // Clear the stats and then send a multi-transaction batches (for simplicity, we will use the same contract - which may cause an abort+retry).
        long totalNanos = maxTotalNanos(stats, 0L);
        stats.clear();
        BigInteger start = kernel.getNonce(deployer);
        BigInteger next = start.add(BigInteger.ONE);
        Transaction transaction1 = createCallTransaction(contractAddress, start, "getFar", shouldFail);
        Transaction transaction2 = createCallTransaction(contractAddress, next, "getFar", shouldFail);
        runSuccessfulBatch(kernel, avm, new Transaction[] {transaction1, transaction2});
        Assert.assertEquals(1, stats.batchesConsumed);
        Assert.assertEquals(2, stats.transactionsConsumed);
        Assert.assertEquals(2, combineThreadTransactions(stats));
        
        avm.shutdown();
        totalNanos = maxTotalNanos(stats, totalNanos);
        long endNanos = System.nanoTime();
        long runningNanos = endNanos - startNanos;
        Assert.assertTrue(runningNanos >= totalNanos);
    }


    private Transaction createCreateTransaction(IExternalState externalState, byte[] createData) {
        return AvmTransactionUtil.create(deployer, externalState.getNonce(deployer), BigInteger.ZERO, createData, ENERGY_LIMIT_DEPLOY, ENERGY_PRICE);
    }

    private Transaction createCallTransaction(AionAddress dAppAddress, BigInteger nonce, String methodName, boolean arg) {
        byte[] argData = new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .encodeOneBoolean(arg)
                .toBytes();
        return AvmTransactionUtil.call(deployer, dAppAddress, nonce, BigInteger.ZERO, argData, ENERGY_LIMIT_CALL, ENERGY_PRICE);
    }

    private TransactionResult[] runSuccessfulBatch(IExternalState externalState, AvmImpl avm, Transaction[] batch) {
        FutureResult[] futures = avm.run(externalState, batch, ExecutionType.ASSUME_MAINCHAIN, externalState.getBlockNumber() - 1);
        TransactionResult[] results = new TransactionResult[batch.length];
        for (int i = 0; i < batch.length; ++i) {
            TransactionResult result = futures[i].getResult();
            Assert.assertTrue(result.transactionStatus.isSuccess());
            results[i] = result;
        }
        return results;
    }

    private int combineThreadTransactions(AvmCoreStats stats) {
        int total = 0;
        for (AvmThreadStats stat : stats.threadStats) {
            total += stat.transactionsProcessed;
        }
        return total;
    }

    // Note that we pass in the max from a previous sample since these can overlap when calculating multiple samples
    // (since a thread sleeping across both samples will not report a time until the end, when it reports the entire sleeping time).
    private long maxTotalNanos(AvmCoreStats stats, long startMax) {
        long max = startMax;
        for (AvmThreadStats stat : stats.threadStats) {
            long nanos = stat.nanosRunning + stat.nanosSleeping;
            max = Math.max(max, nanos);
        }
        return max;
    }
}

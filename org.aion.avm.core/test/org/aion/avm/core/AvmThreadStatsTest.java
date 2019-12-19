package org.aion.avm.core;

import avm.Address;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.avm.utilities.JarBuilder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.parallel.TestContract;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;

public class AvmThreadStatsTest {

    private static TestingBlock block;

    @BeforeClass
    public static void setupClass() {
        block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
    }

    @Test
    public void testSequentialReentrantCalls() {
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        byte[] code = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(TestContract.class, Collections.emptyMap(), ABIDecoder.class, ABIException.class, ABIEncoder.class);

        int length = 10;
        AionAddress[] user = new AionAddress[length];
        Transaction[] ctx = new Transaction[length];
        for (int i = 0; i < user.length; i++) {
            user[i] = Helpers.randomAddress();
            kernel.adjustBalance(user[i], BigInteger.TEN.pow(20));
            ctx[i] = AvmTransactionUtil.create(user[i], BigInteger.ZERO, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 5_000_000L, 1);
        }

        FutureResult[] results = avm.run(kernel, ctx, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        AionAddress[] contractAddresses = new AionAddress[results.length];
        for (int i = 0; i < results.length; i++) {
            contractAddresses[i] = new AionAddress(results[i].getResult().copyOfTransactionOutput().orElseThrow());
        }

        Transaction[] tx = new Transaction[results.length - 1];
        // A->A->B, B->B->C, C->C->D
        for (int i = 0; i < contractAddresses.length - 1; i++) {
            byte[] internalCallData = new ABIStreamingEncoder()
                    .encodeOneString("doCallOther")
                    .encodeOneAddress(new Address(contractAddresses[i + 1].toByteArray()))
                    .encodeOneByteArray(new ABIStreamingEncoder().encodeOneString("addValue").toBytes())
                    .toBytes();

            byte[] data = new ABIStreamingEncoder().encodeOneString("doCallThis").encodeOneByteArray(internalCallData).toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, data, 5_000_000, 1);
        }

        results = avm.run(kernel, tx, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        for (FutureResult f : results) {
            f.getResult();
        }

        AvmCoreStats stats = avm.getStats();
        Assert.assertEquals(2, stats.batchesConsumed);
        Assert.assertEquals(length * 2 - 1, stats.transactionsConsumed);

        AvmThreadStats[] threadStats = stats.threadStats;
        int transformationTotalCount = 0;
        int retransformationTotalCount = 0;
        int totalAcquireCount = 0;
        int totalAbortCount = 0;

        for (AvmThreadStats avmThreadStats : threadStats) {
            transformationTotalCount += avmThreadStats.transformationCount;
            Assert.assertTrue(avmThreadStats.transformationAvgTimeNanos > 0);
            Assert.assertTrue(avmThreadStats.transformationMaxTimeNanos > 0);
            Assert.assertTrue(avmThreadStats.transformationMaxTimeNanos > avmThreadStats.transformationAvgTimeNanos);
            Assert.assertTrue(avmThreadStats.transformationCount > 0);
            // no contracts are retransformed at this step
            Assert.assertEquals(0, avmThreadStats.retransformationCount);
            Assert.assertEquals(0, avmThreadStats.retransformationAvgTimeNanos);
            Assert.assertEquals(0, avmThreadStats.retransformationMaxTimeNanos);
            // Note that some of these calls have conflicting dependencies so some waits or aborts could happen (result cannot be verified as it is non-deterministic).
            totalAcquireCount += avmThreadStats.concurrentResource_acquired;
            totalAbortCount += avmThreadStats.concurrentResource_aborted;
        }

        // There are at least "length" contracts deployed, causing "length" transformations
        Assert.assertTrue(length <= transformationTotalCount);
        // Each abort can cause at MOST, an additional "length" transformations
        Assert.assertTrue((totalAbortCount + 1) * length >= transformationTotalCount);
        
        // Should be a minimum of 2*contracts (deployer+contract) + 4*(contractAddresses.length - 1) (caller+contract+contract+other).
        // (in reality, it will typically be higher since aborts will cause a re-request - single-threaded, this always produces 56).
        Assert.assertTrue(totalAcquireCount >= ((2 * length) + (4 * (contractAddresses.length - 1))));

        for (AionAddress address : contractAddresses) {
            kernel.setTransformedCode(address, null);
        }

        stats.clear();
        totalAcquireCount = 0;

        // each call will re-transform the code
        for (int i = 0; i < length - 1; i++) {
            byte[] getCallCount = new ABIStreamingEncoder().encodeOneString("getCallCount").toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, getCallCount, 5_000_000, 1);
        }

        results = avm.run(kernel, tx, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        for (FutureResult f : results) {
            Assert.assertEquals(2, new ABIDecoder(f.getResult().copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
        }
        
        totalAbortCount = 0;

        for (AvmThreadStats avmThreadStats : threadStats) {
            retransformationTotalCount += avmThreadStats.retransformationCount;
            Assert.assertEquals(0, avmThreadStats.transformationAvgTimeNanos);
            Assert.assertEquals(0, avmThreadStats.transformationMaxTimeNanos);
            Assert.assertEquals(0, avmThreadStats.transformationCount);

            Assert.assertTrue(avmThreadStats.retransformationAvgTimeNanos > 0);
            Assert.assertTrue(avmThreadStats.retransformationMaxTimeNanos > avmThreadStats.retransformationAvgTimeNanos);

            totalAcquireCount += avmThreadStats.concurrentResource_acquired;
            totalAbortCount += avmThreadStats.concurrentResource_aborted;
            
            // These assertions were made redundant by the fix in AKI-638, which makes all threads acquire the coinbase lock.
            // AKI-639 is an item that should remove the necessity of acquiring the coinbase lock. 
            // When AKI-639 is done, these assertions can be meaningful again.
            Assert.assertTrue(0 <= avmThreadStats.concurrentResource_waited);
            Assert.assertTrue(0 <= avmThreadStats.concurrentResource_aborted);
        }

        // There are at least "length -1" retransformations
        Assert.assertTrue(length - 1 <= retransformationTotalCount);
        // Each abort can cause at MOST, an additional "length -1" retransformations
        Assert.assertTrue((totalAbortCount + 1) * (length - 1) >= retransformationTotalCount);
        
        // Total acquires is a sender, a contract, and the coinbase for each length-1.
        Assert.assertTrue(3 * (length - 1) <= totalAcquireCount);
        // Each abort can cause at MOST, an additional "3 * (length -1)" retransformations
        Assert.assertTrue((totalAbortCount + 1) * 3 * (length - 1) >= totalAcquireCount);
        avm.shutdown();
    }

    @Test
    public void testCreate() {
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        byte[] code = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(TestContract.class, Collections.emptyMap(), ABIDecoder.class, ABIException.class, ABIEncoder.class);

        int length = 4;
        AionAddress[] user = new AionAddress[length];
        Transaction[] ctx = new Transaction[length];
        for (int i = 0; i < user.length; i++) {
            user[i] = Helpers.randomAddress();
            kernel.adjustBalance(user[i], BigInteger.TEN.pow(20));
            ctx[i] = AvmTransactionUtil.create(user[i], BigInteger.ZERO, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 5_000_000L, 1);
        }

        FutureResult[] results = avm.run(kernel, ctx, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        AionAddress[] contractAddresses = new AionAddress[results.length];
        for (int i = 0; i < results.length; i++) {
            contractAddresses[i] = new AionAddress(results[i].getResult().copyOfTransactionOutput().orElseThrow());
        }

        Transaction[] tx = new Transaction[results.length];
        for (int i = 0; i < contractAddresses.length; i++) {
            byte[] data = new ABIStreamingEncoder()
                    .encodeOneString("deploy")
                    .encodeOneByteArray(new CodeAndArguments(code, null).encodeToBytes())
                    .toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, data, 5_000_000, 1);
        }

        results = avm.run(kernel, tx, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        for (FutureResult f : results) {
            f.getResult();
        }

        AvmCoreStats stats = avm.getStats();
        AvmThreadStats[] threadStats = stats.threadStats;
        int transformationTotalCount = 0;
        int totalAcquireCount = 0;
        int totalAbortCount = 0;
        
        for (AvmThreadStats avmThreadStats : threadStats) {
            transformationTotalCount += avmThreadStats.transformationCount;
            Assert.assertTrue(avmThreadStats.transformationAvgTimeNanos > 0);
            Assert.assertTrue(avmThreadStats.transformationMaxTimeNanos > 0);
            Assert.assertTrue(avmThreadStats.transformationMaxTimeNanos > avmThreadStats.transformationAvgTimeNanos);
            Assert.assertTrue(2 <= avmThreadStats.transformationCount);
            // Each abort can cause transformationCount to increase by at most the lower bound.
            Assert.assertTrue(2 * (avmThreadStats.concurrentResource_aborted + 1) >= avmThreadStats.transformationCount);
            // no contracts are retransformed at this step
            Assert.assertEquals(0, avmThreadStats.retransformationCount);
            Assert.assertEquals(0, avmThreadStats.retransformationAvgTimeNanos);
            Assert.assertEquals(0, avmThreadStats.retransformationMaxTimeNanos);
            
            // These assertions were made redundant by the fix in AKI-638, which makes all threads acquire the coinbase lock.
            // AKI-639 is an item that should remove the necessity of acquiring the coinbase lock. 
            // When AKI-639 is done, these assertions can be meaningful again.
            totalAcquireCount += avmThreadStats.concurrentResource_acquired;
            totalAbortCount += avmThreadStats.concurrentResource_aborted;
            Assert.assertTrue(0 <= avmThreadStats.concurrentResource_waited);
            Assert.assertTrue(0 <= avmThreadStats.concurrentResource_aborted);
        }

        // should equal number of contracts deployed * 2, since each contract deploys its own contract
        Assert.assertTrue(length * 2 <= transformationTotalCount);
        // Each abort can cause totalAcquireCount to increase by at most the lower bound.
        Assert.assertTrue((totalAbortCount + 1) * length * 2  >= transformationTotalCount);

        // We expect that the acquire count will be at least length * 7:  (deployer+contract+coinbase) + (caller+contract+newContract+coinbase).
        Assert.assertTrue(length * 7 <= totalAcquireCount);
        // Each abort can cause totalAcquireCount to increase by at most the lower bound.
        Assert.assertTrue((totalAbortCount + 1) * length * 7 >= totalAcquireCount);
        
        avm.shutdown();
    }

    @Test
    public void testDifferingGraphSizes() {
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        byte[] code = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(TestContract.class, Collections.emptyMap(), ABIDecoder.class, ABIException.class, ABIEncoder.class);

        int length = 4;
        AionAddress[] user = new AionAddress[length];
        Transaction[] ctx = new Transaction[length];
        for (int i = 0; i < user.length; i++) {
            user[i] = Helpers.randomAddress();
            kernel.adjustBalance(user[i], BigInteger.TEN.pow(20));
            ctx[i] = AvmTransactionUtil.create(user[i], BigInteger.ZERO, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 5_000_000L, 1);
        }

        FutureResult[] results = avm.run(kernel, ctx, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        AionAddress[] contractAddresses = new AionAddress[results.length];
        for (int i = 0; i < results.length; i++) {
            contractAddresses[i] = new AionAddress(results[i].getResult().copyOfTransactionOutput().orElseThrow());
        }

        // Clear the stats here since we aren't measuring the deployment.
        AvmCoreStats stats = avm.getStats();
        stats.clear();
        
        // We will increase the number of links in each instance a different number of times, thus allowing the stats on this to diverge.
        byte[] addLinkData = new ABIStreamingEncoder()
                .encodeOneString("addLink")
                .toBytes();
        int total = summation(length);
        Transaction[] tx = new Transaction[total];
        int index = 0;
        BigInteger[] nonce = new BigInteger[length];
        for (int i = 0; i < nonce.length; ++i) {
            nonce[i] = kernel.getNonce(user[i]);
        }
        for (int i = 0; i < contractAddresses.length; i++) {
            for (int j = 0; j <= i; ++j) {
                tx[index] = AvmTransactionUtil.call(user[j], contractAddresses[j], nonce[j], BigInteger.ZERO, addLinkData, 2_000_000, 1);
                index += 1;
                nonce[j] = nonce[j].add(BigInteger.ONE);
            }
        }

        results = avm.run(kernel, tx, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        for (FutureResult f : results) {
            TransactionResult result = f.getResult();
            Assert.assertTrue(result.transactionStatus.isSuccess());
        }

        AvmThreadStats[] threadStats = stats.threadStats;
        int totalAcquireCount = 0;
        int totalAbortCount = 0;

        // We don't know which thread will have the min/max, but we know that they will dirverge across the system.
        int graphMin = Integer.MAX_VALUE;
        int graphMax = 0;
        long graphSum = 0;
        int graphCount = 0;
        for (AvmThreadStats avmThreadStats : threadStats) {
            // No contracts are re-transformed at this step.
            Assert.assertEquals(0, avmThreadStats.retransformationCount);
            Assert.assertEquals(0, avmThreadStats.retransformationAvgTimeNanos);
            Assert.assertEquals(0, avmThreadStats.retransformationMaxTimeNanos);
            // There are various inter-dependencies here but no internal transactions so we can total the acquired, but that is all.
            totalAcquireCount += avmThreadStats.concurrentResource_acquired;
            totalAbortCount += avmThreadStats.concurrentResource_aborted;
            // It is possible that not every thread did work.
            if (avmThreadStats.serializedGraph_count > 0) {
                if (avmThreadStats.serializedGraph_min < graphMin) {
                    graphMin = avmThreadStats.serializedGraph_min;
                }
                if (avmThreadStats.serializedGraph_max > graphMax) {
                    graphMax = avmThreadStats.serializedGraph_max;
                }
                graphSum += avmThreadStats.serializedGraph_sum;
                graphCount += avmThreadStats.serializedGraph_count;
                Assert.assertTrue(avmThreadStats.serializedGraph_avgNanos > 0L);
            }
        }

        // We expect that the acquire count will be at least total * 3:  (sender, receiver, and coinbase).
        Assert.assertTrue(total * 3 <= totalAcquireCount);
        // Each abort can cause totalAcquireCount to increase by at most the lower bound.
        Assert.assertTrue((totalAbortCount + 1) * length * 3 >= totalAcquireCount);
        
        // We know that we will see a graph serialization for each transaction, at a minimum.
        Assert.assertTrue(total <= graphCount);
        // Each abort can cause totalAcquireCount to increase by at most the lower bound.
        Assert.assertTrue((totalAbortCount + 1) * total >= graphCount);
        
        // Also, enforce the relationships of the sizing (which we know must diverge since they _are_ different sizes).
        int graphAvg = (int)(graphSum / (long)graphCount);
        Assert.assertTrue(graphMin < graphAvg);
        Assert.assertTrue(graphAvg < graphMax);
        
        avm.shutdown();
    }

    @Test
    public void testCacheUsage() {
        TestingState kernel = new TestingState(block);
        AvmConfiguration config = new AvmConfiguration();
        config.threadCount = 4;
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);

        byte[] code = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(TestContract.class, Collections.emptyMap(), ABIDecoder.class, ABIException.class, ABIEncoder.class);

        AionAddress user = Helpers.randomAddress();
        BigInteger nonce = BigInteger.ZERO;
        kernel.adjustBalance(user, BigInteger.TEN.pow(20));
        Transaction create = AvmTransactionUtil.create(user, nonce, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 5_000_000L, 1);
        nonce = nonce.add(BigInteger.ONE);

        kernel.generateBlock();
        FutureResult result = avm.run(kernel, new Transaction[] {create}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0];
        AionAddress contractAddress = new AionAddress(result.getResult().copyOfTransactionOutput().orElseThrow());

        // Clear the stats here since we aren't measuring the deployment.
        AvmCoreStats stats = avm.getStats();
        AvmThreadStats[] threadStats = stats.threadStats;
        verifyCacheStats(threadStats, 0, 0, 0, 0, 0, 0);
        stats.clear();
        
        // We will increase the number of links in each instance a different number of times, thus allowing the stats on this to diverge.
        byte[] addValueData = new ABIStreamingEncoder()
                .encodeOneString("addValue")
                .toBytes();
        for (int i = 0; i < 1; ++i) {
            Transaction call = AvmTransactionUtil.call(user, contractAddress, nonce, BigInteger.ZERO, addValueData, 2_000_000, 1);
            nonce = nonce.add(BigInteger.ONE);
            kernel.generateBlock();
            result = avm.run(kernel, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0];
            Assert.assertTrue(result.getResult().transactionStatus.isSuccess());
            
            // Note that we don't currently populate the code cache or the data cache on deployment, only the first call after that.
            boolean isFirstCall = (0 == i);
            verifyCacheStats(threadStats
                    , isFirstCall ? 0 : 1
                    , isFirstCall ? 1 : 0
                    , 0
                    , isFirstCall ? 0 : 1
                    , isFirstCall ? 1 : 0
                    , 0
            );
            stats.clear();
        }
        
        // Finally, do a reentrant call.
        byte[] doCallThisData = new ABIStreamingEncoder()
                .encodeOneString("doCallThis")
                .encodeOneByteArray(addValueData)
                .toBytes();
        Transaction call = AvmTransactionUtil.call(user, contractAddress, nonce, BigInteger.ZERO, doCallThisData, 2_000_000, 1);
        nonce = nonce.add(BigInteger.ONE);
        kernel.generateBlock();
        result = avm.run(kernel, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0];
        Assert.assertTrue(result.getResult().transactionStatus.isSuccess());
        verifyCacheStats(threadStats
                , 1
                , 0
                , 1
                , 1
                , 0
                , 1
        );
        stats.clear();
        
        avm.shutdown();
    }


    private static int summation(int number) {
        int total = 0;
        for (int i = 1; i <= number; ++i) {
            total += i;
        }
        return total;
    }

    private void verifyCacheStats(AvmThreadStats[] threadStats
            , int expectedCodeHit
            , int expectedCodeMiss
            , int expectedCodeReentrant
            , int expectedDataHit
            , int expectedDataMiss
            , int expectedDataReentrant
    ) {
        int codeCacheHit = 0;
        int codeCacheMiss = 0;
        int codeCacheReentrant = 0;
        int dataCacheHit = 0;
        int dataCacheMiss = 0;
        int dataCacheReentrant = 0;
        
        for (AvmThreadStats avmThreadStats : threadStats) {
            codeCacheHit += avmThreadStats.cache_code_hit;
            codeCacheMiss += avmThreadStats.cache_code_miss;
            codeCacheReentrant += avmThreadStats.cache_code_reentrant;
            dataCacheHit += avmThreadStats.cache_data_hit;
            dataCacheMiss += avmThreadStats.cache_data_miss;
            dataCacheReentrant += avmThreadStats.cache_data_reentrant;
        }
        Assert.assertEquals(expectedCodeHit, codeCacheHit);
        Assert.assertEquals(expectedCodeMiss, codeCacheMiss);
        Assert.assertEquals(expectedCodeReentrant, codeCacheReentrant);
        Assert.assertEquals(expectedDataHit, dataCacheHit);
        Assert.assertEquals(expectedDataMiss, dataCacheMiss);
        Assert.assertEquals(expectedDataReentrant, dataCacheReentrant);
    }
}

package org.aion.avm.core;

import avm.Address;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
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

        byte[] code = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(TestContract.class);

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
        }

        // should equal number of contracts deployed
        Assert.assertEquals(length, transformationTotalCount);

        for (AionAddress address : contractAddresses) {
            kernel.setTransformedCode(address, null);
        }

        stats.clear();

        // each call will re-transform the code
        for (int i = 0; i < length - 1; i++) {
            byte[] getCallCount = new ABIStreamingEncoder().encodeOneString("getCallCount").toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], kernel.getNonce(user[i]), BigInteger.ZERO, getCallCount, 5_000_000, 1);
        }

        results = avm.run(kernel, tx, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1);
        for (FutureResult f : results) {
            Assert.assertEquals(2, new ABIDecoder(f.getResult().copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
        }

        for (AvmThreadStats avmThreadStats : threadStats) {
            retransformationTotalCount += avmThreadStats.retransformationCount;
            Assert.assertEquals(0, avmThreadStats.transformationAvgTimeNanos);
            Assert.assertEquals(0, avmThreadStats.transformationMaxTimeNanos);
            Assert.assertEquals(0, avmThreadStats.transformationCount);

            Assert.assertTrue(avmThreadStats.retransformationAvgTimeNanos > 0);
            Assert.assertTrue(avmThreadStats.retransformationMaxTimeNanos > avmThreadStats.retransformationAvgTimeNanos);
        }

        Assert.assertEquals(length - 1, retransformationTotalCount);
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

        for (AvmThreadStats avmThreadStats : threadStats) {
            transformationTotalCount += avmThreadStats.transformationCount;
            Assert.assertTrue(avmThreadStats.transformationAvgTimeNanos > 0);
            Assert.assertTrue(avmThreadStats.transformationMaxTimeNanos > 0);
            Assert.assertTrue(avmThreadStats.transformationMaxTimeNanos > avmThreadStats.transformationAvgTimeNanos);
            Assert.assertEquals(2, avmThreadStats.transformationCount);
            // no contracts are retransformed at this step
            Assert.assertEquals(0, avmThreadStats.retransformationCount);
            Assert.assertEquals(0, avmThreadStats.retransformationAvgTimeNanos);
            Assert.assertEquals(0, avmThreadStats.retransformationMaxTimeNanos);
        }

        // should equal number of contracts deployed * 2, since each contract deploys its own contract
        Assert.assertEquals(length * 2, transformationTotalCount);

        avm.shutdown();
    }
}

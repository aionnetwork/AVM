package org.aion.avm.tooling;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Log;
import org.aion.kernel.SideEffects;
import org.aion.types.AionAddress;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.LogSizeUtils;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingKernel;
import org.aion.kernel.TestingTransaction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ContractLoggingTest {
    private static final int NUM_LOGS = 5;

    private static AionAddress from = TestingKernel.PREMINED_ADDRESS;
    private static long energyLimit = 5_000_000L;
    private static long energyPrice = 1;

    private static TestingKernel kernel;
    private static AvmImpl avm;
    private static AionAddress contract;

    private List<Integer> counts = new ArrayList<>();

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingKernel(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new StandardCapabilities(), new AvmConfiguration());
        deployContract();
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testLogs() {
        TestingTransaction transaction = generateTxForMethodCall("hitLogs");
        AvmTransactionResult result = runTransaction(transaction);
        assertTrue(result.getResultCode().isSuccess());

        SideEffects sideEffects = result.getSideEffects();
        assertEquals(NUM_LOGS, sideEffects.getExecutionLogs().size());
        assertEquals(0, sideEffects.getInternalTransactions().size());

        verifyLogs(sideEffects.getExecutionLogs(), 1);
    }

    @Test
    public void testLogsFireOffInDeepestInternalTransaction() {
        TestingTransaction transaction = generateTxForMethodCall("spawnInternalTransactionsAndHitLogsAtBottomLevel", 9);
        AvmTransactionResult result = runTransaction(transaction);
        assertTrue(result.getResultCode().isSuccess());

        SideEffects sideEffects = result.getSideEffects();
        assertEquals(NUM_LOGS, sideEffects.getExecutionLogs().size());
        assertEquals(9, sideEffects.getInternalTransactions().size());

        verifyLogs(sideEffects.getExecutionLogs(), 1);
    }

    @Test
    public void testLogsFiredOffInEachInternalTransaction() {
        int depth = 9;

        TestingTransaction transaction = generateTxForMethodCall("spawnInternalTransactionsAndHitLogsAtEachLevel", depth);
        AvmTransactionResult result = runTransaction(transaction);
        assertTrue(result.getResultCode().isSuccess());

        SideEffects sideEffects = result.getSideEffects();
        assertEquals(NUM_LOGS * (depth + 1), sideEffects.getExecutionLogs().size());
        assertEquals(depth, sideEffects.getInternalTransactions().size());

        verifyLogs(sideEffects.getExecutionLogs(), depth + 1);
    }

    @Test
    public void testLogsFiredOffInEachInternalTransactionUptoFive() {
        TestingTransaction transaction = generateTxForMethodCall("spawnInternalTransactionsAndFailAtDepth5", 9);
        AvmTransactionResult result = runTransaction(transaction);
        assertTrue(result.getResultCode().isSuccess());

        SideEffects sideEffects = result.getSideEffects();
        assertEquals(NUM_LOGS * 5, sideEffects.getExecutionLogs().size());
        assertEquals(5, sideEffects.getInternalTransactions().size());

        verifyLogs(sideEffects.getExecutionLogs(), 5);
    }

    /**
     * Checks that each of the logs is in its expected state and that it has been generated the
     * appropriate number of times.
     *
     * If anything fails to check out here the calling test will fail.
     */
    private void verifyLogs(List<Log> logs, int numCallsToHitLogs) {
        resetCounters();
        for (Log log : logs) {
            verifyLog(log);
        }
        verifyCounts(numCallsToHitLogs);
    }

    /**
     * Checks that each of the counters is equal to numCalls.
     */
    private void verifyCounts(int numCalls) {
        for (Integer count : this.counts) {
            assertEquals(numCalls, count.intValue());
        }
    }

    /**
     * Verifies that log is one of the 5 possible logging calls in the contract.
     */
    private void verifyLog(Log log) {
        assertEquals(contract, log.getSourceAddress());
        switch (log.getTopics().size()) {
            case 0:
                assertArrayEquals(LoggingTarget.DATA1, log.getData());
                incrementCounter(0);
                break;
            case 1:
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC1), log.getTopics().get(0));
                assertArrayEquals(LoggingTarget.DATA2, log.getData());
                incrementCounter(1);
                break;
            case 2:
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC1), log.getTopics().get(0));
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC2), log.getTopics().get(1));
                assertArrayEquals(LoggingTarget.DATA3, log.getData());
                incrementCounter(2);
                break;
            case 3:
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC1), log.getTopics().get(0));
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC2), log.getTopics().get(1));
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC3), log.getTopics().get(2));
                assertArrayEquals(LoggingTarget.DATA4, log.getData());
                incrementCounter(3);
                break;
            case 4:
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC1), log.getTopics().get(0));
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC2), log.getTopics().get(1));
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC3), log.getTopics().get(2));
                assertArrayEquals(LogSizeUtils.truncatePadTopic(LoggingTarget.TOPIC4), log.getTopics().get(3));
                assertArrayEquals(LoggingTarget.DATA5, log.getData());
                incrementCounter(4);
                break;
            default:
                fail("Log topic size should be in the range [0,4] but was: " + log.getTopics().size());
        }
    }

    private static void deployContract() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(LoggingTarget.class);
        jar = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        TestingTransaction transaction = TestingTransaction.create(from, kernel.getNonce(from), BigInteger.ZERO, jar, energyLimit, energyPrice);
        AvmTransactionResult result = avm.run(ContractLoggingTest.kernel, new TestingTransaction[] {transaction})[0].get();

        assertTrue(result.getResultCode().isSuccess());
        contract = new AionAddress(result.getReturnData());
    }

    private AvmTransactionResult runTransaction(TestingTransaction tx) {
        return avm.run(ContractLoggingTest.kernel, new TestingTransaction[] {tx})[0].get();
    }

    private TestingTransaction generateTxForMethodCall(String methodName, Object... args) {
        kernel.generateBlock();
        byte[] callData = ABIUtil.encodeMethodArguments(methodName, args);
        return TestingTransaction.call(from, contract, kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
    }

    private void resetCounters() {
        this.counts.clear();
        for (int i = 0; i < 5; i++) {
            this.counts.add(0);
        }
    }

    private void incrementCounter(int position) {
        this.counts.add(position, this.counts.remove(position) + 1);
    }

}

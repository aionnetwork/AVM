package org.aion.avm.core.performance;

import org.aion.avm.core.*;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

public class PerformanceTest {
    // NOTE:  Output is ONLY produced if REPORT is set to true.
    private static final boolean REPORT = false;

    private TestingState kernel;
    private AvmImpl avm;

    private static final int transactionBlockSize = 10;
    private static final int contextNum = 3;
    // We want to use the same number for single and batched calls.
    private static final int userDappNum = transactionBlockSize * contextNum;
    private static final int heavyLevel = 1;
    private static final int allocSize = (1 * (1 << 20));
    private static long energyLimit = 1_000_000_000_000_000l;
    private static long energyPrice = 1l;

    private AionAddress[] userAddrs = new AionAddress[userDappNum];
    private AionAddress[] contractAddrs = new AionAddress[userDappNum];

    @Before
    public void setup() {
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingState(block);
        deploy();
    }

    /**
     * Creating a lot of random userAddrs and the same number of dapps of performance test.
     */
    public void deploy() {
        long startTime = System.currentTimeMillis();

        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(PerformanceTestTarget.class);

        byte[] args = ABIEncoder.encodeOneIntegerArray(new int[] { heavyLevel, allocSize });
        byte[] txData = new CodeAndArguments(jar, args).encodeToBytes();

        // Deploy
        for(int i = 0; i < userDappNum; ++i) {
            //creating users
            AionAddress userAddress = Helpers.randomAddress();
            kernel.createAccount(userAddress);
            kernel.adjustBalance(userAddress, BigInteger.TEN.pow(18));
            userAddrs[i] = userAddress;

            //deploying dapp
            Transaction create = AvmTransactionUtil.create(userAddress, kernel.getNonce(userAddress), BigInteger.ZERO, txData, energyLimit, energyPrice);
            TransactionResult createResult = avm.run(this.kernel, new Transaction[]{create}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
            Assert.assertTrue(createResult.transactionStatus.isSuccess());
            AionAddress contractAddr = new AionAddress(createResult.copyOfTransactionOutput().orElseThrow());
            contractAddrs[i] = contractAddr;
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        if (REPORT) {
            System.out.printf("deploy: %d ms\n", timeElapsed);
        }
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testPerformanceCpuNto1Single() throws Exception {
        performanceTestSingle("cpuHeavy", true, "testPerformanceCpuNto1Single");
    }

    @Test
    public void testPerformanceCpuNtoNSingle() throws Exception {
        performanceTestSingle("cpuHeavy", false, "testPerformanceCpuNtoNSingle");
    }

    @Test
    public void testPerformanceMemoryNto1Single() throws Exception {
        performanceTestSingle("memoryHeavy", true, "testPerformanceMemoryNto1Single");
    }

    @Test
    public void testPerformanceMemoryNtoNSingle() throws Exception {
        performanceTestSingle("memoryHeavy", false, "testPerformanceMemoryNtoNSingle");
    }

    public void performanceTestSingle(String methodName, boolean Nto1, String testName) {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < userDappNum; ++i) {
            callSingle(userAddrs[i], Nto1 ? contractAddrs[0] : contractAddrs[i], methodName);
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        if (REPORT) {
            System.out.printf("%s: %d ms\n", testName, timeElapsed);
        }
    }

    private void callSingle(AionAddress sender, AionAddress contractAddr, String methodName) {
        byte[] argData = new ABIStreamingEncoder().encodeOneString(methodName).toBytes();
        Transaction call = AvmTransactionUtil.call(sender, contractAddr, kernel.getNonce(sender), BigInteger.ZERO, argData, energyLimit, energyPrice);
        TransactionResult result = avm.run(this.kernel, new Transaction[] {call}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * One transaction context contains a lot of transactions.
     */
    @Test
    public void testPerformanceCpuNto1Batch() throws Exception {
        performanceBatch("cpuHeavy", true, "testPerformanceCpuNto1Batch");
    }

    @Test
    public void testPerformanceCpuNtoNBatch() throws Exception {
        performanceBatch("cpuHeavy",false, "testPerformanceCpuNtoNBatch");
    }

    @Test
    public void testPerformanceMemoryNto1Batch() throws Exception {
        performanceBatch("memoryHeavy", true, "testPerformanceMemoryNto1Batch");
    }

    @Test
    public void testPerformanceMemoryNtoNBatch() throws Exception {
        performanceBatch("memoryHeavy", false, "testPerformanceMemoryNtoNBatch");
    }

    public void performanceBatch(String methodName, boolean Nto1, String testName) {
        long startTime = System.currentTimeMillis();

        callBatch(methodName, Nto1);

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        if (REPORT) {
            System.out.printf("%s: %d ms\n", testName, timeElapsed);
        }
    }

    public void callBatch(String methodName, boolean Nto1){
        byte[] argData = new ABIStreamingEncoder().encodeOneString(methodName).toBytes();
        for(int j = 0; j < contextNum; ++j) {
            Transaction[] transactionArray = new Transaction[transactionBlockSize];
            for (int i = 0; i < transactionBlockSize; ++i) {
                AionAddress sender = userAddrs[i];
                AionAddress contractAddr = Nto1 ? contractAddrs[0] : contractAddrs[i];
                transactionArray[i] = AvmTransactionUtil.call(sender, contractAddr, kernel.getNonce(sender), BigInteger.ZERO, argData, energyLimit, energyPrice);
            }
            FutureResult[] futures = avm.run(this.kernel, transactionArray, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1);
            for (FutureResult future : futures) {
                TransactionResult result = future.getResult();
                Assert.assertTrue(result.transactionStatus.isSuccess());
                // These should all return an empty byte[] (void).
                Assert.assertEquals(0, result.copyOfTransactionOutput().orElseThrow().length);
            }
        }
    }
}

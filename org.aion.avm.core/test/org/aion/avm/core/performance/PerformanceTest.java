package org.aion.avm.core.performance;

import avm.Address;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.SimpleFuture;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

public class PerformanceTest {
    private TestingKernel kernel;
    private AvmImpl avm;
    Block block;

    private static final int transactionBlockSize = 10;
    private static final int contextNum = 3;
    // We want to use the same number for single and batched calls.
    private static final int userDappNum = transactionBlockSize * contextNum;
    private static final int heavyLevel = 1;
    private static final int allocSize = (1 * (1 << 20));
    private static long energyLimit = 1_000_000_000_000_000l;
    private static long energyPrice = 1l;

    private org.aion.types.Address[] userAddrs = new org.aion.types.Address[userDappNum];
    private Address[] contractAddrs = new Address[userDappNum];

    @Before
    public void setup() {
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingKernel(block);
        deploy();
    }

    /**
     * Creating a lot of random userAddrs and the same number of dapps of performance test.
     */
    public void deploy() {
        long startTime = System.currentTimeMillis();

        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(PerformanceTestTarget.class);

        byte[] args = ABIEncoder.encodeOneIntegerArray(new int[] { heavyLevel, allocSize });
        byte[] txData = new CodeAndArguments(jar, args).encodeToBytes();

        // Deploy
        for(int i = 0; i < userDappNum; ++i) {
            //creating users
            org.aion.types.Address userAddress = Helpers.randomAddress();
            kernel.createAccount(userAddress);
            kernel.adjustBalance(userAddress, BigInteger.TEN.pow(18));
            userAddrs[i] = userAddress;

            //deploying dapp
            Transaction create = Transaction.create(userAddress, kernel.getNonce(userAddress), BigInteger.ZERO, txData, energyLimit, energyPrice);
            AvmTransactionResult createResult = (AvmTransactionResult) avm.run(this.kernel, new Transaction[]{create})[0].get();
            Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
            Address contractAddr = new Address(createResult.getReturnData());
            contractAddrs[i] = contractAddr;
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.printf("deploy: %d ms\n", timeElapsed);
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
            callSingle(userAddrs[i], block, Nto1 ? contractAddrs[0] : contractAddrs[i], methodName);
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.printf("%s: %d ms\n", testName, timeElapsed);
    }

    private void callSingle(org.aion.types.Address sender, Block block, Address contractAddr, String methodName) {
        byte[] argData = ABIUtil.encodeMethodArguments(methodName);
        Transaction call = Transaction.call(sender, org.aion.types.Address.wrap(contractAddr.unwrap()), kernel.getNonce(sender), BigInteger.ZERO, argData, energyLimit, energyPrice);
        AvmTransactionResult result = (AvmTransactionResult) avm.run(this.kernel, new Transaction[] {call})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
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

        callBatch(methodName, block, Nto1);

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.printf("%s: %d ms\n", testName, timeElapsed);
    }

    public void callBatch(String methodName, Block block, boolean Nto1){
        byte[] argData = ABIUtil.encodeMethodArguments(methodName);
        for(int j = 0; j < contextNum; ++j) {
            Transaction[] transactionArray = new Transaction[transactionBlockSize];
            for (int i = 0; i < transactionBlockSize; ++i) {
                org.aion.types.Address sender = userAddrs[i];
                Address contractAddr = Nto1 ? contractAddrs[0] : contractAddrs[i];
                transactionArray[i] = Transaction.call(sender, org.aion.types.Address.wrap(contractAddr.unwrap()), kernel.getNonce(sender), BigInteger.ZERO, argData, energyLimit, energyPrice);
            }
            SimpleFuture<TransactionResult>[] futures = avm.run(this.kernel, transactionArray);
            for (SimpleFuture<TransactionResult> future : futures) {
                AvmTransactionResult result = (AvmTransactionResult) future.get();
                Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
                ABIUtil.decodeOneObject(result.getReturnData());
            }
        }
    }
}

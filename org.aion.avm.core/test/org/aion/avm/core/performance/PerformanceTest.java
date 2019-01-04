package org.aion.avm.core.performance;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.kernel.AvmAddress;
import org.aion.kernel.AvmTransactionResult;
import org.aion.kernel.Block;
import org.aion.kernel.KernelInterfaceImpl;
import org.aion.kernel.Transaction;
import org.aion.kernel.TransactionContextImpl;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.VirtualMachine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PerformanceTest {
    private KernelInterfaceImpl kernel;
    private VirtualMachine avm;
    Block block;

    private static final int userDappNum = 30;
    private static final int heavyLevel = 1;
    private static final int allocSize = (1 * (1 << 20));

    private org.aion.vm.api.interfaces.Address[] userAddrs = new org.aion.vm.api.interfaces.Address[userDappNum];
    private Address[] contractAddrs = new Address[userDappNum];

    @Before
    public void setup() {
        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        deploy();
    }

    /**
     * Creating a lot of random userAddrs and the same number of dapps of performance test.
     */
    public void deploy() {
        long startTime = System.currentTimeMillis();

        byte[] jar = JarBuilder.buildJarForMainAndClasses(PerformanceTestTarget.class);

        byte[] args = ABIEncoder.encodeOneObject(new int[] { heavyLevel, allocSize });
        byte[] txData = new CodeAndArguments(jar, args).encodeToBytes();

        // Deploy
        long energyLimit = 1_000_000l;
        long energyPrice = 1l;
        for(int i = 0; i < userDappNum; ++i) {
            //creating users
            org.aion.vm.api.interfaces.Address userAddress = Helpers.randomAddress();
            kernel.createAccount(userAddress);
            kernel.adjustBalance(userAddress, BigInteger.TEN.pow(18));
            userAddrs[i] = userAddress;

            //deploying dapp
            Transaction create = Transaction.create(userAddress, kernel.getNonce(userAddress).longValue(), BigInteger.ZERO, txData, energyLimit, energyPrice);
            AvmTransactionResult createResult = (AvmTransactionResult) avm.run(new TransactionContext[]{new TransactionContextImpl(create, block)})[0].get();
            Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, createResult.getResultCode());
            Address contractAddr = TestingHelper.buildAddress(createResult.getReturnData());
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
    public void testPerformanceCpuNto1() throws Exception {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < userDappNum; ++i) {
            call(userAddrs[i], block, contractAddrs[0], "cpuHeavy");
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.printf("testPerformanceCpuNto1: %d ms\n", timeElapsed);
    }

    @Test
    public void testPerformanceCpuNtoN() throws Exception {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < userDappNum; ++i) {
            call(userAddrs[i], block, contractAddrs[i], "cpuHeavy");
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.printf("testPerformanceCpuNtoN: %d ms\n", timeElapsed);
    }

    @Test
    public void testPerformanceMemoryNto1() throws Exception {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < userDappNum; ++i) {
            call(userAddrs[i], block, contractAddrs[0], "memoryHeavy");
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.printf("testPerformanceMemoryNto1: %d ms\n", timeElapsed);
    }

    @Test
    public void testPerformanceMemoryNtoN() throws Exception {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < userDappNum; ++i) {
            call(userAddrs[i], block, contractAddrs[0], "memoryHeavy");
        }

        long endTime = System.currentTimeMillis();
        long timeElapsed = endTime - startTime;
        System.out.printf("testPerformanceMemoryNtoN: %d ms\n", timeElapsed);
    }

    private void call(org.aion.vm.api.interfaces.Address sender, Block block, Address contractAddr, String methodName) {
        long energyLimit = 1_000_000_000_000_000l;
        long energyPrice = 1l;
        byte[] argData = ABIEncoder.encodeMethodArguments(methodName);
        Transaction call = Transaction.call(sender, AvmAddress.wrap(contractAddr.unwrap()), kernel.getNonce(sender).longValue(), BigInteger.ZERO, argData, energyLimit, energyPrice);
        AvmTransactionResult result = (AvmTransactionResult) avm.run(new TransactionContext[] {new TransactionContextImpl(call, block)})[0].get();
        Assert.assertEquals(AvmTransactionResult.Code.SUCCESS, result.getResultCode());
    }
}

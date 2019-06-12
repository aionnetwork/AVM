package org.aion.parallel;

import java.math.BigInteger;
import org.aion.types.AionAddress;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.SimpleFuture;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Test;

public class AvmParallelTest {

    private AionAddress preminedAddress = TestingKernel.PREMINED_ADDRESS;

    private TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    @Test
    public void basicConcurrencyTest(){
        TestingKernel kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AionAddress usr1 = new AionAddress(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        BigInteger expected1 = BigInteger.ZERO;

        TestingTransaction t0 = TestingTransaction.call(preminedAddress, usr1, BigInteger.ZERO, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        TestingTransaction t1 = TestingTransaction.call(preminedAddress, usr1, BigInteger.ONE, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        TestingTransaction t2 = TestingTransaction.call(preminedAddress, usr1, BigInteger.TWO, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        TestingTransaction t3 = TestingTransaction.call(preminedAddress, usr1, BigInteger.valueOf(3), BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        TestingTransaction t4 = TestingTransaction.call(preminedAddress, usr1, BigInteger.valueOf(4), BigInteger.valueOf(500_000), new byte[0], 100000L, 1);

        TestingTransaction[] batch = new TestingTransaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<TransactionResult>[] results = avm.run(kernel, batch);
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        expected1 = expected1.add(BigInteger.valueOf(500_000 * 5));

        Assert.assertEquals(expected1, kernel.getBalance(usr1));

        AionAddress usr2 = new AionAddress(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        BigInteger expected2 = BigInteger.ZERO;

        t0 = TestingTransaction.call(usr1, usr2, BigInteger.ZERO, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t1 = TestingTransaction.call(usr1, usr2, BigInteger.ONE, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t2 = TestingTransaction.call(usr1, usr2, BigInteger.TWO, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t3 = TestingTransaction.call(usr1, usr2, BigInteger.valueOf(3), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t4 = TestingTransaction.call(usr1, usr2, BigInteger.valueOf(4), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);

        batch = new TestingTransaction[]{t0, t1, t2, t3, t4};
        results = avm.run(kernel, batch);
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        expected1 = expected1.subtract(BigInteger.valueOf(100_000 * 5)).subtract(BigInteger.valueOf(t1.getTransactionCost() * 5));
        expected2 = BigInteger.valueOf(100_000 * 5);

        Assert.assertEquals(expected1, kernel.getBalance(usr1));
        Assert.assertEquals(expected2, kernel.getBalance(usr2));

        t0 = TestingTransaction.call(usr1, usr2, BigInteger.ZERO, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t1 = TestingTransaction.call(usr2, usr1, BigInteger.ONE, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);

        int iteration = 10;
        batch = new TestingTransaction[iteration * 2];
        for (int i = 0; i < iteration; i++){
            batch[i * 2]     = TestingTransaction.call(usr1, usr2, BigInteger.valueOf(5 + i), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
            batch[i * 2 + 1] = TestingTransaction.call(usr2, usr1, BigInteger.valueOf(i), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        }

        results = avm.run(kernel, batch);
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        expected1 = expected1.subtract(BigInteger.valueOf(t0.getTransactionCost() * iteration));
        expected2 = expected2.subtract(BigInteger.valueOf(t1.getTransactionCost() * iteration));

        Assert.assertEquals(expected1, kernel.getBalance(usr1));
        Assert.assertEquals(expected2, kernel.getBalance(usr2));

        avm.shutdown();
    }

    @Test
    public void cyclicWaitTest(){

        TestingKernel kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AionAddress usr1 = new AionAddress(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        AionAddress usr2 = new AionAddress(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        AionAddress usr3 = new AionAddress(Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333"));
        AionAddress usr4 = new AionAddress(Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444"));

        TestingTransaction t0 = TestingTransaction.call(preminedAddress, usr1, BigInteger.ZERO, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        TestingTransaction t1 = TestingTransaction.call(usr1, usr2, BigInteger.ZERO, BigInteger.valueOf(4_000_000), new byte[0], 100000L, 1);
        TestingTransaction t2 = TestingTransaction.call(usr2, usr3, BigInteger.ZERO, BigInteger.valueOf(3_000_000), new byte[0], 100000L, 1);
        TestingTransaction t3 = TestingTransaction.call(usr3, usr4, BigInteger.ZERO, BigInteger.valueOf(2_000_000), new byte[0], 100000L, 1);
        TestingTransaction t4 = TestingTransaction.call(usr4, usr1, BigInteger.ZERO, BigInteger.valueOf(1_000_000), new byte[0], 100000L, 1);

        TestingTransaction[] batch = new TestingTransaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<TransactionResult>[] results = avm.run(kernel, batch);
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        avm.shutdown();
    }

    @Test
    public void internalTransactionTest(){

        byte[] code = JarBuilder.buildJarForMainAndClassesAndUserlib(TestContract.class);

        TestingKernel kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        AionAddress usr1 = new AionAddress(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        AionAddress usr2 = new AionAddress(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        AionAddress usr3 = new AionAddress(Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333"));
        AionAddress usr4 = new AionAddress(Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444"));

        TestingTransaction t0 = TestingTransaction.call(preminedAddress, usr1, BigInteger.ZERO, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        TestingTransaction t1 = TestingTransaction.call(preminedAddress, usr2, BigInteger.ONE, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        TestingTransaction t2 = TestingTransaction.call(preminedAddress, usr3, BigInteger.TWO, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        TestingTransaction t3 = TestingTransaction.call(preminedAddress, usr4, BigInteger.valueOf(3), BigInteger.valueOf(15_000_000), new byte[0], 100000L, 1);
        TestingTransaction t4 = TestingTransaction.create(usr4, BigInteger.ZERO, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 10_000_000L, 1);

        TestingTransaction[] batch = new TestingTransaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<TransactionResult>[] results = avm.run(kernel, batch);
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        TransactionResult res = results[4].get();
        AionAddress contractAddr = new AionAddress(res.getReturnData());

        byte[] args = encodeNoArgsMethodCall("doTransfer");
        byte[] args2 = encodeNoArgsMethodCall("addValue");

        t0 = TestingTransaction.call(preminedAddress, contractAddr, BigInteger.valueOf(4), BigInteger.valueOf(5_000_000), args2, 100000L, 1);
        t1 = TestingTransaction.call(usr1, contractAddr, BigInteger.ZERO, BigInteger.ZERO, args, 200000L, 1);
        t2 = TestingTransaction.call(usr2, contractAddr, BigInteger.ZERO, BigInteger.ZERO, args, 200000L, 1);
        t3 = TestingTransaction.call(usr3, contractAddr, BigInteger.ZERO, BigInteger.ZERO, args, 200000L, 1);
        t4 = TestingTransaction.call(usr4, contractAddr, BigInteger.ONE, BigInteger.ZERO, args, 200000L, 1);

        batch = new TestingTransaction[]{t0, t1, t2, t3, t4};
        results = avm.run(kernel, batch);
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        avm.shutdown();
    }

    /**
     * This test attempts to transfer to 100 users and then from those to 1 user.  This will cause heavy abort behaviour because this is a data hazard.
     */
    @Test
    public void heavyAbortTest(){
        TestingKernel kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        
        // We will send 2x the value to these accounts, initially, and they will send 1x to the target.
        int iterations = 100;
        long valueToSend = 1_000_000L;
        AionAddress targetUser = Helpers.randomAddress();
        AionAddress[] tempUsers = new AionAddress[iterations];
        
        // First batch - disperse funds.
        TestingTransaction[] firstBatch = new TestingTransaction[iterations];
        for (int i = 0; i < iterations; ++i) {
            tempUsers[i] = Helpers.randomAddress();
            firstBatch[i] = TestingTransaction.call(preminedAddress, tempUsers[i], BigInteger.valueOf(i), BigInteger.valueOf(2L * valueToSend), new byte[0], 100_000L, 1L);
        }
        SimpleFuture<TransactionResult>[] results = avm.run(kernel, firstBatch);
        for (SimpleFuture<TransactionResult> f : results){
            Assert.assertTrue(f.get().getResultCode().isSuccess());
        }
        
        // Second batch - collect funds.
        TestingTransaction[] secondBatch = new TestingTransaction[iterations];
        for (int i = 0; i < iterations; ++i) {
            secondBatch[i] = TestingTransaction.call(tempUsers[i], targetUser, BigInteger.ZERO, BigInteger.valueOf(valueToSend), new byte[0], 100_000L, 1L);
        }
        results = avm.run(kernel, secondBatch);
        for (SimpleFuture<TransactionResult> f : results){
            Assert.assertTrue(f.get().getResultCode().isSuccess());
        }
        
        // Check that this had the expected result.
        Assert.assertEquals(BigInteger.valueOf((long)iterations * valueToSend), kernel.getBalance(targetUser));
        avm.shutdown();
    }


    private static byte[] encodeNoArgsMethodCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }
}

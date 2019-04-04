package org.aion.parallel;

import java.math.BigInteger;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.ABIUtil;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.SimpleFuture;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Test;

public class AvmParallelTest {

    private org.aion.types.Address preminedAddress = TestingKernel.PREMINED_ADDRESS;

    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    @Test
    public void basicConcurrencyTest(){
        TestingKernel kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        org.aion.types.Address usr1 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        BigInteger expected1 = BigInteger.ZERO;

        Transaction t0 = Transaction.call(preminedAddress, usr1, BigInteger.ZERO, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t1 = Transaction.call(preminedAddress, usr1, BigInteger.ONE, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t2 = Transaction.call(preminedAddress, usr1, BigInteger.TWO, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t3 = Transaction.call(preminedAddress, usr1, BigInteger.valueOf(3), BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t4 = Transaction.call(preminedAddress, usr1, BigInteger.valueOf(4), BigInteger.valueOf(500_000), new byte[0], 100000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<TransactionResult>[] results = avm.run(kernel, batch);
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        expected1 = expected1.add(BigInteger.valueOf(500_000 * 5));

        Assert.assertEquals(expected1, kernel.getBalance(usr1));

        org.aion.types.Address usr2 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        BigInteger expected2 = BigInteger.ZERO;

        t0 = Transaction.call(usr1, usr2, BigInteger.ZERO, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t1 = Transaction.call(usr1, usr2, BigInteger.ONE, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t2 = Transaction.call(usr1, usr2, BigInteger.TWO, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t3 = Transaction.call(usr1, usr2, BigInteger.valueOf(3), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t4 = Transaction.call(usr1, usr2, BigInteger.valueOf(4), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);

        batch = new Transaction[]{t0, t1, t2, t3, t4};
        results = avm.run(kernel, batch);
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        expected1 = expected1.subtract(BigInteger.valueOf(100_000 * 5)).subtract(BigInteger.valueOf(t1.getTransactionCost() * 5));
        expected2 = BigInteger.valueOf(100_000 * 5);

        Assert.assertEquals(expected1, kernel.getBalance(usr1));
        Assert.assertEquals(expected2, kernel.getBalance(usr2));

        t0 = Transaction.call(usr1, usr2, BigInteger.ZERO, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t1 = Transaction.call(usr2, usr1, BigInteger.ONE, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);

        int iteration = 10;
        batch = new Transaction[iteration * 2];
        for (int i = 0; i < iteration; i++){
            batch[i * 2]     = Transaction.call(usr1, usr2, BigInteger.valueOf(5 + i), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
            batch[i * 2 + 1] = Transaction.call(usr2, usr1, BigInteger.valueOf(i), BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
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

        org.aion.types.Address usr1 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        org.aion.types.Address usr2 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        org.aion.types.Address usr3 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333"));
        org.aion.types.Address usr4 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444"));

        Transaction t0 = Transaction.call(preminedAddress, usr1, BigInteger.ZERO, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t1 = Transaction.call(usr1, usr2, BigInteger.ZERO, BigInteger.valueOf(4_000_000), new byte[0], 100000L, 1);
        Transaction t2 = Transaction.call(usr2, usr3, BigInteger.ZERO, BigInteger.valueOf(3_000_000), new byte[0], 100000L, 1);
        Transaction t3 = Transaction.call(usr3, usr4, BigInteger.ZERO, BigInteger.valueOf(2_000_000), new byte[0], 100000L, 1);
        Transaction t4 = Transaction.call(usr4, usr1, BigInteger.ZERO, BigInteger.valueOf(1_000_000), new byte[0], 100000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
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


        org.aion.types.Address usr1 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        org.aion.types.Address usr2 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        org.aion.types.Address usr3 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333"));
        org.aion.types.Address usr4 = org.aion.types.Address.wrap(Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444"));

        Transaction t0 = Transaction.call(preminedAddress, usr1, BigInteger.ZERO, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t1 = Transaction.call(preminedAddress, usr2, BigInteger.ONE, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t2 = Transaction.call(preminedAddress, usr3, BigInteger.TWO, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t3 = Transaction.call(preminedAddress, usr4, BigInteger.valueOf(3), BigInteger.valueOf(15_000_000), new byte[0], 100000L, 1);
        Transaction t4 = Transaction.create(usr4, BigInteger.ZERO, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 10_000_000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<TransactionResult>[] results = avm.run(kernel, batch);
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        TransactionResult res = results[4].get();
        org.aion.types.Address contractAddr = org.aion.types.Address.wrap(res.getReturnData());

        byte[] args = ABIUtil.encodeMethodArguments("doTransfer");
        byte[] args2 = ABIUtil.encodeMethodArguments("addValue");

        t0 = Transaction.call(preminedAddress, contractAddr, BigInteger.valueOf(4), BigInteger.valueOf(5_000_000), args2, 100000L, 1);
        t1 = Transaction.call(usr1, contractAddr, BigInteger.ZERO, BigInteger.ZERO, args, 200000L, 1);
        t2 = Transaction.call(usr2, contractAddr, BigInteger.ZERO, BigInteger.ZERO, args, 200000L, 1);
        t3 = Transaction.call(usr3, contractAddr, BigInteger.ZERO, BigInteger.ZERO, args, 200000L, 1);
        t4 = Transaction.call(usr4, contractAddr, BigInteger.ONE, BigInteger.ZERO, args, 200000L, 1);

        batch = new Transaction[]{t0, t1, t2, t3, t4};
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
        org.aion.types.Address targetUser = org.aion.types.Address.wrap(Helpers.randomBytes(org.aion.types.Address.SIZE));
        org.aion.types.Address[] tempUsers = new org.aion.types.Address[iterations];
        
        // First batch - disperse funds.
        Transaction[] firstBatch = new Transaction[iterations];
        for (int i = 0; i < iterations; ++i) {
            tempUsers[i] = org.aion.types.Address.wrap(Helpers.randomBytes(org.aion.types.Address.SIZE));
            firstBatch[i] = Transaction.call(preminedAddress, tempUsers[i], BigInteger.valueOf(i), BigInteger.valueOf(2L * valueToSend), new byte[0], 100_000L, 1L);
        }
        SimpleFuture<TransactionResult>[] results = avm.run(kernel, firstBatch);
        for (SimpleFuture<TransactionResult> f : results){
            Assert.assertTrue(f.get().getResultCode().isSuccess());
        }
        
        // Second batch - collect funds.
        Transaction[] secondBatch = new Transaction[iterations];
        for (int i = 0; i < iterations; ++i) {
            secondBatch[i] = Transaction.call(tempUsers[i], targetUser, BigInteger.ZERO, BigInteger.valueOf(valueToSend), new byte[0], 100_000L, 1L);
        }
        results = avm.run(kernel, secondBatch);
        for (SimpleFuture<TransactionResult> f : results){
            Assert.assertTrue(f.get().getResultCode().isSuccess());
        }
        
        // Check that this had the expected result.
        Assert.assertEquals(BigInteger.valueOf((long)iterations * valueToSend), kernel.getBalance(targetUser));
        avm.shutdown();
    }

}

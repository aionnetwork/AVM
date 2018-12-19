package org.aion.parallel;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.Avm;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.SimpleFuture;
import org.junit.Assert;
import org.junit.Test;

public class AvmParallelTest {

    private org.aion.vm.api.interfaces.Address preminedAddress = KernelInterfaceImpl.PREMINED_ADDRESS;

    private Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);

    private TransactionContext[] generateCTXBatch(Transaction[] batch){
        TransactionContext[] ret = new TransactionContext[batch.length];

        for (int i = 0; i < batch.length; i++){
            ret[i] = new TransactionContextImpl(batch[i], block);
        }

        return ret;
    }

    @Test
    public void basicConcurrencyTest(){
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);

        org.aion.vm.api.interfaces.Address usr1 = AvmAddress.wrap(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        BigInteger expected1 = BigInteger.ZERO;

        Transaction t0 = Transaction.call(preminedAddress, usr1, 0, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t1 = Transaction.call(preminedAddress, usr1, 1, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t2 = Transaction.call(preminedAddress, usr1, 2, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t3 = Transaction.call(preminedAddress, usr1, 3, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);
        Transaction t4 = Transaction.call(preminedAddress, usr1, 4, BigInteger.valueOf(500_000), new byte[0], 100000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<AvmTransactionResult>[] results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<AvmTransactionResult> f : results){
            f.get();
        }

        expected1 = expected1.add(BigInteger.valueOf(500_000 * 5));

        Assert.assertEquals(expected1, kernel.getBalance(usr1));

        org.aion.vm.api.interfaces.Address usr2 = AvmAddress.wrap(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        BigInteger expected2 = BigInteger.ZERO;

        t0 = Transaction.call(usr1, usr2, 0, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t1 = Transaction.call(usr1, usr2, 1, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t2 = Transaction.call(usr1, usr2, 2, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t3 = Transaction.call(usr1, usr2, 3, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t4 = Transaction.call(usr1, usr2, 4, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);

        batch = new Transaction[]{t0, t1, t2, t3, t4};
        results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<AvmTransactionResult> f : results){
            f.get();
        }

        expected1 = expected1.subtract(BigInteger.valueOf(100_000 * 5)).subtract(BigInteger.valueOf(t1.getTransactionCost() * 5));
        expected2 = BigInteger.valueOf(100_000 * 5);

        Assert.assertEquals(expected1, kernel.getBalance(usr1));
        Assert.assertEquals(expected2, kernel.getBalance(usr2));

        t0 = Transaction.call(usr1, usr2, 0, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        t1 = Transaction.call(usr2, usr1, 1, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);

        int iteration = 10;
        batch = new Transaction[iteration * 2];
        for (int i = 0; i < iteration; i++){
            batch[i * 2]     = Transaction.call(usr1, usr2, 5 + i, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
            batch[i * 2 + 1] = Transaction.call(usr2, usr1, i, BigInteger.valueOf(100_000), new byte[0], 100000L, 1);
        }

        results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<AvmTransactionResult> f : results){
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

        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);

        org.aion.vm.api.interfaces.Address usr1 = AvmAddress.wrap(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        org.aion.vm.api.interfaces.Address usr2 = AvmAddress.wrap(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        org.aion.vm.api.interfaces.Address usr3 = AvmAddress.wrap(Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333"));
        org.aion.vm.api.interfaces.Address usr4 = AvmAddress.wrap(Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444"));

        Transaction t0 = Transaction.call(preminedAddress, usr1, 0, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t1 = Transaction.call(usr1, usr2, 0, BigInteger.valueOf(4_000_000), new byte[0], 100000L, 1);
        Transaction t2 = Transaction.call(usr2, usr3, 0, BigInteger.valueOf(3_000_000), new byte[0], 100000L, 1);
        Transaction t3 = Transaction.call(usr3, usr4, 0, BigInteger.valueOf(2_000_000), new byte[0], 100000L, 1);
        Transaction t4 = Transaction.call(usr4, usr1, 0, BigInteger.valueOf(1_000_000), new byte[0], 100000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<AvmTransactionResult>[] results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<AvmTransactionResult> f : results){
            f.get();
        }

        avm.shutdown();
    }

    @Test
    public void internalTransactionTest(){

        byte[] code = JarBuilder.buildJarForMainAndClasses(TestContract.class);

        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        Avm avm = CommonAvmFactory.buildAvmInstance(kernel);


        org.aion.vm.api.interfaces.Address usr1 = AvmAddress.wrap(Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111"));
        org.aion.vm.api.interfaces.Address usr2 = AvmAddress.wrap(Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222"));
        org.aion.vm.api.interfaces.Address usr3 = AvmAddress.wrap(Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333"));
        org.aion.vm.api.interfaces.Address usr4 = AvmAddress.wrap(Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444"));

        Transaction t0 = Transaction.call(preminedAddress, usr1, 0, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t1 = Transaction.call(preminedAddress, usr2, 1, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t2 = Transaction.call(preminedAddress, usr3, 2, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t3 = Transaction.call(preminedAddress, usr4, 3, BigInteger.valueOf(5_000_000), new byte[0], 100000L, 1);
        Transaction t4 = Transaction.create(usr4, 0, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 3_000_000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<AvmTransactionResult>[] results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<AvmTransactionResult> f : results){
            f.get();
        }

        AvmTransactionResult res = results[4].get();
        org.aion.vm.api.interfaces.Address contractAddr = AvmAddress.wrap(res.getReturnData());

        byte[] args = ABIEncoder.encodeMethodArguments("doTransfer");
        byte[] args2 = ABIEncoder.encodeMethodArguments("addValue");

        t0 = Transaction.call(preminedAddress, contractAddr, 4, BigInteger.valueOf(5_000_000), args2, 100000L, 1);
        t1 = Transaction.call(usr1, contractAddr, 0, BigInteger.ZERO, args, 200000L, 1);
        t2 = Transaction.call(usr2, contractAddr, 0, BigInteger.ZERO, args, 200000L, 1);
        t3 = Transaction.call(usr3, contractAddr, 0, BigInteger.ZERO, args, 200000L, 1);
        t4 = Transaction.call(usr4, contractAddr, 1, BigInteger.ZERO, args, 200000L, 1);

        batch = new Transaction[]{t0, t1, t2, t3, t4};
        results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<AvmTransactionResult> f : results){
            f.get();
        }

        avm.shutdown();
    }

}

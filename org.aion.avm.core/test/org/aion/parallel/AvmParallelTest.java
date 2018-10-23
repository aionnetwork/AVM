package org.aion.parallel;

import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.Avm;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.junit.Assert;
import org.junit.Test;

public class AvmParallelTest {

    private byte[] preminedAddress = KernelInterfaceImpl.PREMINED_ADDRESS;

    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);

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
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

        byte[] usr1 = Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111");
        long expected1 = 0;

        Transaction t0 = Transaction.call(preminedAddress, usr1, 0, 500_000, new byte[0], 100000L, 1);
        Transaction t1 = Transaction.call(preminedAddress, usr1, 1, 500_000, new byte[0], 100000L, 1);
        Transaction t2 = Transaction.call(preminedAddress, usr1, 2, 500_000, new byte[0], 100000L, 1);
        Transaction t3 = Transaction.call(preminedAddress, usr1, 3, 500_000, new byte[0], 100000L, 1);
        Transaction t4 = Transaction.call(preminedAddress, usr1, 4, 500_000, new byte[0], 100000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<TransactionResult>[] results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        expected1 = expected1 + (500_000 * 5);

        Assert.assertEquals(expected1, kernel.getBalance(usr1));

        byte[] usr2 = Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222");
        long expected2 = 0;

        t0 = Transaction.call(usr1, usr2, 0, 100_000, new byte[0], 100000L, 1);
        t1 = Transaction.call(usr1, usr2, 1, 100_000, new byte[0], 100000L, 1);
        t2 = Transaction.call(usr1, usr2, 2, 100_000, new byte[0], 100000L, 1);
        t3 = Transaction.call(usr1, usr2, 3, 100_000, new byte[0], 100000L, 1);
        t4 = Transaction.call(usr1, usr2, 4, 100_000, new byte[0], 100000L, 1);

        batch = new Transaction[]{t0, t1, t2, t3, t4};
        results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        expected1 = expected1 - (100_000 * 5) - (t1.getBasicCost() * 5);
        expected2 = 100_000 * 5;

        Assert.assertEquals(expected1, kernel.getBalance(usr1));
        Assert.assertEquals(expected2, kernel.getBalance(usr2));

        t0 = Transaction.call(usr1, usr2, 0, 100_000, new byte[0], 100000L, 1);
        t1 = Transaction.call(usr2, usr1, 1, 100_000, new byte[0], 100000L, 1);

        int iteration = 10;
        batch = new Transaction[iteration * 2];
        for (int i = 0; i < iteration; i++){
            batch[i * 2]     = Transaction.call(usr1, usr2, 5 + i, 100_000, new byte[0], 100000L, 1);
            batch[i * 2 + 1] = Transaction.call(usr2, usr1, i, 100_000, new byte[0], 100000L, 1);
        }

        results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        expected1 = expected1 - (t0.getBasicCost() * iteration);
        expected2 = expected2 - (t1.getBasicCost() * iteration);

        Assert.assertEquals(expected1, kernel.getBalance(usr1));
        Assert.assertEquals(expected2, kernel.getBalance(usr2));

        avm.shutdown();
    }

    @Test
    public void cyclicWaitTest(){

        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);

        byte[] usr1 = Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111");
        byte[] usr2 = Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222");
        byte[] usr3 = Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333");
        byte[] usr4 = Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444");

        Transaction t0 = Transaction.call(preminedAddress, usr1, 0, 5_000_000, new byte[0], 100000L, 1);
        Transaction t1 = Transaction.call(usr1, usr2, 0, 4_000_000, new byte[0], 100000L, 1);
        Transaction t2 = Transaction.call(usr2, usr3, 0, 3_000_000, new byte[0], 100000L, 1);
        Transaction t3 = Transaction.call(usr3, usr4, 0, 2_000_000, new byte[0], 100000L, 1);
        Transaction t4 = Transaction.call(usr4, usr1, 0, 1_000_000, new byte[0], 100000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<TransactionResult>[] results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        avm.shutdown();
    }

    @Test
    public void internalTransactionTest(){

        byte[] code = JarBuilder.buildJarForMainAndClasses(TestContract.class);

        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        Avm avm = NodeEnvironment.singleton.buildAvmInstance(kernel);


        byte[] usr1 = Helpers.hexStringToBytes("1111111111111111111111111111111111111111111111111111111111111111");
        byte[] usr2 = Helpers.hexStringToBytes("2222222222222222222222222222222222222222222222222222222222222222");
        byte[] usr3 = Helpers.hexStringToBytes("3333333333333333333333333333333333333333333333333333333333333333");
        byte[] usr4 = Helpers.hexStringToBytes("4444444444444444444444444444444444444444444444444444444444444444");

        Transaction t0 = Transaction.call(preminedAddress, usr1, 0, 5_000_000, new byte[0], 100000L, 1);
        Transaction t1 = Transaction.call(preminedAddress, usr2, 1, 5_000_000, new byte[0], 100000L, 1);
        Transaction t2 = Transaction.call(preminedAddress, usr3, 2, 5_000_000, new byte[0], 100000L, 1);
        Transaction t3 = Transaction.call(preminedAddress, usr4, 3, 5_000_000, new byte[0], 100000L, 1);
        Transaction t4 = Transaction.create(usr4, 0, 0, new CodeAndArguments(code, null).encodeToBytes(), 3_000_000L, 1);

        Transaction[] batch = new Transaction[]{t0, t1, t2, t3, t4};
        SimpleFuture<TransactionResult>[] results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        TransactionResult res = results[4].get();
        byte[] contractAddr = res.getReturnData();

        byte[] args = ABIEncoder.encodeMethodArguments("doTransfer");
        byte[] args2 = ABIEncoder.encodeMethodArguments("addValue");

        t0 = Transaction.call(preminedAddress, contractAddr, 4, 5_000_000, args2, 100000L, 1);
        t1 = Transaction.call(usr1, contractAddr, 0, 0, args, 200000L, 1);
        t2 = Transaction.call(usr2, contractAddr, 0, 0, args, 200000L, 1);
        t3 = Transaction.call(usr3, contractAddr, 0, 0, args, 200000L, 1);
        t4 = Transaction.call(usr4, contractAddr, 1, 0, args, 200000L, 1);

        batch = new Transaction[]{t0, t1, t2, t3, t4};
        results = avm.run(generateCTXBatch(batch));
        for (SimpleFuture<TransactionResult> f : results){
            f.get();
        }

        avm.shutdown();
    }

}

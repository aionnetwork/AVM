package org.aion.avm.core.shadowing.testString;

import java.math.BigInteger;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.SimpleFuture;
import org.aion.vm.api.interfaces.TransactionContext;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Test;

public class StringShadowingTest {

    @Test
    public void testSingleString() {
        org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        AvmImpl avm = CommonAvmFactory.buildAvmInstance(kernel);

        // deploy it
        byte[] testJar = JarBuilder.buildJarForMainAndClasses(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        org.aion.vm.api.interfaces.Address dappAddr = AvmAddress.wrap(avm.run(new TransactionContext[] {context})[0].get().getReturnData());

        // call transactions and validate the results
        txData = ABIEncoder.encodeMethodArguments("singleStringReturnInt");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        TransactionResult result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertTrue(java.util.Arrays.equals(new int[]{96354, 3, 1, -1}, (int[]) ABIDecoder.decodeOneObject(result.getReturnData())));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnBoolean");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();
        //Assert.assertTrue(java.util.Arrays.equals(new byte[]{1, 0, 1, 0, 1, 0, 0}, (byte[]) ABIDecoder.decodeOneObject(result.getReturnData())));
        Assert.assertTrue(java.util.Arrays.equals(new boolean[]{true, false, true, false, true, false, false}, (boolean[]) ABIDecoder.decodeOneObject(result.getReturnData())));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnChar");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals('a', ABIDecoder.decodeOneObject(result.getReturnData()));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnBytes");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertTrue(java.util.Arrays.equals(new byte[]{'a', 'b', 'c'}, (byte[]) ABIDecoder.decodeOneObject(result.getReturnData())));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnLowerCase");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals("abc", ABIDecoder.decodeOneObject(result.getReturnData()));

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnUpperCase");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        context = new TransactionContextImpl(tx, block);
        result = avm.run(new TransactionContext[] {context})[0].get();
        Assert.assertEquals("ABC", ABIDecoder.decodeOneObject(result.getReturnData()));
        avm.shutdown();
    }

    /**
     * Same logic as testSingleString(), but done as a single transaction batch to verify that doesn't change the results
     * of a long sequence of calls.
     */
    @Test
    public void testBatchingCalls() {
        org.aion.vm.api.interfaces.Address from = KernelInterfaceImpl.PREMINED_ADDRESS;
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        AvmImpl avm = CommonAvmFactory.buildAvmInstance(kernel);

        // We do the deployment, first, since we need the resultant DApp address for the other calls.
        byte[] testJar = JarBuilder.buildJarForMainAndClasses(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        org.aion.vm.api.interfaces.Address dappAddr = AvmAddress.wrap(avm.run(new TransactionContext[] {context})[0].get().getReturnData());

        // Now, batch the other 6 transactions together and verify that the result is the same (note that the nonces are artificially incremented since these all have the same sender).
        TransactionContext[] batch = new TransactionContext[6];
        
        txData = ABIEncoder.encodeMethodArguments("singleStringReturnInt");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        batch[0] = new TransactionContextImpl(tx, block);

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnBoolean");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from) .add(BigInteger.ONE), BigInteger.ZERO, txData, energyLimit, energyPrice);
        batch[1] = new TransactionContextImpl(tx, block);

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnChar");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.TWO), BigInteger.ZERO, txData, energyLimit, energyPrice);
        batch[2] = new TransactionContextImpl(tx, block);

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnBytes");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(3)), BigInteger.ZERO, txData, energyLimit, energyPrice);
        batch[3] = new TransactionContextImpl(tx, block);

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnLowerCase");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(4)), BigInteger.ZERO, txData, energyLimit, energyPrice);
        batch[4] = new TransactionContextImpl(tx, block);

        txData = ABIEncoder.encodeMethodArguments("singleStringReturnUpperCase");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(5)), BigInteger.ZERO, txData, energyLimit, energyPrice);
        batch[5] = new TransactionContextImpl(tx, block);

        // Send the batch.
        SimpleFuture<TransactionResult>[] results = avm.run(batch);
        
        // Now, process the results.
        Assert.assertTrue(java.util.Arrays.equals(new int[]{96354, 3, 1, -1}, (int[]) ABIDecoder.decodeOneObject(results[0].get().getReturnData())));
        Assert.assertTrue(java.util.Arrays.equals(new boolean[]{true, false, true, false, true, false, false}, (boolean[]) ABIDecoder.decodeOneObject(results[1].get().getReturnData())));
        Assert.assertEquals('a', ABIDecoder.decodeOneObject(results[2].get().getReturnData()));
        Assert.assertTrue(java.util.Arrays.equals(new byte[]{'a', 'b', 'c'}, (byte[]) ABIDecoder.decodeOneObject(results[3].get().getReturnData())));
        Assert.assertEquals("abc", ABIDecoder.decodeOneObject(results[4].get().getReturnData()));
        Assert.assertEquals("ABC", ABIDecoder.decodeOneObject(results[5].get().getReturnData()));
        
        avm.shutdown();
    }
}

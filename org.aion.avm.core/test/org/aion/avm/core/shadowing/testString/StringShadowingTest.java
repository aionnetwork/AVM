package org.aion.avm.core.shadowing.testString;

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

public class StringShadowingTest {

    @Test
    public void testSingleString() {
        org.aion.types.Address from = TestingKernel.PREMINED_ADDRESS;
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        TestingKernel kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        // deploy it
        byte[] testJar = JarBuilder.buildJarForMainAndClassesAndUserlib(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        org.aion.types.Address dappAddr = org.aion.types.Address.wrap(avm.run(kernel, new Transaction[] {tx})[0].get().getReturnData());

        // call transactions and validate the results
        txData = ABIUtil.encodeMethodArguments("singleStringReturnInt");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult result = avm.run(kernel, new Transaction[] {tx})[0].get();
        Assert.assertTrue(java.util.Arrays.equals(new int[]{96354, 3, 1, -1}, (int[]) ABIUtil.decodeOneObject(result.getReturnData())));

        txData = ABIUtil.encodeMethodArguments("singleStringReturnBoolean");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx})[0].get();
        //Assert.assertTrue(java.util.Arrays.equals(new byte[]{1, 0, 1, 0, 1, 0, 0}, (byte[]) ABIUtil.decodeOneObject(result.getReturnData())));
        Assert.assertTrue(java.util.Arrays.equals(new boolean[]{true, false, true, false, true, false, false}, (boolean[]) ABIUtil.decodeOneObject(result.getReturnData())));

        txData = ABIUtil.encodeMethodArguments("singleStringReturnChar");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx})[0].get();
        Assert.assertEquals('a', ABIUtil.decodeOneObject(result.getReturnData()));

        txData = ABIUtil.encodeMethodArguments("singleStringReturnBytes");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx})[0].get();
        Assert.assertTrue(java.util.Arrays.equals(new byte[]{'a', 'b', 'c'}, (byte[]) ABIUtil.decodeOneObject(result.getReturnData())));

        txData = ABIUtil.encodeMethodArguments("singleStringReturnLowerCase");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx})[0].get();
        Assert.assertEquals("abc", ABIUtil.decodeOneObject(result.getReturnData()));

        txData = ABIUtil.encodeMethodArguments("singleStringReturnUpperCase");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx})[0].get();
        Assert.assertEquals("ABC", ABIUtil.decodeOneObject(result.getReturnData()));

        txData = ABIUtil.encodeMethodArguments("stringReturnSubSequence");
        tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx})[0].get();
        Assert.assertEquals("Sub", ABIUtil.decodeOneObject(result.getReturnData()));
        avm.shutdown();
    }

    /**
     * Same logic as testSingleString(), but done as a single transaction batch to verify that doesn't change the results
     * of a long sequence of calls.
     */
    @Test
    public void testBatchingCalls() {
        org.aion.types.Address from = TestingKernel.PREMINED_ADDRESS;
        Block block = new Block(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        TestingKernel kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        // We do the deployment, first, since we need the resultant DApp address for the other calls.
        byte[] testJar = JarBuilder.buildJarForMainAndClassesAndUserlib(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        org.aion.types.Address dappAddr = org.aion.types.Address.wrap(avm.run(kernel, new Transaction[] {tx})[0].get().getReturnData());

        // Now, batch the other 6 transactions together and verify that the result is the same (note that the nonces are artificially incremented since these all have the same sender).
        Transaction[] batch = new Transaction[6];
        
        txData = ABIUtil.encodeMethodArguments("singleStringReturnInt");
        batch[0] = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = ABIUtil.encodeMethodArguments("singleStringReturnBoolean");
        batch[1] = Transaction.call(from, dappAddr, kernel.getNonce(from) .add(BigInteger.ONE), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = ABIUtil.encodeMethodArguments("singleStringReturnChar");
        batch[2] = Transaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.TWO), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = ABIUtil.encodeMethodArguments("singleStringReturnBytes");
        batch[3] = Transaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(3)), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = ABIUtil.encodeMethodArguments("singleStringReturnLowerCase");
        batch[4] = Transaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(4)), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = ABIUtil.encodeMethodArguments("singleStringReturnUpperCase");
        batch[5] = Transaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(5)), BigInteger.ZERO, txData, energyLimit, energyPrice);

        // Send the batch.
        SimpleFuture<TransactionResult>[] results = avm.run(kernel, batch);
        
        // Now, process the results.
        Assert.assertTrue(java.util.Arrays.equals(new int[]{96354, 3, 1, -1}, (int[]) ABIUtil.decodeOneObject(results[0].get().getReturnData())));
        Assert.assertTrue(java.util.Arrays.equals(new boolean[]{true, false, true, false, true, false, false}, (boolean[]) ABIUtil.decodeOneObject(results[1].get().getReturnData())));
        Assert.assertEquals('a', ABIUtil.decodeOneObject(results[2].get().getReturnData()));
        Assert.assertTrue(java.util.Arrays.equals(new byte[]{'a', 'b', 'c'}, (byte[]) ABIUtil.decodeOneObject(results[3].get().getReturnData())));
        Assert.assertEquals("abc", ABIUtil.decodeOneObject(results[4].get().getReturnData()));
        Assert.assertEquals("ABC", ABIUtil.decodeOneObject(results[5].get().getReturnData()));
        
        avm.shutdown();
    }
}

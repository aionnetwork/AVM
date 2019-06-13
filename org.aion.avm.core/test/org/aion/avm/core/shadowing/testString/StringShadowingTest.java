package org.aion.avm.core.shadowing.testString;

import java.math.BigInteger;

import org.aion.avm.core.FutureResult;
import org.aion.types.AionAddress;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.*;
import org.aion.vm.api.interfaces.TransactionResult;
import org.junit.Assert;
import org.junit.Test;

public class StringShadowingTest {

    @Test
    public void testSingleString() {
        AionAddress from = TestingKernel.PREMINED_ADDRESS;
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        TestingKernel kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        kernel.generateBlock();
        // deploy it
        byte[] testJar = JarBuilder.buildJarForMainAndClassesAndUserlib(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        TestingTransaction tx = TestingTransaction.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AionAddress dappAddr = new AionAddress(avm.run(kernel, new TestingTransaction[] {tx})[0].get().getReturnData());
        kernel.generateBlock();

        // call transactions and validate the results
        txData = encodeNoArgsMethodCall("singleStringReturnInt");
        tx = TestingTransaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult result = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        Assert.assertTrue(java.util.Arrays.equals(new int[]{96354, 3, 1, -1}, new ABIDecoder(result.getReturnData()).decodeOneIntegerArray()));
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnBoolean");
        tx = TestingTransaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        Assert.assertTrue(java.util.Arrays.equals(new boolean[]{true, false, true, false, true, false, false}, new ABIDecoder(result.getReturnData()).decodeOneBooleanArray()));
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnChar");
        tx = TestingTransaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        Assert.assertEquals('a', new ABIDecoder(result.getReturnData()).decodeOneCharacter());
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnBytes");
        tx = TestingTransaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        Assert.assertTrue(java.util.Arrays.equals(new byte[]{'a', 'b', 'c'}, new ABIDecoder(result.getReturnData()).decodeOneByteArray()));
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnLowerCase");
        tx = TestingTransaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        Assert.assertEquals("abc", new ABIDecoder(result.getReturnData()).decodeOneString());
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnUpperCase");
        tx = TestingTransaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        Assert.assertEquals("ABC", new ABIDecoder(result.getReturnData()).decodeOneString());
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("stringReturnSubSequence");
        tx = TestingTransaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        Assert.assertEquals("Sub", new ABIDecoder(result.getReturnData()).decodeOneString());

        txData = encodeNoArgsMethodCall("equalsIgnoreCase");
        tx = TestingTransaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new TestingTransaction[] {tx})[0].get();
        Assert.assertEquals(false, new ABIDecoder(result.getReturnData()).decodeOneBoolean());

        avm.shutdown();
    }

    /**
     * Same logic as testSingleString(), but done as a single transaction batch to verify that doesn't change the results
     * of a long sequence of calls.
     */
    @Test
    public void testBatchingCalls() {
        AionAddress from = TestingKernel.PREMINED_ADDRESS;
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        TestingKernel kernel = new TestingKernel(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        // We do the deployment, first, since we need the resultant DApp address for the other calls.
        byte[] testJar = JarBuilder.buildJarForMainAndClassesAndUserlib(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        TestingTransaction tx = TestingTransaction.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AionAddress dappAddr = new AionAddress(avm.run(kernel, new TestingTransaction[] {tx})[0].get().getReturnData());

        // Now, batch the other 6 transactions together and verify that the result is the same (note that the nonces are artificially incremented since these all have the same sender).
        TestingTransaction[] batch = new TestingTransaction[6];
        
        txData = encodeNoArgsMethodCall("singleStringReturnInt");
        batch[0] = TestingTransaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnBoolean");
        batch[1] = TestingTransaction.call(from, dappAddr, kernel.getNonce(from) .add(BigInteger.ONE), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnChar");
        batch[2] = TestingTransaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.TWO), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnBytes");
        batch[3] = TestingTransaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(3)), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnLowerCase");
        batch[4] = TestingTransaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(4)), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnUpperCase");
        batch[5] = TestingTransaction.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(5)), BigInteger.ZERO, txData, energyLimit, energyPrice);

        // Send the batch.
        FutureResult[] results = avm.run(kernel, batch);
        
        // Now, process the results.
        Assert.assertArrayEquals(new int[]{96354, 3, 1, -1}, new ABIDecoder(results[0].get().getReturnData()).decodeOneIntegerArray());
        Assert.assertArrayEquals(new boolean[]{true, false, true, false, true, false, false}, new ABIDecoder(results[1].get().getReturnData()).decodeOneBooleanArray());
        Assert.assertEquals('a', new ABIDecoder(results[2].get().getReturnData()).decodeOneCharacter());
        Assert.assertArrayEquals(new byte[]{'a', 'b', 'c'}, new ABIDecoder(results[3].get().getReturnData()).decodeOneByteArray());
        Assert.assertEquals("abc", new ABIDecoder(results[4].get().getReturnData()).decodeOneString());
        Assert.assertEquals("ABC", new ABIDecoder(results[5].get().getReturnData()).decodeOneString());
        
        avm.shutdown();
    }


    private static byte[] encodeNoArgsMethodCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }
}

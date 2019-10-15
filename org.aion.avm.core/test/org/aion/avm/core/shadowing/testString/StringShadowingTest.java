package org.aion.avm.core.shadowing.testString;

import java.math.BigInteger;

import org.aion.avm.core.*;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.*;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.Test;

public class StringShadowingTest {

    @Test
    public void testSingleString() {
        AionAddress from = TestingState.PREMINED_ADDRESS;
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        kernel.generateBlock();
        // deploy it
        byte[] testJar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = AvmTransactionUtil.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AionAddress dappAddr = new AionAddress(avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult().copyOfTransactionOutput().orElseThrow());
        kernel.generateBlock();

        // call transactions and validate the results
        txData = encodeNoArgsMethodCall("singleStringReturnInt");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(java.util.Arrays.equals(new int[]{96354, 3, 1, -1}, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneIntegerArray()));
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnBoolean");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(java.util.Arrays.equals(new boolean[]{true, false, true, false, true, false, false}, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneBooleanArray()));
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnChar");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertEquals('a', new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneCharacter());
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnBytes");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(java.util.Arrays.equals(new byte[]{'a', 'b', 'c'}, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneByteArray()));
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnLowerCase");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertEquals("abc", new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("singleStringReturnUpperCase");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertEquals("ABC", new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());
        kernel.generateBlock();

        txData = encodeNoArgsMethodCall("stringReturnSubSequence");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertEquals("Sub", new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());

        txData = encodeNoArgsMethodCall("equalsIgnoreCase");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        Assert.assertEquals(false, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());

        txData = encodeNoArgsMethodCall("regionMatches");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertEquals(true, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());

        txData = encodeNoArgsMethodCall("valueOf");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertEquals(true, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneBoolean());
        avm.shutdown();
    }

    /**
     * Same logic as testSingleString(), but done as a single transaction batch to verify that doesn't change the results
     * of a long sequence of calls.
     */
    @Test
    public void testBatchingCalls() {
        AionAddress from = TestingState.PREMINED_ADDRESS;
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 6_000_0000;
        long energyPrice = 1;
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        // We do the deployment, first, since we need the resultant DApp address for the other calls.
        byte[] testJar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = AvmTransactionUtil.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AionAddress dappAddr = new AionAddress(avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult().copyOfTransactionOutput().orElseThrow());

        // Now, batch the other 6 transactions together and verify that the result is the same (note that the nonces are artificially incremented since these all have the same sender).
        Transaction[] batch = new Transaction[6];
        
        txData = encodeNoArgsMethodCall("singleStringReturnInt");
        batch[0] = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnBoolean");
        batch[1] = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from) .add(BigInteger.ONE), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnChar");
        batch[2] = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.TWO), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnBytes");
        batch[3] = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(3)), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnLowerCase");
        batch[4] = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(4)), BigInteger.ZERO, txData, energyLimit, energyPrice);

        txData = encodeNoArgsMethodCall("singleStringReturnUpperCase");
        batch[5] = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from).add(BigInteger.valueOf(5)), BigInteger.ZERO, txData, energyLimit, energyPrice);

        // Send the batch.
        FutureResult[] results = avm.run(kernel, batch, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1);

        // Now, process the results.
        Assert.assertArrayEquals(new int[]{96354, 3, 1, -1}, new ABIDecoder(results[0].getResult().copyOfTransactionOutput().orElseThrow()).decodeOneIntegerArray());
        Assert.assertArrayEquals(new boolean[]{true, false, true, false, true, false, false}, new ABIDecoder(results[1].getResult().copyOfTransactionOutput().orElseThrow()).decodeOneBooleanArray());
        Assert.assertEquals('a', new ABIDecoder(results[2].getResult().copyOfTransactionOutput().orElseThrow()).decodeOneCharacter());
        Assert.assertArrayEquals(new byte[]{'a', 'b', 'c'}, new ABIDecoder(results[3].getResult().copyOfTransactionOutput().orElseThrow()).decodeOneByteArray());
        Assert.assertEquals("abc", new ABIDecoder(results[4].getResult().copyOfTransactionOutput().orElseThrow()).decodeOneString());
        Assert.assertEquals("ABC", new ABIDecoder(results[5].getResult().copyOfTransactionOutput().orElseThrow()).decodeOneString());
        
        avm.shutdown();
    }

    @Test
    public void testInvalidCases() {
        AionAddress from = TestingState.PREMINED_ADDRESS;
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        long energyLimit = 5_000_0000;
        long energyPrice = 1;
        TestingState kernel = new TestingState(block);
        AvmImpl avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());

        // We do the deployment, first, since we need the resultant DApp address for the other calls.
        byte[] testJar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(TestResource.class);
        byte[] txData = new CodeAndArguments(testJar, null).encodeToBytes();
        Transaction tx = AvmTransactionUtil.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        AionAddress dappAddr = new AionAddress(avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult().copyOfTransactionOutput().orElseThrow());

        txData = encodeNoArgsMethodCall("regionMatchesInvalidLength");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        txData = encodeNoArgsMethodCall("regionMatchesDoNotIgnoreInvalidLength");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        txData = encodeNoArgsMethodCall("copyValueOfInvalidCount");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        txData = encodeNoArgsMethodCall("valueOfInvalidCount");
        tx = AvmTransactionUtil.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[] {tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        avm.shutdown();
    }


    private static byte[] encodeNoArgsMethodCall(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }
}

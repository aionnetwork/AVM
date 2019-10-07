package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MethodWrapperInvocationTest {
    private static final AionAddress DEPLOYER = TestingState.PREMINED_ADDRESS;
    private static TestingBlock block;
    private static TestingState kernel;
    private static AvmImpl avm;
    private static AionAddress dappAddress;

    @BeforeClass
    public static void setupClass() {
        block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        AvmConfiguration config = new AvmConfiguration();
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);

        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(MethodWrapperTarget.class, MethodWrapperAbstractTarget.class, MethodWrapperInterfaceTarget.class);
        byte[] deployment = new CodeAndArguments(jar, null).encodeToBytes();
        Transaction create = AvmTransactionUtil.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, deployment, 5_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {create}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        dappAddress = new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }

    @AfterClass
    public static void tearDownClass() {
        avm.shutdown();
    }

    @Test
    public void testInvokeVoidReturnMethod() {
        byte[] call = ABIEncoder.encodeOneString("invokeVoidReturnMethod");

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        // The actual call has no output so the main method returns the invoke string back so we can verify we hit the correct case.
        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertEquals("invokeVoidReturnMethod", new ABIDecoder(output).decodeOneString());
    }

    @Test
    public strictfp void testInvokePrimitiveReturnMethod() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokePrimitiveReturnMethod").encodeOneByte((byte) 0x2).encodeOneCharacterArray(new char[]{ 'a', 'b' }).encodeOneString("s").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertEquals((float) 0.0, new ABIDecoder(output).decodeOneFloat(), 0.0);
    }

    @Test
    public void testInvokeArrayReturnMethod() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeArrayReturnMethod").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertArrayEquals(new int[]{ 5, 4, 3, 2, 1 }, new ABIDecoder(output).decodeOneIntegerArray());
    }

    @Test
    public void testInvokeObjectReturnMethod() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeObjectReturnMethod").encodeOneByte((byte) 0x2).encodeOneCharacterArray(new char[]{ 'a', 'b' }).encodeOneString("s").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertNull(new ABIDecoder(output).decodeOneString());
    }

    @Test
    public void testInvokeFinalMethod() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeFinalMethod").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertEquals(BigInteger.TEN, new ABIDecoder(output).decodeOneBigInteger());
    }

    @Test
    public void testInvokeMethodWithTryCatch() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeMethodWithTryCatch").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertArrayEquals(new int[2], new ABIDecoder(output).decodeOneIntegerArray());
    }

    @Test
    public void testInvokeCallChainOfDepth3() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeCallChainOfDepth3").encodeOneInteger(15).encodeOneInteger(78).toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertArrayEquals(new int[][]{ new int[]{ 30, 156 }, new int[]{ 156, 30 } }, new ABIDecoder(output).decodeOne2DIntegerArray());
    }

    @Test
    public void testInvokeRecursiveMethod() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeRecursiveMethod").encodeOneInteger(8).toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertEquals(8, new ABIDecoder(output).decodeOneInteger());
    }

    @Test
    public void testInvokeImplementationMethodDefinedInAbstractClass() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeImplementationMethodDefinedInAbstractClass").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertEquals(7, new ABIDecoder(output).decodeOneLong());
    }

    @Test
    public void testInvokeImplementationOfAbstractMethod() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeImplementationOfAbstractMethod").encodeOneInteger(17).encodeOneLong(19).toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertEquals(BigInteger.valueOf(17 * 19), new ABIDecoder(output).decodeOneBigInteger());
    }

    @Test
    public void testInvokeImplementationOfInterfaceMethod() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeImplementationOfInterfaceMethod").encodeOneString("something").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertEquals("avm_something", new ABIDecoder(output).decodeOneString());
    }

    @Test
    public void testInvokeMethodThatThrowsException() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeMethodThatThrowsException").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    @Test
    public void testUnimplementedToStringOnMainClass() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeUnimplementedToStringOnMainClass").encodeOneString("something").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();

        String outputAsString = new ABIDecoder(output).decodeOneString();
        String truncatedToRemoveHash = outputAsString.substring(0, outputAsString.length() - 3);
        Assert.assertEquals("org.aion.avm.core.MethodWrapperTarget", truncatedToRemoveHash);
    }

    @Test
    public void testInvokeInterfaceDefaultMethod() {
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeDefaultMethod").encodeOneString("something").toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertEquals(51L, new ABIDecoder(output).decodeOneLong());
    }

    @Test
    public void testInvokeConstructors() {
        String s1 = "something";
        String s2 = "something else";
        byte[] call = new ABIStreamingEncoder().encodeOneString("invokeConstructors").encodeOneString(s1).encodeOneString(s2).toBytes();

        Transaction callTransaction = AvmTransactionUtil.call(DEPLOYER, dappAddress, kernel.getNonce(DEPLOYER), BigInteger.ZERO, call, 2_000_000L, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {callTransaction}, ExecutionType.ASSUME_MAINCHAIN,kernel.getBlockNumber() - 1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        byte[] output = result.copyOfTransactionOutput().orElseThrow();
        Assert.assertEquals("args = args = " + s1 + "args = " + s1 + s2, new ABIDecoder(output).decodeOneString());
    }
}

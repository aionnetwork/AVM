package org.aion.avm.embed.benchmark;

import avm.Address;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.embed.AvmRule;
import org.aion.avm.embed.AvmRule.ResultWrapper;
import org.aion.avm.tooling.abi.ABICompiler;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.types.TransactionResult;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.ClassRule;
import org.junit.Test;


/**
 * These are just meant to demonstrate the basic use-cases of the benchmarks we are collecting for demonstrating various behaviours.
 */
public class BenchmarkTest {
    @ClassRule
    public static final AvmRule RULE = new AvmRule(false);
    private static final Address FROM = RULE.getPreminedAccount();

    /**
     * Note that this "test" method only exists as a convenient way to capture test resources to deploy into other environments.
     * Set the "TEST_RESOURCES" env var in order to write these resources out.
     */
    @Test
    public void outputTestResources() throws Exception {
        String outputPath = System.getenv("TEST_RESOURCES");
        // Ignore this is not set.
        Assume.assumeNotNull(outputPath);
        
        // For both of these, capture the optimized JAR and ABI.
        System.out.println("Outputing test resources to: \"" + outputPath + "\"");
        outputResourcesForTestClass(outputPath, CallContract.class);
        outputResourcesForTestClass(outputPath, CPUIntensiveOperations.class);
        outputResourcesForTestClass(outputPath, MemoryUsage.class);
        outputResourcesForTestClass(outputPath, SimpleContractWithABI.class);
        outputResourcesForTestClass(outputPath, SimpleContractWithoutABI.class);
        outputResourcesForTestClass(outputPath, SimpleStorageStatic.class);
        outputResourcesForTestClass(outputPath, StorageLargeDataContract.class);
    }

    @Test
    public void testReentrantCall() {
        byte[] payload = RULE.getDappBytes(CallContract.class, new byte[0]);
        Address target = RULE.deploy(FROM, BigInteger.ZERO, payload).getDappAddress();
        
        // Connect this to itself.
        byte[] args = new ABIStreamingEncoder().encodeOneString("setOtherCallee").encodeOneAddress(target).toBytes();
        ResultWrapper result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        
        // Call can go 10 iterations deep.
        args = new ABIStreamingEncoder().encodeOneString("callOtherContract").encodeOneInteger(1).encodeOneInteger(10).toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertTrue((Boolean) result.getDecodedReturnData());
        
        // But not 11.
        args = new ABIStreamingEncoder().encodeOneString("callOtherContract").encodeOneInteger(1).encodeOneInteger(11).toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertFalse((Boolean) result.getDecodedReturnData());
    }

    @Test
    public void testSqrtDouble() {
        byte[] payload = RULE.getDappBytes(CPUIntensiveOperations.class, new byte[0]);
        Address target = RULE.deploy(FROM, BigInteger.ZERO, payload).getDappAddress();
        
        // We can do 1243 iterations before running out of energy.
        byte[] args = new ABIStreamingEncoder().encodeOneString("sqrt").encodeOneInteger(1243).encodeOneDouble(2.0D).toBytes();
        TransactionResult result = RULE.call(FROM, target, BigInteger.ZERO, args).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(1995158L, result.energyUsed);
        
        args = new ABIStreamingEncoder().encodeOneString("sqrt").encodeOneInteger(1247).encodeOneDouble(2.0D).toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args).getTransactionResult();
        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertEquals(2000000L, result.energyUsed);
    }

    @Test
    public void testFibLong() {
        byte[] payload = RULE.getDappBytes(CPUIntensiveOperations.class, new byte[0]);
        Address target = RULE.deploy(FROM, BigInteger.ZERO, payload).getDappAddress();
        byte[] args = new ABIStreamingEncoder().encodeOneString("fibonacciLong").encodeOneInteger(6).toBytes();
        long result = (Long) RULE.call(FROM, target, BigInteger.ZERO, args).getDecodedReturnData();
        Assert.assertEquals(8L, result);
        
        // Note that the long variant will overflow.
        args = new ABIStreamingEncoder().encodeOneString("fibonacciLong").encodeOneInteger(25000).toBytes();
        result = (Long) RULE.call(FROM, target, BigInteger.ZERO, args).getDecodedReturnData();
        Assert.assertEquals(-7582677186204719669L, result);
        
        args = new ABIStreamingEncoder().encodeOneString("fibonacciLong").encodeOneInteger(30000).toBytes();
        Assert.assertTrue(RULE.call(FROM, target, BigInteger.ZERO, args).getTransactionResult().transactionStatus.isFailed());
    }

    @Test
    public void testFibBigInteger() {
        byte[] payload = RULE.getDappBytes(CPUIntensiveOperations.class, new byte[0]);
        Address target = RULE.deploy(FROM, BigInteger.ZERO, payload).getDappAddress();
        byte[] args = new ABIStreamingEncoder().encodeOneString("fibonacciBigInteger").encodeOneInteger(6).toBytes();
        BigInteger result = new BigInteger((byte[]) RULE.call(FROM, target, BigInteger.ZERO, args).getDecodedReturnData());
        Assert.assertEquals(new BigInteger("8"), result);
        
        // The BigInteger loop fails on overflow after more than 367 iterations - the long version, above, is allowed to overflow and runs until the energy limit.
        args = new ABIStreamingEncoder().encodeOneString("fibonacciBigInteger").encodeOneInteger(367).toBytes();
        result = new BigInteger((byte[]) RULE.call(FROM, target, BigInteger.ZERO, args).getDecodedReturnData());
        Assert.assertEquals(new BigInteger("22334640661774067356412331900038009953045351020683823507202893507476314037053"), result);
        
        // We catch the exception, within the contract, and revert.  We can detect that case, here.
        args = new ABIStreamingEncoder().encodeOneString("fibonacciBigInteger").encodeOneInteger(368).toBytes();
        TransactionResult failureResult = RULE.call(FROM, target, BigInteger.ZERO, args).getTransactionResult();
        Assert.assertTrue(failureResult.transactionStatus.isReverted());
        Assert.assertEquals(298866L, failureResult.energyUsed);
    }

    @Test
    public void testMemoryUsage() {
        byte[] payload = RULE.getDappBytes(MemoryUsage.class, new byte[0]);
        Address target = RULE.deploy(FROM, BigInteger.ZERO, payload).getDappAddress();
        
        // Check the baseline sum cost when nothing is in the list.
        byte[] args = new ABIStreamingEncoder().encodeOneString("getSum").encodeOneAddress(FROM).toBytes();
        ResultWrapper result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(0, ((Integer) result.getDecodedReturnData()).intValue());
        Assert.assertEquals(34569L, result.getTransactionResult().energyUsed);
        
        // Add 1000 elements, 1 at a time, watching how this grows in cost as the graph grows.
        for (int i = 0; i < 1000; ++i) {
            args = new ABIStreamingEncoder().encodeOneString("insert").encodeOneInteger(1).toBytes();
            result = RULE.call(FROM, target, BigInteger.ZERO, args);
            Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
            long cost = 28221L + (i * 280L);
            Assert.assertEquals(cost, result.getTransactionResult().energyUsed);
        }
        
        // Now, check the cost of walking the entire list.
        args = new ABIStreamingEncoder().encodeOneString("getSum").encodeOneAddress(FROM).toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(500500, ((Integer) result.getDecodedReturnData()).intValue());
        Assert.assertEquals(1970569L, result.getTransactionResult().energyUsed);
    }

    @Test
    public void testSimpleWithABI() {
        byte[] payload = RULE.getDappBytes(SimpleContractWithABI.class, new byte[0]);
        Address target = RULE.deploy(FROM, BigInteger.ZERO, payload).getDappAddress();
        
        // Just check what happens when we call a method which just returns.
        byte[] args = new ABIStreamingEncoder().encodeOneString("myFunction").toBytes();
        ResultWrapper result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(26641L, result.getTransactionResult().energyUsed);
    }

    @Test
    public void testSimpleWithoutABI() {
        byte[] payload = RULE.getDappBytes(SimpleContractWithoutABI.class, new byte[0]);
        Address target = RULE.deploy(FROM, BigInteger.ZERO, payload).getDappAddress();
        
        // Just check what happens when we call a method which just returns.
        ResultWrapper result = RULE.call(FROM, target, BigInteger.ZERO, new byte[0]);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(21042L, result.getTransactionResult().energyUsed);
    }

    @Test
    public void testSimpleStorageStatic() {
        byte[] payload = RULE.getDappBytes(SimpleStorageStatic.class, new byte[0]);
        Address target = RULE.deploy(FROM, BigInteger.ZERO, payload).getDappAddress();
        
        // Just issue the reads of the various fields.
        byte[] args = new ABIStreamingEncoder().encodeOneString("getMyInt").toBytes();
        ResultWrapper result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(31442L, result.getTransactionResult().energyUsed);
        Assert.assertEquals(703, ((Integer) result.getDecodedReturnData()).intValue());
        
        args = new ABIStreamingEncoder().encodeOneString("getMyString").toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(34722L, result.getTransactionResult().energyUsed);
        Assert.assertEquals("Benchmark Testing", (String) result.getDecodedReturnData());
        
        args = new ABIStreamingEncoder().encodeOneString("getMyInt1DArray").toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(35071L, result.getTransactionResult().energyUsed);
        Assert.assertArrayEquals(new int[] {0,1,2,3,4,5}, (int[]) result.getDecodedReturnData());
        
        // Basic graph map interactions.
        args = new ABIStreamingEncoder().encodeOneString("getMap").encodeOneInteger(1).toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(38212L, result.getTransactionResult().energyUsed);
        Assert.assertEquals("one", (String) result.getDecodedReturnData());
        
        args = new ABIStreamingEncoder().encodeOneString("putMap").encodeOneInteger(1).encodeOneString("Testing").toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(39098L, result.getTransactionResult().energyUsed);
        
        args = new ABIStreamingEncoder().encodeOneString("getMap").encodeOneInteger(1).toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(38376L, result.getTransactionResult().energyUsed);
        Assert.assertEquals("Testing", (String) result.getDecodedReturnData());
        
        // Basic key-value store interactions.
        args = new ABIStreamingEncoder().encodeOneString("getStorage").encodeOneString("key").toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(37363L, result.getTransactionResult().energyUsed);
        Assert.assertEquals(null, (String) result.getDecodedReturnData());
        
        args = new ABIStreamingEncoder().encodeOneString("putStorage").encodeOneString("key").encodeOneString("value").toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(60593L, result.getTransactionResult().energyUsed);
        
        args = new ABIStreamingEncoder().encodeOneString("getStorage").encodeOneString("key").toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(40371L, result.getTransactionResult().energyUsed);
        Assert.assertEquals("value", (String) result.getDecodedReturnData());
    }

    @Test
    public void testStorageLargeDataContract() {
        byte[] payload = RULE.getDappBytes(StorageLargeDataContract.class, new byte[0]);
        Address target = RULE.deploy(FROM, BigInteger.ZERO, payload).getDappAddress();
        
        // Create something small.
        int size = 0;
        byte[] args = new ABIStreamingEncoder().encodeOneString("writeToObjectArray").encodeOneInteger(size).encodeOneInteger(size).toBytes();
        ResultWrapper result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(28921L, result.getTransactionResult().energyUsed);
        
        // Something just below the max.
        size = 280;
        args = new ABIStreamingEncoder().encodeOneString("writeToObjectArray").encodeOneInteger(size).encodeOneInteger(size).toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(1994792L, result.getTransactionResult().energyUsed);
        
        // Clear this.
        size = 0;
        args = new ABIStreamingEncoder().encodeOneString("writeToObjectArray").encodeOneInteger(size).encodeOneInteger(size).toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertEquals(347032L, result.getTransactionResult().energyUsed);
        
        // Just above the max.
        size = 281;
        args = new ABIStreamingEncoder().encodeOneString("writeToObjectArray").encodeOneInteger(size).encodeOneInteger(size).toBytes();
        result = RULE.call(FROM, target, BigInteger.ZERO, args);
        Assert.assertTrue(result.getTransactionResult().transactionStatus.isFailed());
        Assert.assertEquals(2000000L, result.getTransactionResult().energyUsed);
    }


    private void outputResourcesForTestClass(String outputPath, Class<?> clazz) throws FileNotFoundException, IOException {
        String baseName = clazz.getSimpleName();
        String jarPath = new File(outputPath, baseName + ".jar").toString();
        String abiPath = new File(outputPath, baseName + ".abi").toString();
        
        byte[] optimized = RULE.getDappBytes(clazz, new byte[0]);
        Helpers.writeBytesToFile(optimized, jarPath);
        System.out.println("Wrote: " + jarPath);
        try (FileOutputStream output = new FileOutputStream(abiPath)) {
            ABICompiler.compileJarBytes(JarBuilder.buildJarForMainAndClasses(clazz)).writeAbi(output, ABICompiler.getDefaultVersionNumber());
        }
        System.out.println("Wrote: " + abiPath);
    }
}

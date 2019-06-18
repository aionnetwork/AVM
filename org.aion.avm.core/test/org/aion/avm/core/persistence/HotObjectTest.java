package org.aion.avm.core.persistence;


import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.AvmTransactionUtil;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.FutureResult;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import avm.Address;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.*;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

// todo: AKI-221: enable this once caching has been enabled.
@Ignore
public class HotObjectTest {
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static TestingBlock block;
    private static AvmImpl avm;

    private static long energyLimit = 2_000_000L;
    private static long energyPrice = 1L;

    @Before
    public void setupClass() {
        block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        avm.shutdown();
    }

    /**
     * Demonstrates executing two transactions to the same contract in one block
     * The code cache is invalidated before the execution of the tx3 since both transactions are in the same block
     */
    @Test
    public void executeMultipleTransactionSameBlock() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.adjustBalance(kernel.getMinerAddress(), BigInteger.TEN.pow(10));
        kernel.generateBlock();

        byte[] data = new ABIStreamingEncoder().encodeOneString("doubleStaticValue").toBytes();

        //read from the cache
        Transaction tx2 = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        //read from DB
        Transaction tx3 = AvmTransactionUtil.call(kernel.getMinerAddress(), dappAddress, kernel.getNonce(kernel.getMinerAddress()), BigInteger.ZERO, data, energyLimit, energyPrice);

        FutureResult[] result2 = avm.run(kernel, new Transaction[]{tx2, tx3});
        Assert.assertEquals(20, new ABIDecoder((result2[0].getResult()).copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
        Assert.assertEquals(40, new ABIDecoder((result2[1].getResult()).copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Demonstrates caching will not be used for an earlier block than the dapp's loaded block number
     */
    @Test
    public void executeTransactionEarlierBlock() {
        //setup kernel
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        TestingState kernel1 = new TestingState(block);
        AionAddress dapp1 = deploy(deployer, kernel1, txData);

        TestingState kernel2 = new TestingState(block);
        AionAddress dapp2 = deploy(deployer, kernel2, txData);

        //advance the block number
        kernel2.generateBlock();
        kernel2.generateBlock();

        Assert.assertEquals(dapp1, dapp2);

        TransactionResult result = callDapp(kernel2, deployer, dapp2, "doubleStaticValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        // execute same transaction on an earlier block
        result = callDapp(kernel1, deployer, dapp1, "doubleStaticValue");

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Demonstrates executing same transaction in the same contract in one block
     * The storage is reset between executions and field values are read from DB
     * The code cache is invalidated before the execution of the second transaction
     */
    @Test
    public void executeTransactionSameBlock() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        byte[] objectGraph = kernel.getObjectGraph(dappAddress);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        //reset the object graph
        kernel.putObjectGraph(dappAddress, objectGraph);

        result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Demonstrates field value is read from cache not storage for a new block
     */
    @Test
    public void executeTransactionFromCache() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        byte[] objectGraph = kernel.getObjectGraph(dappAddress);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        //reset the object graph
        kernel.putObjectGraph(dappAddress, objectGraph);
        // reset the transformed code to ensure we're reading from the code cache
        kernel.setTransformedCode(dappAddress, new byte[0]);
        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");
        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Demonstrates the behaviour after revert.
     * Dapp code is stored and read from the cache. Dapp data is not written back
     */
    @Test
    public void executeTransactionRevert() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        kernel.generateBlock();

        // add dapp to cache
        TransactionResult result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddress, "revert");
        Assert.assertTrue(result.transactionStatus.isReverted());

        kernel.generateBlock();

        // reset the transformed code to ensure we're reading from the code cache
        kernel.setTransformedCode(dappAddress, new byte[0]);

        result = callDapp(kernel, deployer, dappAddress, "getStr");
        Assert.assertEquals("initial", new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());
    }

    /**
     * Demonstrates the behaviour after exception.
     * Dapp code is stored and read from the cache. Dapp data is not written back.
     */
    @Test
    public void executeTransactionException() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        kernel.generateBlock();

        // add dapp to cache
        TransactionResult result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddress, "exception");
        Assert.assertEquals(AvmInternalError.FAILED_EXCEPTION.error, result.transactionStatus.causeOfError);

        kernel.generateBlock();

        // reset the transformed code to ensure we're reading from the code cache
        kernel.setTransformedCode(dappAddress, new byte[0]);

        result = callDapp(kernel, deployer, dappAddress, "getStr");
        Assert.assertEquals("initial", new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());
    }

    /**
     * Demonstrates the behaviour after revert in a reentrant call.
     * State of Dapp is not updated in cache after the revert
     */
    @Test
    public void executeReentrantRevert() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] deployData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, deployData);

        kernel.generateBlock();

        // add dapp to cache
        TransactionResult result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        byte[] data = new ABIStreamingEncoder().encodeOneString("revert").toBytes();

        byte[] txData = new ABIStreamingEncoder().encodeOneString("makeCall").encodeOneAddress(new Address(dappAddress.toByteArray())).encodeOneByteArray(data).toBytes();
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddress, "getStr");
        Assert.assertEquals("initial", new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneString());
    }

    /**
     * Demonstrates the behaviour in a reentrant call.
     * State of Dapp is updated in cache
     */
    @Test
    public void executeReentrantSuccess() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] deployData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, deployData);

        kernel.generateBlock();

        // add dapp to cache
        TransactionResult result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        byte[] data = new ABIStreamingEncoder().encodeOneString("doubleStaticValue").toBytes();
        byte[] txData = new ABIStreamingEncoder().encodeOneString("makeCall").encodeOneAddress(new Address(dappAddress.toByteArray())).encodeOneByteArray(data).toBytes();
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddress, "getValue");
        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Demonstrates the difference in performance between reading from data cache and code cache
     */
    @Test
    public void performanceTest() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        //307168 bytes
        byte[] data = new ABIStreamingEncoder().encodeOneString("writeToObjectArray").encodeOneInteger(150).encodeOneInteger(500).toBytes();
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        TransactionResult result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        kernel.generateBlock();

        long cacheResult = measureMethodExecutionTime(kernel, dappAddress, "getValue", true, 5);
        long DBResult = measureMethodExecutionTime(kernel, dappAddress, "getValue", false, 5);

        System.out.println("Avg execution time (307168 serialized bytes) using code cache: " + DBResult + ", data cache: " + cacheResult + "(ns). code/data cache ratio: " + (double) DBResult / cacheResult);


        // 8308 bytes
        data = new ABIStreamingEncoder().encodeOneString("writeToObjectArray").encodeOneInteger(15).encodeOneInteger(55).toBytes();
        tx = AvmTransactionUtil.call(deployer, dappAddress, kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        kernel.generateBlock();

        cacheResult = measureMethodExecutionTime(kernel, dappAddress, "getValue", true, 5);
        DBResult = measureMethodExecutionTime(kernel, dappAddress, "getValue", false, 5);

        System.out.println("Avg execution time (8308 serialized bytes) using code cache: " + DBResult + ", data cache: " + cacheResult + "(ns). code/data cache ratio: " + (double) DBResult / cacheResult);
    }

    /**
     * Demonstrates the case one contract is making an internal call and updates another contract. All the data is read from the cache
     */
    @Test
    public void callOtherContractInCache() {
        int length = 2;
        AionAddress[] dappAddresses = new AionAddress[length];
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] deployData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);

        for (int i = 0; i < length; i++) {
            AionAddress deployer = Helpers.randomAddress();
            kernel.adjustBalance(deployer, BigInteger.TEN.pow(20));
            dappAddresses[i] = deploy(deployer, kernel, deployData);
            //add to cache
            TransactionResult result = callDapp(kernel, deployer, dappAddresses[i], "getValue");
            Assert.assertEquals(5, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
            kernel.generateBlock();
        }

        byte[] objectGraph = kernel.getObjectGraph(dappAddresses[1]);

        // update the cache
        byte[] data = new ABIStreamingEncoder().encodeOneString("doubleStaticValue").toBytes();
        byte[] txData = new ABIStreamingEncoder().encodeOneString("makeCall").encodeOneAddress(new Address(dappAddresses[1].toByteArray())).encodeOneByteArray(data).toBytes();
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddresses[0], kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionResult result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        kernel.generateBlock();

        //reset the object graph and transformed code to ensure data is read form cache
        for (int i = 0; i < length; i++) {
            kernel.putObjectGraph(dappAddresses[i], objectGraph);
            kernel.setTransformedCode(dappAddresses[i], new byte[0]);
        }

        //read the cache
        result = callDapp(kernel, deployer, dappAddresses[1], "getValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddresses[0], "getValue");
        Assert.assertEquals(5, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Demonstrates the case where 2 contracts make internal calls and update each other. All the data is read from the cache
     */
    @Test
    public void InternalCallsBetween2Contracts() {
        int length = 2;
        AionAddress[] dappAddresses = new AionAddress[length];
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] deployData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);

        for (int i = 0; i < length; i++) {
            AionAddress deployer = Helpers.randomAddress();
            kernel.adjustBalance(deployer, BigInteger.TEN.pow(20));
            dappAddresses[i] = deploy(deployer, kernel, deployData);
            //add to cache
            TransactionResult result = callDapp(kernel, deployer, dappAddresses[i], "getValue");
            Assert.assertEquals(5, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
            kernel.generateBlock();
        }

        byte[] objectGraph = kernel.getObjectGraph(dappAddresses[0]);

        // update the cache
        byte[] data = new ABIStreamingEncoder().encodeOneString("doubleStaticValue").toBytes();
        byte[] dataForCallFrom2To1 = new ABIStreamingEncoder().encodeOneString("makeCall").encodeOneAddress(new Address(dappAddresses[0].toByteArray())).encodeOneByteArray(data).toBytes();

        byte[] ForCallFrom1To2 = new ABIStreamingEncoder().encodeOneString("makeCall").encodeOneAddress(new Address(dappAddresses[1].toByteArray())).encodeOneByteArray(dataForCallFrom2To1).toBytes();
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddresses[0], kernel.getNonce(deployer), BigInteger.ZERO, ForCallFrom1To2, energyLimit, energyPrice);
        TransactionResult result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        kernel.generateBlock();

        //reset the object graph and transformed code to ensure data is read form cache
        for (int i = 0; i < length; i++) {
            kernel.putObjectGraph(dappAddresses[i], objectGraph);
            kernel.setTransformedCode(dappAddresses[i], new byte[0]);
        }

        //read the cache
        result = callDapp(kernel, deployer, dappAddresses[1], "getCallCount");
        Assert.assertEquals(1, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddresses[0], "getCallCount");
        Assert.assertEquals(1, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddresses[0], "getValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Executes and caches the result of array modifications and reads from the cache while executing transactions concurrently
     */
    @Test
    public void readConcurrentlyFromCache() {
        int length = 4;
        AionAddress[] dappAddresses = new AionAddress[length];
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);

        Transaction[] txs = new Transaction[length];
        byte[] readIntArrayData = new ABIStreamingEncoder().encodeOneString("readIntArray").toBytes();

        for (int i = 0; i < length; i++) {
            AionAddress deployer = Helpers.randomAddress();
            kernel.adjustBalance(deployer, BigInteger.TEN.pow(20));
            dappAddresses[i] = deploy(deployer, kernel, txData);

            //setup concurrent transactions
            txs[i] = AvmTransactionUtil.call(deployer, dappAddresses[i], kernel.getNonce(deployer), BigInteger.ZERO, readIntArrayData, energyLimit, energyPrice);
        }

        // add to cache
        for (int i = 0; i < length; i++) {
            byte[] data = new ABIStreamingEncoder().encodeOneString("updateIntArray").encodeOneInteger(i).toBytes();
            Transaction tx = AvmTransactionUtil.call(deployer, dappAddresses[i], kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
            TransactionResult result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
            Assert.assertTrue(result.transactionStatus.isSuccess());
            kernel.generateBlock();
        }

        // read from cache while executing concurrently
        FutureResult[] results = avm.run(kernel, txs);

        for (int i = 0; i < length; i++) {
            TransactionResult r = results[i].getResult();
            Assert.assertTrue(r.transactionStatus.isSuccess());
            int[] expectedResult = new int[length];
            expectedResult[i] = 10;
            Assert.assertArrayEquals(expectedResult, new ABIDecoder(r.copyOfTransactionOutput().orElseThrow()).decodeOneIntegerArray());
        }
    }

    /**
     * Executes array modifications for different contracts concurrently, and caches the result. Then reads from the cache.
     */
    @Test
    public void addToCacheFromConcurrentExecutor() {
        int length = 4;
        AionAddress[] dappAddresses = new AionAddress[length];
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        Transaction[] txs = new Transaction[length];

        for (int i = 0; i < length; i++) {
            AionAddress deployer = Helpers.randomAddress();
            kernel.adjustBalance(deployer, BigInteger.TEN.pow(20));
            dappAddresses[i] = deploy(deployer, kernel, txData);
            // add to cache
            byte[] data = new ABIStreamingEncoder().encodeOneString("updateIntArray").encodeOneInteger(i).toBytes();
            txs[i] = AvmTransactionUtil.call(deployer, dappAddresses[i], kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
        }

        FutureResult[] results = avm.run(kernel, txs);

        for (int i = 0; i < length; i++) {
            TransactionResult r = results[i].getResult();
            Assert.assertTrue(r.transactionStatus.isSuccess());
        }

        kernel.generateBlock();

        for (int i = 0; i < length; i++) {
            byte[] data = new ABIStreamingEncoder().encodeOneString("readIntArray").toBytes();
            Transaction tx = AvmTransactionUtil.call(deployer, dappAddresses[i], kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
            TransactionResult result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
            Assert.assertTrue(result.transactionStatus.isSuccess());
            int[] expectedResult = new int[length];
            expectedResult[i] = 10;
            Assert.assertArrayEquals(expectedResult, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneIntegerArray());
            kernel.generateBlock();
        }
    }

    //todo Will be updated after the fix to AddressResourceMonitor
    @Ignore
    @Test
    public void abortException() {
        int length = 4;
        AionAddress[] dappAddresses = new AionAddress[length + 1];
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] deployData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);

        byte[] dataForInternalCall = new ABIStreamingEncoder().encodeOneString("doubleStaticValue").toBytes();
        Transaction[] txs = new Transaction[length];

        for (int i = 0; i < length + 1; i++) {
            AionAddress deployer = Helpers.randomAddress();
            kernel.adjustBalance(deployer, BigInteger.TEN.pow(20));
            dappAddresses[i] = deploy(deployer, kernel, deployData);
            System.out.println(dappAddresses[i]);
            if (i > 0) {
                byte[] txData = new ABIStreamingEncoder().encodeOneString("makeCall").encodeOneAddress(new Address(dappAddresses[i].toByteArray())).encodeOneByteArray(dataForInternalCall).toBytes();
                txs[i - 1] = AvmTransactionUtil.call(deployer, dappAddresses[i - 1], kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
            }
        }

        FutureResult[] results = avm.run(kernel, txs);

        for (int i = 0; i < length; i++) {
            TransactionResult r = results[i].getResult();
            Assert.assertTrue(r.transactionStatus.isSuccess());
        }

        kernel.generateBlock();

        byte[] data = new ABIStreamingEncoder().encodeOneString("getValue").toBytes();
        for (int i = 1; i < length; i++) {
            Transaction tx = AvmTransactionUtil.call(deployer, dappAddresses[i], kernel.getNonce(deployer), BigInteger.ZERO, data, energyLimit, energyPrice);
            TransactionResult result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
            Assert.assertTrue(result.transactionStatus.isSuccess());
            Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
        }
    }

    @Test
    public void selfDestructThenSendTransaction() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        TransactionResult result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        callDapp(kernel, deployer, dappAddress, "selfDestruct");

        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddress, "doubleStaticValue");
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertNull(result.copyOfTransactionOutput().orElseThrow());
    }

    /**
     * Demonstrates a cross call which fails. The dapp code is read from the cache but the data is cleared
     */
    @Test
    public void callOtherContractInCacheAndFail() {
        int length = 2;
        AionAddress[] dappAddresses = new AionAddress[length];
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] deployData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);

        for (int i = 0; i < length; i++) {
            AionAddress deployer = Helpers.randomAddress();
            kernel.adjustBalance(deployer, BigInteger.TEN.pow(20));
            dappAddresses[i] = deploy(deployer, kernel, deployData);
            //add to cache
            TransactionResult result = callDapp(kernel, deployer, dappAddresses[i], "getValue");
            Assert.assertEquals(5, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
            kernel.generateBlock();
        }

        // save the current state of the object graph. this will be used later to ensure the value is not read from the cache
        byte[] objectGraph = kernel.getObjectGraph(dappAddresses[1]);

        TransactionResult result = callDapp(kernel, deployer, dappAddresses[1], "doubleStaticValue");
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        // update the cache
        byte[] data = new ABIStreamingEncoder().encodeOneString("revert").toBytes();
        byte[] txData = new ABIStreamingEncoder().encodeOneString("makeCall").encodeOneAddress(new Address(dappAddresses[1].toByteArray())).encodeOneByteArray(data).toBytes();
        Transaction tx = AvmTransactionUtil.call(deployer, dappAddresses[0], kernel.getNonce(deployer), BigInteger.ZERO, txData, energyLimit, energyPrice);
        result = avm.run(kernel, new Transaction[]{tx})[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());

        kernel.generateBlock();

        //reset the object graph and transformed code to ensure data is read form cache
        for (int i = 0; i < length; i++) {
            // object graph at this stage, had the initial value of 5
            kernel.putObjectGraph(dappAddresses[i], objectGraph);
            kernel.setTransformedCode(dappAddresses[i], new byte[0]);
        }

        //read the cache
        result = callDapp(kernel, deployer, dappAddresses[1], "getValue");
        Assert.assertEquals(5, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        result = callDapp(kernel, deployer, dappAddresses[0], "getValue");
        Assert.assertEquals(5, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    private static long measureMethodExecutionTime(TestingState kernel, AionAddress dappAddress, String methodName, boolean generateBlock, int rounds) {
        long totalDuration = 0l;
        for (int i = 0; i < rounds; ++i) {
            long start = System.nanoTime();
            TransactionResult result = callDapp(kernel, deployer, dappAddress, methodName);
            totalDuration += (System.nanoTime() - start);
            Assert.assertTrue(result.transactionStatus.isSuccess());
            if (generateBlock && i != rounds - 1) {
                kernel.generateBlock();
            }
        }
        return totalDuration / rounds;
    }

    private static AionAddress deploy(AionAddress deployer, TestingState kernel, byte[] txData) {
        Transaction tx1 = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, 5_000_000, energyPrice);
        TransactionResult result = avm.run(kernel, new Transaction[]{tx1})[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
        return new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }

    private static TransactionResult callDapp(TestingState kernel, AionAddress sender, AionAddress dappAddress, String methodName, Object... args) {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString(methodName);
        for (Object arg : args) {
            encoder.encodeOneByteArray((byte[]) arg);
        }
        byte[] data = encoder.toBytes();
        Transaction tx = AvmTransactionUtil.call(sender, dappAddress, kernel.getNonce(sender), BigInteger.ZERO, data, energyLimit, energyPrice);
        return avm.run(kernel, new Transaction[]{tx})[0].getResult();
    }
}

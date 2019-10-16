package org.aion.avm.core.persistence;

import avm.Address;
import org.aion.avm.core.*;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

public class TransformedCodeCacheTest {
    private static AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private static TestingBlock block;
    private static AvmImpl avm;

    @Before
    public void setupClass() {
        block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @After
    public void tearDown() {
        avm.shutdown();
    }

    @Test
    public void notWriteToCacheAfterSuccessfulTransactionTest() {
        TestingState kernel = new TestingState(block);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        kernel.generateBlock();

        // remove the transformed code. This means the avm version has changed.
        // transformed code cache is empty.
        kernel.setTransformedCode(dappAddress, null);

        // this transaction will force a re-transformation operation to be done
        TransactionResult result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 500_000);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        // transformed code should be written to the database
        Assert.assertNotNull(kernel.getTransformedCode(dappAddress));
        kernel.generateBlock();

        //validate in a successful transaction cache is not updated
        //This should force the re-transformation step to be done again since the cache is empty. Code is set to byte[0] to make re-transformation to fail.
        kernel.putCode(dappAddress, new byte[0]);
        kernel.setTransformedCode(dappAddress, null);

        //since it's not reading from the cache the result of the operation is failed
        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 500_000);

        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertEquals("Failed: re-transformation failure", result.transactionStatus.causeOfError);
    }

    @Test
    public void writeToCacheAfterFailedTransactionTest() {
        TestingState kernel = new TestingState(block);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        kernel.generateBlock();

        // remove the transformed code. This means the avm version has changed.
        // transformed code cache is empty.
        kernel.setTransformedCode(dappAddress, null);

        // this transaction will run out of energy. It should write back the transformed code to cache
        TransactionResult result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 25_000);

        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertEquals("Failed: out of energy", result.transactionStatus.causeOfError);
        Assert.assertNull(kernel.getTransformedCode(dappAddress));

        //this should read from the cache. Code is set to byte[0] to make sure re-transformation step is not performed
        kernel.putCode(dappAddress, new byte[0]);
        kernel.generateBlock();

        //since it's reading from the cache the result of the operation is success
        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 2_000_000);

        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        // since the transaction is successful, the transformed code should not be written back to the cache
        kernel.putCode(dappAddress, new byte[0]);
        kernel.setTransformedCode(dappAddress, null);

        //since it's not reading from the cache the result of the operation is failed
        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 500_000);

        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertEquals("Failed: re-transformation failure", result.transactionStatus.causeOfError);
    }

    @Test
    public void reorganizationTest() {
        TestingState kernel = new TestingState(block);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        // remove the transformed code. This means the avm version has changed.
        // transformed code cache is empty.
        kernel.setTransformedCode(dappAddress, null);

        // this transaction will run out of energy. It should write back the transformed code to cache
        TransactionResult result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 25_000);

        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertEquals("Failed: out of energy", result.transactionStatus.causeOfError);
        Assert.assertNull(kernel.getTransformedCode(dappAddress));

        //If the chain the reorganizes and the transformed code for Avm1 is present, that should be used
        kernel.setTransformedCode(dappAddress, new byte[0]);

        //byte[0] as the transformed code will return null as a dApp, thus no code is executed
        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 2_000_000);

        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    @Test
    public void internalTransactionTest() {
        TestingState kernel = new TestingState(block);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress1 = deploy(deployer, kernel, txData);
        AionAddress dappAddress2 = deploy(deployer, kernel, txData);

        // remove the transformed code. This means the avm version has changed.
        // transformed code cache is empty.
        kernel.setTransformedCode(dappAddress1, null);
        kernel.setTransformedCode(dappAddress2, null);

        // this transaction will run out of energy. It should write back the transformed code to cache
        TransactionResult result = callDappNoArgument(kernel, deployer, dappAddress2, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 25_000);

        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertNull(kernel.getTransformedCode(dappAddress2));

        //this ensures that only the cache is read and re-transformation is not done
        kernel.putCode(dappAddress2, new byte[0]);

        byte[] data = new ABIStreamingEncoder().encodeOneString("revert").toBytes();
        byte[] encodedData = new ABIStreamingEncoder().encodeOneString("makeCall")
                .encodeOneAddress(new Address(dappAddress2.toByteArray()))
                .encodeOneByteArray(data).toBytes();

        result = callDappArguments(kernel, deployer, dappAddress1, encodedData);

        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertNull(kernel.getTransformedCode(dappAddress2));
    }

    @Test
    public void selfDestructTest() {
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TestingState kernel = new TestingState(block);
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        kernel.generateBlock();
        kernel.setTransformedCode(dappAddress, null);

        // transformed code is added to the cache
        callDappNoArgument(kernel, deployer, dappAddress, "selfDestruct", ExecutionType.ASSUME_MAINCHAIN, 2_000_000);

        Assert.assertNull(kernel.getTransformedCode(dappAddress));
        Assert.assertNull(kernel.getCode(dappAddress));
        kernel.generateBlock();

        TransactionResult result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 2_000_000);

        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    @Test
    public void notWriteToCacheAfterEthCallTest() {
        TestingState kernel = new TestingState(block);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        kernel.generateBlock();

        // remove the transformed code. This means the avm version has changed.
        // transformed code cache is empty.
        kernel.setTransformedCode(dappAddress, null);

        // this transaction will force a re-transformation operation to be done
        TransactionResult result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ETH_CALL, 500_000);
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        //validate in a successful transaction cache is not updated
        //This should force the re-transformation step to be done again since the cache is empty. Code is set to byte[0] to make re-transformation to fail.
        kernel.putCode(dappAddress, new byte[0]);
        kernel.setTransformedCode(dappAddress, null);

        //since it's not reading from the cache the result of the operation is failed
        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 500_000);

        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertEquals("Failed: re-transformation failure", result.transactionStatus.causeOfError);
    }

    @Test
    public void writeBackToCacheAfterEthCallTest() {
        TestingState kernel = new TestingState(block);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        kernel.generateBlock();

        // remove the transformed code. This means the avm version has changed.
        // transformed code cache is empty.
        kernel.setTransformedCode(dappAddress, null);

        TransactionResult result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 25_000);
        Assert.assertTrue(result.transactionStatus.isFailed());

        // this transaction will read from the cache and write back
        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ETH_CALL, 500_000);
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.generateBlock();

        // Code is set to byte[0] to make re-transformation is not performed.
        kernel.putCode(dappAddress, new byte[0]);
        kernel.setTransformedCode(dappAddress, null);

        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 500_000);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void writeBackToCacheAfterMiningTest() {
        TestingState kernel = new TestingState(block);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        kernel.generateBlock();

        // remove the transformed code. This means the avm version has changed.
        // transformed code cache is empty.
        kernel.setTransformedCode(dappAddress, null);

        // this transaction will run out of energy. It should write back the transformed code to cache
        TransactionResult result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 25_000);

        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertNull(kernel.getTransformedCode(dappAddress));

        kernel.generateBlock();
        kernel.putCode(dappAddress, new byte[0]);

        // this transaction must read from the cache, thus should succeed
        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.MINING, 500_000);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        kernel.setTransformedCode(dappAddress, null);

        // this transaction must read from the cache, thus should succeed
        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_SIDECHAIN, 500_000);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    @Test
    public void deepSidechainTest() {
        TestingState kernel = new TestingState(block);
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, kernel, txData);

        kernel.generateBlock();

        // remove the transformed code. This means the avm version has changed.
        // transformed code cache is empty.
        kernel.setTransformedCode(dappAddress, null);

        // this transaction will run out of energy. It should write back the transformed code to cache
        TransactionResult result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_MAINCHAIN, 25_000);

        Assert.assertTrue(result.transactionStatus.isFailed());
        Assert.assertNull(kernel.getTransformedCode(dappAddress));

        kernel.generateBlock();
        kernel.putCode(dappAddress, new byte[0]);

        //since it's not reading from the cache the result of the operation is failed
        result = callDappNoArgument(kernel, deployer, dappAddress, "doubleStaticValue", ExecutionType.ASSUME_DEEP_SIDECHAIN, 500_000);
        Assert.assertTrue(result.transactionStatus.isFailed());
    }

    private static AionAddress deploy(AionAddress deployer, TestingState kernel, byte[] txData) {
        Transaction tx1 = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, 5_000_000, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{tx1}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
        return new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }

    private static TransactionResult callDappNoArgument(TestingState kernel, AionAddress sender, AionAddress dappAddress, String methodName, ExecutionType executionType, long energyLimit) {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString(methodName);
        byte[] data = encoder.toBytes();
        long commonBlockNumber = executionType == ExecutionType.ASSUME_DEEP_SIDECHAIN ? 0 : kernel.getBlockNumber() - 1;
        Transaction tx = AvmTransactionUtil.call(sender, dappAddress, kernel.getNonce(sender), BigInteger.ZERO, data, energyLimit, 1);
        return avm.run(kernel, new Transaction[]{tx}, executionType, commonBlockNumber)[0].getResult();
    }

    private static TransactionResult callDappArguments(TestingState kernel, AionAddress sender, AionAddress dappAddress, byte[] encodedData) {
        Transaction tx = AvmTransactionUtil.call(sender, dappAddress, kernel.getNonce(sender), BigInteger.ZERO, encodedData, (long) 2_000_000, 1);
        return avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
    }
}

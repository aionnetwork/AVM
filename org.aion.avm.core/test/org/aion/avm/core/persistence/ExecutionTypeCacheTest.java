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
import org.aion.parallel.TestContract;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertTrue;

//todo AKI-232: test cases related to ethcall can be updated

public class ExecutionTypeCacheTest {

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

    /**
     * Tests the case where the code cache block is after the common ancestor. Thus, caching will not be used.
     */
    @Test
    public void sidechainInvalidCodeCache() {
        TestingState sidechainKernel = new TestingState(block);
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);
        deploy(deployer, sidechainKernel, txData);

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        long commonParent = mainchainKernel.getBlockNumber();

        mainchainKernel.generateBlock();

        //add to cache on mainchain
        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        // generate sidechain block with parent on mainchain
        sidechainKernel.generateBlock();

        // reset the transformed code on the kernel
        sidechainKernel.setTransformedCode(dappAddress, new byte[0]);

        //since it's not reading from the cache the result of the revert operation is success
        result = callDapp(sidechainKernel, deployer, dappAddress, "revert",
                ExecutionType.ASSUME_SIDECHAIN, commonParent);

        Assert.assertTrue(result.transactionStatus.isSuccess());

        // validate mainchain cache is valid
        mainchainKernel.generateBlock();
        // reset to ensure cache is used
        mainchainKernel.setTransformedCode(dappAddress, new byte[0]);
        mainchainKernel.putObjectGraph(dappAddress, new byte[0]);

        //read from the cache to make sure it's not corrupted
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Tests the case where both code and data cache have been put before the common ancestor.
     * They can both be used but only the code is written back.
     */
    @Test
    public void sidechainValidCodeCacheValidDataCache() {
        TestingState sidechainKernel = new TestingState(block);
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);
        deploy(deployer, sidechainKernel, txData);

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        //add to cache on mainchain
        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        sidechainKernel.generateBlock();
        mainchainKernel.generateBlock();
        long commonParent = mainchainKernel.getBlockNumber();

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        // reset the transformed code on the kernel
        sidechainKernel.setTransformedCode(dappAddress, new byte[0]);

        // it should only read the code form cache
        result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_SIDECHAIN, commonParent);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();

        //update the data cache on mainchain
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Tests the case where the code cache block number is below and the data cache block number is after the common ancestor
     * Code cache will be used but data cache will not be.
     * At the end of the transaction dapp code will be written back to the cache
     */
    @Test
    public void sidechainValidCodeCacheInvalidDataCache() {
        TestingState sidechainKernel = new TestingState(block);
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);
        deploy(deployer, sidechainKernel, txData);

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        //add to cache on mainchain
        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        sidechainKernel.generateBlock();
        mainchainKernel.generateBlock();
        long commonParent = mainchainKernel.getBlockNumber();

        mainchainKernel.generateBlock();

        //update the data cache on mainchain
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        sidechainKernel.generateBlock();

        // reset the transformed code on the kernel
        sidechainKernel.setTransformedCode(dappAddress, new byte[0]);

        // it should only read the code form cache
        result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_SIDECHAIN, commonParent);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        // reset the transformed code on the kernel
        mainchainKernel.setTransformedCode(dappAddress, new byte[0]);
        mainchainKernel.generateBlock();

        //update the data cache on mainchain
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(40, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Tests the case where dapp is not included in the cache.
     * After the transaction cache will not be updated.
     */
    @Test
    public void sidechainNotInCache() {
        TestingState sidechainKernel = new TestingState(block);
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);
        deploy(deployer, sidechainKernel, txData);

        sidechainKernel.generateBlock();
        mainchainKernel.generateBlock();
        long commonParent = mainchainKernel.getBlockNumber();

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        // it should only read the code form cache
        TransactionResult result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_SIDECHAIN, commonParent);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();

        //update the data cache on mainchain
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * It sequentially adds mainchain and deep sidechain blocks.
     * Sidechain blocks do not interact with cache while mainchain blocks are stored in both data and code cache
     */
    @Test
    public void multipleDeepSidechainBlockTest() {
        TestingState sidechainKernel = new TestingState(block);
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);
        deploy(deployer, sidechainKernel, txData);

        //add to cache
        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();


        //update cache
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_DEEP_SIDECHAIN, 0);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);
        Assert.assertEquals(40, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_DEEP_SIDECHAIN, 0);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);
        Assert.assertEquals(80, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Tests the case where mining is done after a mainchain import
     * ensures code is read from cache and written back
     */
    @Test
    public void miningMainchainMixtureTest() {
        TestingState miningKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, miningKernel, txData);

        miningKernel.generateBlock();

        TransactionResult result = callDapp(miningKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.MINING, miningKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        miningKernel.generateBlock();

        // add to cache
        result = callDapp(miningKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, miningKernel.getBlockNumber() - 1);
        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        miningKernel.generateBlock();
        // make sure it's read from the cache
        miningKernel.setTransformedCode(dappAddress, new byte[0]);

        result = callDapp(miningKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.MINING, miningKernel.getBlockNumber() - 1);

        Assert.assertEquals(40, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        miningKernel.generateBlock();
        // make sure it was written back to cache
        miningKernel.setTransformedCode(dappAddress, new byte[0]);

        result = callDapp(miningKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, miningKernel.getBlockNumber() - 1);
        Assert.assertEquals(80, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Tests the case where mining is done after a mainchain import
     * ensures data is not written back to the cache
     */
    @Test
    public void miningMainchainMixtureNoDataTest() {
        TestingState miningKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, miningKernel, txData);

        miningKernel.generateBlock();

        byte[] graph = miningKernel.getObjectGraph(dappAddress);

        TransactionResult result = callDapp(miningKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.MINING, miningKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        miningKernel.generateBlock();

        // add to cache
        result = callDapp(miningKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, miningKernel.getBlockNumber() - 1);
        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        miningKernel.generateBlock();
        // make sure it's read from the cache
        miningKernel.setTransformedCode(dappAddress, new byte[0]);
        // reset the data in db, to ensure cache data is not read
        miningKernel.putObjectGraph(dappAddress, graph);

        result = callDapp(miningKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.MINING, miningKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        miningKernel.generateBlock();
        // make sure it was wriiten back to cache
        miningKernel.setTransformedCode(dappAddress, new byte[0]);
        // rest the data to ensure data is not read from the cache
        miningKernel.putObjectGraph(dappAddress, graph);

        result = callDapp(miningKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, miningKernel.getBlockNumber() - 1);
        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        miningKernel.generateBlock();
        // make sure it was wriiten back to cache
        miningKernel.setTransformedCode(dappAddress, new byte[0]);
        // rest the data to ensure data is not read from the cache
        miningKernel.putObjectGraph(dappAddress, graph);

        result = callDapp(miningKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, miningKernel.getBlockNumber() - 1);
        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * It simulates running one transaction in multiple blocks during mining.
     * Mining blocks do not interact with cache.
     */
    @Test
    public void sameMiningBlockTest() {
        TestingState miningKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, miningKernel, txData);

        miningKernel.generateBlock();
        AionAddress sender = Helpers.randomAddress();
        miningKernel.adjustBalance(sender, BigInteger.TEN.pow(20));

        miningKernel.generateBlock();
        byte[] graph = miningKernel.getObjectGraph(dappAddress);

        TransactionResult result = callDapp(miningKernel, sender, dappAddress, "doubleStaticValue",
                ExecutionType.MINING, miningKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        miningKernel.generateBlock();

        miningKernel.deleteAccount(sender);
        miningKernel.adjustBalance(sender, BigInteger.TEN.pow(20));

        miningKernel.putObjectGraph(dappAddress, graph);

        result = callDapp(miningKernel, sender, dappAddress, "doubleStaticValue",
                ExecutionType.MINING, miningKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Builds mainchain and sidechain simultaneously. Then marks the sidechain as the new mainchain
     */
    @Test
    public void switchMainchainTest() {
        TestingState sidechainKernel = new TestingState(block);
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);
        deploy(deployer, sidechainKernel, txData);

        //add to cache for mainchain kernel
        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        //add to cache for sidechain kernel
        result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, sidechainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        long commonParent = mainchainKernel.getBlockNumber();
        //fork
        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        //update cache for mainchain kernel
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        //update cache for mainchain kernel
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);
        Assert.assertEquals(40, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        //update db for sidechain kernel
        result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_DEEP_SIDECHAIN, 0);
        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        sidechainKernel.generateBlock();
        //ensure cache is used
        sidechainKernel.setTransformedCode(dappAddress, new byte[0]);

        //mark the sidechain as the new mainchain
        result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.SWITCHING_MAINCHAIN, commonParent);
        Assert.assertEquals(40, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Builds mainchain and sidechain simultaneously. Contract is selfdestructed on the sidechain but still remains in the cache
     */
    @Test
    public void switchMainchainSelfDestructTest() {
        TestingState sidechainKernel = new TestingState(block);
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);
        deploy(deployer, sidechainKernel, txData);

        //add to cache for mainchain kernel
        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        //add to cache for sidechain kernel
        result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, sidechainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        long commonParent = mainchainKernel.getBlockNumber();
        //fork
        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        //update cache for mainchain kernel
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();
        sidechainKernel.generateBlock();

        //update cache for mainchain kernel
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);
        Assert.assertEquals(40, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        //update db for sidechain kernel
        result = callDapp(sidechainKernel, deployer, dappAddress, "selfDestruct",
                ExecutionType.ASSUME_DEEP_SIDECHAIN, 0);
        Assert.assertTrue(result.transactionStatus.isSuccess());

        sidechainKernel.generateBlock();

        //mark the sidechain as the new mainchain
        result = callDapp(sidechainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.SWITCHING_MAINCHAIN, commonParent);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    /**
     * Tests ethcall without valid cache. Dapp code will not be written back to the cache
     */
    @Test
    public void ethCallWithoutCacheValidateCodeNotWrittenBack() {
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);

        mainchainKernel.generateBlock();

        mainchainKernel.generateBlock();

        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ETH_CALL, mainchainKernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());

        mainchainKernel.generateBlock();
        // reset the transformed code and object graph on the kernel
        mainchainKernel.setTransformedCode(dappAddress, new byte[0]);

        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    /**
     * Tests ethcall without valid cache. Dapp data will not be written back to the cache
     */
    @Test
    public void ethCallWithoutCacheValidateDataNotWrittenBack() {
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);

        mainchainKernel.generateBlock();

        byte[] graph = mainchainKernel.getObjectGraph(dappAddress);
        mainchainKernel.generateBlock();

        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ETH_CALL, mainchainKernel.getBlockNumber() - 1);
        Assert.assertTrue(result.transactionStatus.isSuccess());

        mainchainKernel.generateBlock();
        // reset the transformed code and object graph on the kernel
        mainchainKernel.putObjectGraph(dappAddress, graph);

        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Tests the case where the code cache block is after the common ancestor. Thus, caching will not be used.
     */
    @Test
    public void ethCallInvalidCodeCache() {
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);

        mainchainKernel.generateBlock();
        long commonParent = mainchainKernel.getBlockNumber();

        mainchainKernel.generateBlock();
        mainchainKernel.generateBlock();

        //add to cache on mainchain
        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        // generate sidechain block with parent on mainchain
        mainchainKernel.generateBlock();

        // reset the transformed code on the kernel
        mainchainKernel.setTransformedCode(dappAddress, new byte[0]);

        //since it's not reading from the cache the result of the revert operation is success
        result = callDapp(mainchainKernel, deployer, dappAddress, "revert",
                ExecutionType.ETH_CALL, commonParent);

        Assert.assertTrue(result.transactionStatus.isSuccess());

        // validate mainchain cache is valid
        mainchainKernel.generateBlock();
        // reset to ensure cache is used
        mainchainKernel.setTransformedCode(dappAddress, new byte[0]);
        mainchainKernel.putObjectGraph(dappAddress, new byte[0]);

        //read from the cache to make sure it's not corrupted
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Tests the case where both code and data cache have been put before the common ancestor.
     * They can both be used but only the code is written back.
     */
    @Test
    public void ethCallValidCodeCacheValidDataCache() {
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);

        mainchainKernel.generateBlock();

        //add to cache on mainchain
        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();
        long commonParent = mainchainKernel.getBlockNumber();

        mainchainKernel.generateBlock();
        mainchainKernel.generateBlock();

        // reset the transformed code on the kernel
        mainchainKernel.setTransformedCode(dappAddress, new byte[0]);
        byte[] graph = mainchainKernel.getObjectGraph(dappAddress);
        mainchainKernel.putObjectGraph(dappAddress, new byte[0]);

        // it should only read the code form cache
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ETH_CALL, commonParent);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();
        mainchainKernel.setTransformedCode(dappAddress, new byte[0]);
        // rest the graph to before the cll
        mainchainKernel.putObjectGraph(dappAddress, graph);

        //update the data cache on mainchain
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Tests the case where the code cache block number is below and the data cache block number is after the common ancestor
     * Code cache will be used but data cache will not be.
     * At the end of the transaction dapp code will be written back to the cache
     */
    @Test
    public void ethCallValidCodeCacheInvalidDataCache() {
        TestingState mainchainKernel = new TestingState(block);

        //deploy to both
        byte[] jar = JarBuilder.buildJarForMainAndClassesAndUserlib(HotObjectContract.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        AionAddress dappAddress = deploy(deployer, mainchainKernel, txData);

        mainchainKernel.generateBlock();

        //add to cache on mainchain
        TransactionResult result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(10, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();
        long commonParent = mainchainKernel.getBlockNumber();

        mainchainKernel.generateBlock();
        byte[] graphEthCallBlock = mainchainKernel.getObjectGraph(dappAddress);

        //update the data cache on mainchain
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();

        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(40, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        mainchainKernel.generateBlock();
        byte[] graphMainChainLatest = mainchainKernel.getObjectGraph(dappAddress);

        // reset the transformed code on the kernel
        mainchainKernel.setTransformedCode(dappAddress, new byte[0]);
        // rest database to the common parent state. cache reflects value = 40 now and the db reflects value = 10
        mainchainKernel.putObjectGraph(dappAddress, graphEthCallBlock);

        // it should only read the code form cache
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ETH_CALL, commonParent);

        Assert.assertEquals(20, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());

        // reset the transformed code on the kernel
        mainchainKernel.setTransformedCode(dappAddress, new byte[0]);
        // make sure the db with value = 40 is read and cache is not updated after eth_call
        mainchainKernel.putObjectGraph(dappAddress, graphMainChainLatest);
        mainchainKernel.generateBlock();

        //update the data cache on mainchain
        result = callDapp(mainchainKernel, deployer, dappAddress, "doubleStaticValue",
                ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);

        Assert.assertEquals(80, new ABIDecoder(result.copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
    }

    /**
     * Tests executing sidechain transactions concurrently. all dapps are put back in the cache
     */
    @Test
    public void concurrentSidechainExecution() {
        byte[] code = JarBuilder.buildJarForMainAndClassesAndUserlib(TestContract.class);

        TestingState mainchainKernel = new TestingState(block);
        TestingState sidechainKernel = new TestingState(block);

        int length = 4;
        AionAddress[] user = new AionAddress[length];
        Transaction[] ctx = new Transaction[length];
        for (int i = 0; i < length; i++) {
            user[i] = Helpers.randomAddress();
            mainchainKernel.adjustBalance(user[i], BigInteger.TEN.pow(20));
            sidechainKernel.adjustBalance(user[i], BigInteger.TEN.pow(20));
            ctx[i] = AvmTransactionUtil.create(user[i], BigInteger.ZERO, BigInteger.ZERO, new CodeAndArguments(code, null).encodeToBytes(), 5_000_000L, 1);
        }


        FutureResult[] results = avm.run(mainchainKernel, ctx, ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);
        AionAddress[] contractAddresses = new AionAddress[results.length];
        for (int i = 0; i < results.length; i++) {
            contractAddresses[i] = new AionAddress(results[i].getResult().copyOfTransactionOutput().orElseThrow());
        }

        results = avm.run(sidechainKernel, ctx, ExecutionType.ASSUME_MAINCHAIN, sidechainKernel.getBlockNumber() - 1);

        for (int i = 0; i < results.length; i++) {
            contractAddresses[i] = new AionAddress(results[i].getResult().copyOfTransactionOutput().orElseThrow());
        }

        long commonParent = mainchainKernel.getBlockNumber();
        mainchainKernel.generateBlock();

        Transaction[] tx = new Transaction[results.length - 1];
        // u1->A->B, u2->B->C, u3->C->D
        for (int i = 0; i < results.length - 1; i++) {
            byte[] callData = new ABIStreamingEncoder()
                    .encodeOneString("doCallOther")
                    .encodeOneAddress(new Address(contractAddresses[i + 1].toByteArray()))
                    .encodeOneByteArray(new ABIStreamingEncoder().encodeOneString("addValue").toBytes())
                    .toBytes();

            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], mainchainKernel.getNonce(user[i]), BigInteger.ZERO, callData, 5_000_000, 1);
        }

        results = avm.run(mainchainKernel, tx, ExecutionType.ASSUME_MAINCHAIN, mainchainKernel.getBlockNumber() - 1);
        for (FutureResult f : results) {
            f.getResult();
        }

        sidechainKernel.generateBlock();

        // validate the state of contracts
        for (int i = 0; i < length - 1; i++) {
            byte[] getCallCount = new ABIStreamingEncoder().encodeOneString("getCallCount").toBytes();
            tx[i] = AvmTransactionUtil.call(user[i], contractAddresses[i], sidechainKernel.getNonce(user[i]), BigInteger.ZERO, getCallCount, 5_000_000, 1);
        }

        results = avm.run(sidechainKernel, tx, ExecutionType.ASSUME_SIDECHAIN, commonParent);
        for (FutureResult f : results) {
            Assert.assertEquals(0, new ABIDecoder(f.getResult().copyOfTransactionOutput().orElseThrow()).decodeOneInteger());
        }
    }


    private static AionAddress deploy(AionAddress deployer, TestingState kernel, byte[] txData) {
        Transaction tx1 = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), BigInteger.ZERO, txData, 5_000_000, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{tx1}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
        return new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }

    private static TransactionResult callDapp(TestingState kernel, AionAddress sender, AionAddress dappAddress,
                                              String methodName, ExecutionType executionType, long commonMainchainBlockNumber, Object... args) {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder().encodeOneString(methodName);
        for (Object arg : args) {
            encoder.encodeOneByteArray((byte[]) arg);
        }
        byte[] data = encoder.toBytes();
        Transaction tx = AvmTransactionUtil.call(sender, dappAddress, kernel.getNonce(sender), BigInteger.ZERO, data, 2_000_00, 1);
        return avm.run(kernel, new Transaction[]{tx}, executionType, commonMainchainBlockNumber)[0].getResult();
    }
}

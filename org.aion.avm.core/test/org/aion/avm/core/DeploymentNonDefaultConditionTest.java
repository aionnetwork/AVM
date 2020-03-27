package org.aion.avm.core;

import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
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

public class DeploymentNonDefaultConditionTest {

    private TestingState kernel;
    private AvmImpl avm;
    private AionAddress deployer = TestingState.PREMINED_ADDRESS;


    @Before
    public void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingState(block);
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @Test
    public void testHasStorage() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(NonDefaultConditionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TransactionResult result = deploy(deployer, kernel, txData, BigInteger.ZERO);
        assertTrue(result.transactionStatus.isSuccess());
        AionAddress dappAddress = new AionAddress(result.copyOfTransactionOutput().orElseThrow());

        kernel.generateBlock();

        Assert.assertFalse(kernel.hasStorage(dappAddress));

        // put data in storage
        byte[] key = Helpers.randomBytes(32);
        byte[] value = Helpers.randomBytes(50);
        byte[] data = new ABIStreamingEncoder().encodeOneString("putStorage").encodeOneByteArray(key).encodeOneByteArray(value).toBytes();
        result = callDapp(kernel, deployer, dappAddress, data);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertTrue(kernel.hasStorage(dappAddress));

        // update data in storage
        byte[] value2 = Helpers.randomBytes(60);
        data = new ABIStreamingEncoder().encodeOneString("putStorage").encodeOneByteArray(key).encodeOneByteArray(value2).toBytes();
        result = callDapp(kernel, deployer, dappAddress, data);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertTrue(kernel.hasStorage(dappAddress));

        kernel.generateBlock();

        // remove storage
        Assert.assertTrue(kernel.hasStorage(dappAddress));
        data = new ABIStreamingEncoder().encodeOneString("putStorage").encodeOneByteArray(key).encodeOneByteArray(null).toBytes();
        result = callDapp(kernel, deployer, dappAddress, data);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertFalse(kernel.hasStorage(dappAddress));

        kernel.generateBlock();
        // self destruct a contract
        data = new ABIStreamingEncoder().encodeOneString("putStorage").encodeOneByteArray(key).encodeOneByteArray(value).toBytes();
        result = callDapp(kernel, deployer, dappAddress, data);
        Assert.assertTrue(result.transactionStatus.isSuccess());

        data = new ABIStreamingEncoder().encodeOneString("selfDestruct").toBytes();
        result = callDapp(kernel, deployer, dappAddress, data);
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertFalse(kernel.hasStorage(dappAddress));
    }

    @After
    public void tearDown() {
        avm.shutdown();
    }

    private TransactionResult deploy(AionAddress deployer, TestingState kernel, byte[] txData, BigInteger value) {
        Transaction tx1 = AvmTransactionUtil.create(deployer, kernel.getNonce(deployer), value, txData, 5_000_000, 1);
        return avm.run(kernel, new Transaction[]{tx1}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
    }

    private TransactionResult callDapp(TestingState kernel, AionAddress sender, AionAddress dappAddress, byte[] encodedData) {
        Transaction tx = AvmTransactionUtil.call(sender, dappAddress, kernel.getNonce(sender), BigInteger.ZERO, encodedData, (long) 2_000_000, 1);
        return avm.run(kernel, new Transaction[]{tx}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
    }
}

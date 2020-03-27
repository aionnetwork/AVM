package org.aion.avm.core;

import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.avm.utilities.JarBuilder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeploymentNonDefaultConditionTest {

    private TestingState kernel;
    private AvmImpl avm;
    private AionAddress deployer = TestingState.PREMINED_ADDRESS;
    private IExternalCapabilities capabilities;

    @Before
    public void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        this.kernel = new TestingState(block);
        capabilities = new EmptyCapabilities();
        this.avm = CommonAvmFactory.buildAvmInstanceForConfiguration(capabilities, new AvmConfiguration());
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

    @Test
    public void testNonceNotZeroFail() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(NonDefaultConditionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        AionAddress dappAddress = capabilities.generateContractAddress(deployer, kernel.getNonce(deployer));
        kernel.incrementNonce(dappAddress);
        TransactionResult result = deploy(deployer, kernel, txData, BigInteger.ZERO);
        assertTrue(result.transactionStatus.isFailed());
        assertEquals(5_000_000, result.energyUsed);
    }

    @Test
    public void testCodeNotEmpty() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(NonDefaultConditionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        AionAddress dappAddress = capabilities.generateContractAddress(deployer, kernel.getNonce(deployer));
        kernel.putCode(dappAddress, new byte[1]);
        TransactionResult result = deploy(deployer, kernel, txData, BigInteger.ZERO);
        assertTrue(result.transactionStatus.isFailed());
        assertEquals(5_000_000, result.energyUsed);
    }

    @Test
    public void testStorageNotEmpty() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(NonDefaultConditionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        AionAddress dappAddress = capabilities.generateContractAddress(deployer, kernel.getNonce(deployer));
        byte[] key = Helpers.randomBytes(32);
        byte[] value = Helpers.randomBytes(50);
        kernel.putStorage(dappAddress, key, value);
        TransactionResult result = deploy(deployer, kernel, txData, BigInteger.ZERO);
        assertTrue(result.transactionStatus.isFailed());
        assertEquals(5_000_000, result.energyUsed);
    }

    @Test
    public void testInitialBalanceSuccess() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(NonDefaultConditionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        AionAddress dappAddress = capabilities.generateContractAddress(deployer, kernel.getNonce(deployer));
        kernel.adjustBalance(dappAddress, BigInteger.TEN);
        TransactionResult result = deploy(deployer, kernel, txData, BigInteger.ZERO);
        assertTrue(result.transactionStatus.isSuccess());
    }

    @Test
    public void testInternalTransactions() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(NonDefaultConditionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();
        TransactionResult result = deploy(deployer, kernel, txData, BigInteger.ZERO);
        assertTrue(result.transactionStatus.isSuccess());
        AionAddress dappAddress = new AionAddress(result.copyOfTransactionOutput().orElseThrow());

        AionAddress newAddress = capabilities.generateContractAddress(dappAddress, kernel.getNonce(dappAddress));
        kernel.putCode(newAddress, new byte[1]);

        // deploy to an address with code using internal transactions
        byte[] internalCreateJar = JarBuilder.buildJarForMainClassAndExplicitClassNamesAndBytecode(ConstantBillingTarget.class, Collections.emptyMap());
        txData = new CodeAndArguments(internalCreateJar, new byte[0]).encodeToBytes();

        byte[] data = new ABIStreamingEncoder().encodeOneString("call").encodeOneByteArray(txData).encodeOneBoolean(true).toBytes();
        result = callDapp(kernel, deployer, dappAddress, data);
        Assert.assertFalse(result.transactionStatus.isSuccess());
    }

    @Test
    public void testTransferFailed() {
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(NonDefaultConditionTarget.class);
        byte[] txData = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        AionAddress dappAddress = capabilities.generateContractAddress(deployer, kernel.getNonce(deployer));
        kernel.putCode(dappAddress, new byte[1]);
        TransactionResult result = deploy(deployer, kernel, txData, BigInteger.TEN);
        assertTrue(result.transactionStatus.isFailed());
        assertEquals(0, kernel.getBalance(dappAddress).signum());
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

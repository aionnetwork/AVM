package org.aion.avm.core;

import avm.Address;

import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIException;
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

import java.math.BigInteger;


/**
 * Tests interactions with and assumptions around internal contract call Result objects.
 */
public class ResultTest {
    private static AionAddress DEPLOYER = TestingState.PREMINED_ADDRESS;
    private static TestingBlock BLOCK;
    private static TestingState KERNEL;
    private static AvmImpl AVM;
    private static byte[] JAR;
    private static AionAddress DAPP_ADDRESS;

    @BeforeClass
    public static void setupClass() {
        BLOCK = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        KERNEL = new TestingState(BLOCK);
        AvmConfiguration config = new AvmConfiguration();
        AVM = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), config);
        
        // We manually construct the JAR so it can fit in a deployment argument.
        JAR = JarBuilder.buildJarForMainAndClasses(ResultTestTarget.class
                , ABIDecoder.class
                , ABIException.class
                );
        byte[] deployment = new CodeAndArguments(JAR, null).encodeToBytes();
        Transaction create = AvmTransactionUtil.create(DEPLOYER, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, deployment, 5_000_000L, 1);
        TransactionResult result = AVM.run(KERNEL, new Transaction[] {create}, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        DAPP_ADDRESS = new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }

    @AfterClass
    public static void tearDownClass() {
        AVM.shutdown();
    }

    @Test
    public void testReturnData() {
        KERNEL.generateBlock();
        byte[] arg = encode("returnData");
        byte[] data = encodeForCall(DAPP_ADDRESS, arg, true);

        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, DAPP_ADDRESS, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, data, 2_000_000L, 1);
        TransactionResult result = AVM.run(KERNEL, new Transaction[]{ transaction }, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertArrayEquals(new byte[] {1, 2, 3}, result.copyOfTransactionOutput().get());
    }

    @Test
    public void testReturnNull() {
        KERNEL.generateBlock();
        byte[] arg = encode("returnNull");
        byte[] data = encodeForCall(DAPP_ADDRESS, arg, true);

        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, DAPP_ADDRESS, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, data, 2_000_000L, 1);
        TransactionResult result = AVM.run(KERNEL, new Transaction[]{ transaction }, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // A return null should come back to this point since the internal returns null and the external just returns what it received.
        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    @Test
    public void testRevert() {
        KERNEL.generateBlock();
        byte[] arg = encode("revert");
        byte[] data = encodeForCall(DAPP_ADDRESS, arg, false);

        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, DAPP_ADDRESS, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, data, 2_000_000L, 1);
        TransactionResult result = AVM.run(KERNEL, new Transaction[]{ transaction }, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        // The internal transaction reverts but the external one handles this and returns null.
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    @Test
    public void testTransfer() {
        KERNEL.generateBlock();
        AionAddress target = Helpers.randomAddress();
        byte[] arg = new byte[0];
        byte[] data = encodeForTransfer(target, arg);

        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, DAPP_ADDRESS, KERNEL.getNonce(DEPLOYER), BigInteger.ONE, data, 2_000_000L, 1);
        TransactionResult result = AVM.run(KERNEL, new Transaction[]{ transaction }, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        // Make sure that this actually worked.
        Assert.assertEquals(BigInteger.ONE, KERNEL.getBalance(target));
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // A transfer always just returns null.
        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    @Test
    public void testDeploy() {
        KERNEL.generateBlock();
        byte[] data = encodeForCreate(new CodeAndArguments(JAR, null));

        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, DAPP_ADDRESS, KERNEL.getNonce(DEPLOYER), BigInteger.ONE, data, 5_000_000L, 1);
        TransactionResult result = AVM.run(KERNEL, new Transaction[]{ transaction }, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        // Make sure that this actually worked.
        Assert.assertTrue(result.transactionStatus.isSuccess());
        // A deploy always just returns the contract address.
        Assert.assertEquals(BigInteger.ONE, KERNEL.getBalance(new AionAddress(result.copyOfTransactionOutput().orElseThrow())));
    }

    @Test
    public void testUncaught() {
        KERNEL.generateBlock();
        byte[] arg = encode("uncaught");
        byte[] data = encodeForCall(DAPP_ADDRESS, arg, false);

        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, DAPP_ADDRESS, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, data, 2_000_000L, 1);
        TransactionResult result = AVM.run(KERNEL, new Transaction[]{ transaction }, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        // The internal transaction fails but the external one handles this and returns null.
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }

    @Test
    public void testOutOfEnergy() {
        KERNEL.generateBlock();
        byte[] arg = encode("outOfEnergy");
        byte[] data = encodeForCall(DAPP_ADDRESS, arg, false);

        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, DAPP_ADDRESS, KERNEL.getNonce(DEPLOYER), BigInteger.ZERO, data, 2_000_000L, 1);
        TransactionResult result = AVM.run(KERNEL, new Transaction[]{ transaction }, ExecutionType.ASSUME_MAINCHAIN, KERNEL.getBlockNumber()-1)[0].getResult();
        // The internal transaction fails but the external one handles this and returns null.
        Assert.assertTrue(result.transactionStatus.isSuccess());
        Assert.assertFalse(result.copyOfTransactionOutput().isPresent());
    }


    private static byte[] encode(String methodName) {
        return new ABIStreamingEncoder()
                .encodeOneString(methodName)
                .toBytes();
    }

    private static byte[] encodeForCall(AionAddress target, byte[] data, boolean expectToPass) {
        return new ABIStreamingEncoder()
                .encodeOneString("testCall")
                .encodeOneAddress(new Address(target.toByteArray()))
                .encodeOneByteArray(data)
                .encodeOneBoolean(expectToPass)
                .toBytes();
    }

    private static byte[] encodeForTransfer(AionAddress target, byte[] data) {
        return new ABIStreamingEncoder()
                .encodeOneString("testTransfer")
                .encodeOneAddress(new Address(target.toByteArray()))
                .encodeOneByteArray(data)
                .toBytes();
    }

    private static byte[] encodeForCreate(CodeAndArguments toDeploy) {
        return new ABIStreamingEncoder()
                .encodeOneString("testDeploy")
                .encodeOneByteArray(toDeploy.encodeToBytes())
                .toBytes();
    }
}

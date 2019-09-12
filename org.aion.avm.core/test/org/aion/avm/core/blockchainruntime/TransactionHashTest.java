package org.aion.avm.core.blockchainruntime;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.aion.avm.core.AvmConfiguration;
import org.aion.avm.core.AvmImpl;
import org.aion.avm.core.AvmTransactionUtil;
import org.aion.avm.core.CommonAvmFactory;
import org.aion.avm.core.ExecutionType;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;
import org.aion.avm.userlib.abi.ABIException;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.types.TransactionResult;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TransactionHashTest {
    private static AionAddress from = TestingState.PREMINED_ADDRESS;
    private static TestingState kernel;
    private static AvmImpl avm;
    private static long energyLimit = 10_000_000L;
    private static long energyPrice = 5;

    private byte[] deployTransactionHash;
    private AionAddress contract;

    @BeforeClass
    public static void setupClass() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Before
    public void setup() {
        Transaction transaction = constructCreateTransaction();
        this.deployTransactionHash = transaction.copyOfTransactionHash();
        this.contract = deployContract(transaction);
    }

    /**
     * Verifies an external transaction returns its hash.
     */
    @Test
    public void testTransactionHashInExternalTransaction() {
        Transaction transaction = constructCallTransaction(this.contract, "getTransactionHash");
        byte[] hash = transaction.copyOfTransactionHash();

        byte[] returnedHash = callContractAndReturnOutput(transaction);
        Assert.assertArrayEquals(hash, returnedHash);
    }

    /**
     * Verifies an external transaction captured the transaction hash of the deploying transaction in its clinit.
     */
    @Test
    public void testTransactionHashInExternalTransactionClinit() {
        Transaction transaction = constructCallTransaction(this.contract, "getTransactionHashFromClinit");

        byte[] returnedHash = callContractAndReturnOutput(transaction);
        Assert.assertArrayEquals(this.deployTransactionHash, returnedHash);
    }

    /**
     * Verifies an internal transaction returns the origin hash.
     */
    @Test
    public void testTransactionHashInInternalTransaction() {
        Transaction transaction = constructCallTransaction(this.contract, "getTransactionHashInInternalCall");
        byte[] hash = transaction.copyOfTransactionHash();

        byte[] returnedHash = callContractAndReturnOutput(transaction);
        Assert.assertArrayEquals(hash, returnedHash);
    }

    /**
     * Verifies an internal transaction captured the transaction hash of the deploying transaction in its clinit.
     */
    @Test
    public void testTransactionHashInInternalTransactionClinit() {
        Transaction transaction = constructCallTransaction(this.contract, "getTransactionHashFromInternalCallClinit", produceJar());
        byte[] hash = transaction.copyOfTransactionHash();

        byte[] returnedHash = callContractAndReturnOutput(transaction);
        Assert.assertArrayEquals(hash, returnedHash);
    }

    /**
     * The contract will revert if it discovers the hash was modified. We simply assert it succeeded.
     */
    @Test
    public void testModifyingHashInExternalTransaction() {
        Transaction transaction = constructCallTransaction(this.contract, "modifyHashInExternalCall");
        callContractAndVerifySuccess(transaction);
    }

    /**
     * The contract will revert if it discovers the hash was modified. We simply assert it succeeded.
     */
    @Test
    public void testModifyingHashInInternalTransaction() {
        Transaction transaction = constructCallTransaction(this.contract, "modifyHashInInternalCall");
        callContractAndVerifySuccess(transaction);
    }

    private static Transaction constructCallTransaction(AionAddress contract, String method) {
        byte[] callData = new ABIStreamingEncoder().encodeOneString(method).toBytes();
        return AvmTransactionUtil.callWithHash(from, contract, Helpers.randomBytes(32), kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
    }

    private static Transaction constructCallTransaction(AionAddress contract, String method, byte[] jar) {
        byte[] callData = new ABIStreamingEncoder().encodeOneString(method).encodeOneByteArray(jar).toBytes();
        return AvmTransactionUtil.callWithHash(from, contract, Helpers.randomBytes(32), kernel.getNonce(from), BigInteger.ZERO, callData, energyLimit, energyPrice);
    }

    private static byte[] callContractAndReturnOutput(Transaction transaction) {
        kernel.generateBlock();
        TransactionResult result = avm.run(kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
        return result.copyOfTransactionOutput().orElseThrow();
    }

    private static void callContractAndVerifySuccess(Transaction transaction) {
        kernel.generateBlock();
        TransactionResult result = avm.run(kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    private static byte[] produceJar() {
        byte[] jar = JarBuilder.buildJarForMainAndClasses(TransactionHashTarget.class, ABIDecoder.class, ABIEncoder.class, ABIException.class);
        return new CodeAndArguments(jar, new byte[0]).encodeToBytes();
    }

    private static Transaction constructCreateTransaction() {
        return AvmTransactionUtil.createWithHash(from, Helpers.randomBytes(32), kernel.getNonce(from), BigInteger.ZERO, produceJar(), energyLimit, energyPrice);
    }

    private static AionAddress deployContract(Transaction transaction) {
        kernel.generateBlock();
        TransactionResult result = avm.run(kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
        return new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }
}

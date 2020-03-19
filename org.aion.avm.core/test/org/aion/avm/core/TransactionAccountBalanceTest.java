package org.aion.avm.core;

import static org.aion.avm.core.BillingRules.getBasicTransactionCost;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;
import org.aion.kernel.AvmWrappedTransactionResult.AvmInternalError;
import org.aion.kernel.TestingState;
import org.aion.types.AionAddress;
import org.aion.types.Transaction;
import org.aion.avm.core.blockchainruntime.EmptyCapabilities;
import org.aion.avm.core.dappreading.UserlibJarBuilder;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.userlib.CodeAndArguments;
import org.aion.avm.userlib.abi.ABIStreamingEncoder;
import org.aion.kernel.TestingBlock;
import org.aion.types.TransactionResult;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests that key players related to a transaction have their account balances updated correctly
 * after a transaction has been sent.
 */
public class TransactionAccountBalanceTest {
    private static AionAddress from;
    private static final long energyLimit = 10_000_000L;

    private static final long energyLimitForValueTransfer = 21_000L;
    private static final long energyPrice = 5;
    private static TestingBlock block;

    private static TestingState kernel;
    private static AvmImpl avm;

    @Before
    public void resetTestingState() {
        block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        from = TestingState.PREMINED_ADDRESS;
    }

    @BeforeClass
    public static void setup() {
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
    }

    @AfterClass
    public static void tearDown() {
        avm.shutdown();
    }

    @Test
    public void testSenderBalanceAfterCreateNoValueSent() {
        BigInteger senderBalance = kernel.getBalance(from);

        TransactionResult result = deployContract(BigInteger.ZERO);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        BigInteger transactionCost = BigInteger.valueOf(energyUsed * energyPrice);
        assertEquals(senderBalance.subtract(transactionCost), kernel.getBalance(from));
    }

    @Test
    public void testSenderBalanceAfterCreateValueSent() {
        BigInteger value = new BigInteger("328756");
        BigInteger senderBalance = kernel.getBalance(from);

        TransactionResult result = deployContract(value);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        BigInteger transactionCost = BigInteger.valueOf(energyUsed * energyPrice);
        assertEquals(senderBalance.subtract(transactionCost).subtract(value), kernel.getBalance(from));
    }

    @Test
    public void testSenderBalanceAfterCallNoValueSent() {
        AionAddress contract = deployContractAndGetAddress();

        BigInteger senderBalance = kernel.getBalance(from);

        TransactionResult result = callContract(contract, BigInteger.ZERO);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        BigInteger transactionCost = BigInteger.valueOf(energyUsed * energyPrice);
        assertEquals(senderBalance.subtract(transactionCost), kernel.getBalance(from));
    }

    @Test
    public void testSenderBalanceAfterCallValueSent() {
        AionAddress contract = deployContractAndGetAddress();

        BigInteger senderBalance = kernel.getBalance(from);
        BigInteger value = new BigInteger("235762");

        TransactionResult result = callContract(contract, value);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        BigInteger transactionCost = BigInteger.valueOf(energyUsed * energyPrice);
        assertEquals(senderBalance.subtract(transactionCost).subtract(value), kernel.getBalance(from));
    }

    @Test
    public void testSenderBalanceAfterValueTransfer() {
        BigInteger senderBalance = kernel.getBalance(from);
        BigInteger value = new BigInteger("2398652");

        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);
        TransactionResult result = transferValue(recipient, value);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);
        assertEquals(energyLimitForValueTransfer, energyUsed);

        BigInteger transactionCost = BigInteger.valueOf(energyUsed * energyPrice);
        assertEquals(senderBalance.subtract(transactionCost).subtract(value), kernel.getBalance(from));
    }

    @Test
    public void testInsufficientBalance() {
        BigInteger senderBalanceBefore = kernel.getBalance(from);
        BigInteger balanceTransferCost = BigInteger.valueOf(BillingRules.BASIC_TRANSACTION_COST * energyPrice);
        BigInteger value = senderBalanceBefore.subtract(balanceTransferCost).add(BigInteger.ONE);
        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);

        TransactionResult result = transferValueWithData(recipient, value, new byte[0]);

        assertEquals(AvmInternalError.REJECTED_INSUFFICIENT_BALANCE.error, result.transactionStatus.causeOfError);

        long energyUsed = result.energyUsed;
        assertEquals(0, energyUsed);

        assertEquals(senderBalanceBefore, kernel.getBalance(from));
        assertEquals(BigInteger.ZERO, kernel.getBalance(recipient));
    }

    @Test
    public void testTransferEntireBalance() {
        BigInteger senderBalanceBefore = kernel.getBalance(from);
        BigInteger balanceTransferCost = BigInteger.valueOf(BillingRules.BASIC_TRANSACTION_COST * energyPrice);
        BigInteger value = senderBalanceBefore.subtract(balanceTransferCost);
        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);

        TransactionResult result = transferValueWithData(recipient, value, new byte[0]);

        assertTrue(result.transactionStatus.isSuccess());

        long energyUsed = result.energyUsed;
        assertEquals(energyLimitForValueTransfer, energyUsed);

        assertEquals(BigInteger.ZERO, kernel.getBalance(from));
        assertEquals(value, kernel.getBalance(recipient));
    }

    @Test
    public void testTransferEntireBalanceWithData() {
        BigInteger senderBalanceBefore = kernel.getBalance(from);
        byte[] data = new byte[1];
        long energyRequired = getBasicTransactionCost(data);
        BigInteger balanceTransferCost = BigInteger.valueOf(energyRequired * energyPrice);
        BigInteger value = senderBalanceBefore.subtract(balanceTransferCost);
        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);

        TransactionResult result = transferValueWithDataAndEnergyLimit(recipient, value, data, energyRequired);

        assertTrue(result.transactionStatus.isSuccess());

        long energyUsed = result.energyUsed;
        assertEquals(energyRequired, energyUsed);

        assertEquals(BigInteger.ZERO, kernel.getBalance(from));
        assertEquals(value, kernel.getBalance(recipient));
    }



    @Test
    public void testTransferEntireBalanceWithRandomData() {
        BigInteger senderBalanceBefore = kernel.getBalance(from);
        Random r = new Random();
        byte[] data = new byte[1000];
        r.nextBytes(data);
        long energyRequired = getBasicTransactionCost(data);
        BigInteger balanceTransferCost = BigInteger.valueOf(energyRequired * energyPrice);
        BigInteger value = senderBalanceBefore.subtract(balanceTransferCost);
        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);

        TransactionResult result = transferValueWithDataAndEnergyLimit(recipient, value, data, energyRequired);

        assertTrue(result.transactionStatus.isSuccess());

        long energyUsed = result.energyUsed;
        assertEquals(energyRequired, energyUsed);

        assertEquals(BigInteger.ZERO, kernel.getBalance(from));
        assertEquals(value, kernel.getBalance(recipient));
    }

    @Test
    public void testInvalidEnergyAndInsufficientFunds() {
        BigInteger senderBalanceBefore = kernel.getBalance(from);
        BigInteger balanceTransferCost = BigInteger.valueOf(BillingRules.BASIC_TRANSACTION_COST * energyPrice);
        BigInteger value = senderBalanceBefore.subtract(balanceTransferCost);
        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);

        TransactionResult result = transferValueWithData(recipient, value, new byte[1]);

        assertEquals(AvmInternalError.REJECTED_INVALID_ENERGY_LIMIT.error, result.transactionStatus.causeOfError);
    }

    @Test
    public void testBalanceTransferWithDataLowEnergyLimit() {
        BigInteger senderBalanceBefore = kernel.getBalance(from);
        BigInteger value = BigInteger.ONE;
        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);

        TransactionResult result = transferValueWithData(recipient, value, new byte[1]);

        assertEquals(AvmInternalError.REJECTED_INVALID_ENERGY_LIMIT.error, result.transactionStatus.causeOfError);

        long energyUsed = result.energyUsed;
        assertEquals(0, energyUsed);

        assertEquals(senderBalanceBefore, kernel.getBalance(from));
        assertEquals(BigInteger.ZERO, kernel.getBalance(recipient));
    }

    @Test
    public void testBalanceTransferWithLotsOfDataLowEnergyLimit() {
        BigInteger senderBalanceBefore = kernel.getBalance(from);
        BigInteger value = BigInteger.ONE;
        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);

        TransactionResult result = transferValueWithData(recipient, value, new byte[1000000]);

        assertEquals(AvmInternalError.REJECTED_INVALID_ENERGY_LIMIT.error, result.transactionStatus.causeOfError);

        long energyUsed = result.energyUsed;
        assertEquals(0, energyUsed);

        assertEquals(senderBalanceBefore, kernel.getBalance(from));
        assertEquals(BigInteger.ZERO, kernel.getBalance(recipient));
    }

    @Test
    public void testValueZeroWithDataLowEnergyLimit() {
        BigInteger senderBalanceBefore = kernel.getBalance(from);
        BigInteger value = BigInteger.ZERO;
        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);

        TransactionResult result = transferValueWithData(recipient, value, new byte[1]);

        assertEquals(AvmInternalError.REJECTED_INVALID_ENERGY_LIMIT.error, result.transactionStatus.causeOfError);

        long energyUsed = result.energyUsed;
        assertEquals(0, energyUsed);

        assertEquals(senderBalanceBefore, kernel.getBalance(from));
        assertEquals(BigInteger.ZERO, kernel.getBalance(recipient));
    }

    @Test
    public void testMinerBalanceAfterCreate() {
        BigInteger minerBalance = kernel.getBalance(block.getCoinbase());

        TransactionResult result = deployContract(BigInteger.TEN);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        BigInteger transactionCost = BigInteger.valueOf(energyUsed * energyPrice);
        assertEquals(minerBalance.add(transactionCost), kernel.getBalance(block.getCoinbase()));
    }

    @Test
    public void testMinerBalanceAfterCall() {
        AionAddress contract = deployContractAndGetAddress();
        BigInteger minerBalance = kernel.getBalance(block.getCoinbase());

        TransactionResult result = callContract(contract, BigInteger.TEN);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        BigInteger transactionCost = BigInteger.valueOf(energyUsed * energyPrice);
        assertEquals(minerBalance.add(transactionCost), kernel.getBalance(block.getCoinbase()));
    }

    @Test
    public void testMinerBalanceAfterValueTransfer() {
        BigInteger minerBalance = kernel.getBalance(block.getCoinbase());
        BigInteger value = new BigInteger("2345136");

        AionAddress recipient = createNewAccountWithBalance(BigInteger.ZERO);
        TransactionResult result = transferValue(recipient, value);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);
        assertEquals(energyLimitForValueTransfer, energyUsed);

        BigInteger transactionCost = BigInteger.valueOf(energyUsed * energyPrice);
        assertEquals(minerBalance.add(transactionCost), kernel.getBalance(block.getCoinbase()));
    }

    @Test
    public void testDestinationBalanceAfterCreateNoValueSent() {
        TransactionResult result = deployContract(BigInteger.ZERO);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        AionAddress destination = new AionAddress(result.copyOfTransactionOutput().orElseThrow());
        assertEquals(BigInteger.ZERO, kernel.getBalance(destination));
    }

    @Test
    public void testDestinationBalanceAfterCreateValueSent() {
        BigInteger value = new BigInteger("23874773");

        TransactionResult result = deployContract(value);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        AionAddress destination = new AionAddress(result.copyOfTransactionOutput().orElseThrow());
        assertEquals(value, kernel.getBalance(destination));
    }

    @Test
    public void testDestinationBalanceAfterCallNoValueSent() {
        AionAddress contract = deployContractAndGetAddress();

        BigInteger contractBalance = kernel.getBalance(contract);

        TransactionResult result = callContract(contract, BigInteger.ZERO);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        assertEquals(contractBalance, kernel.getBalance(contract));
    }

    @Test
    public void testDestinationBalanceAfterCallValueSent() {
        AionAddress contract = deployContractAndGetAddress();

        BigInteger contractBalance = kernel.getBalance(contract);
        BigInteger value = new BigInteger("23872325");

        TransactionResult result = callContract(contract, value);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        assertEquals(contractBalance.add(value), kernel.getBalance(contract));
    }

    @Test
    public void testDestinationBalanceAfterValueTransfer() {
        BigInteger initialBalance = new BigInteger("897346532");
        BigInteger value = new BigInteger("2398652");

        AionAddress recipient = createNewAccountWithBalance(initialBalance);
        TransactionResult result = transferValue(recipient, value);
        long energyUsed = result.energyUsed;
        assertTrue(energyUsed > 0);

        assertEquals(initialBalance.add(value), kernel.getBalance(recipient));
    }

    private TransactionResult deployContract(BigInteger value) {
        kernel.generateBlock();
        byte[] jar = UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(BasicAppTestTarget.class);
        jar = new CodeAndArguments(jar, new byte[0]).encodeToBytes();

        Transaction transaction = AvmTransactionUtil.create(from, kernel.getNonce(from), value, jar, energyLimit, energyPrice);
        return avm.run(TransactionAccountBalanceTest.kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
    }

    private AionAddress deployContractAndGetAddress() {
        TransactionResult result = deployContract(BigInteger.ZERO);
        assertTrue(result.transactionStatus.isSuccess());
        return new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }

    private TransactionResult callContract(AionAddress contract, BigInteger value) {
        kernel.generateBlock();
        byte[] callData = new ABIStreamingEncoder().encodeOneString("allocateObjectArray").toBytes();
        Transaction transaction = AvmTransactionUtil.call(from, contract, kernel.getNonce(from), value, callData, energyLimit, energyPrice);
        return avm.run(TransactionAccountBalanceTest.kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
    }

    private TransactionResult transferValue(AionAddress recipient, BigInteger value) {
        return transferValueWithData(recipient, value, new byte[0]);
    }

    private TransactionResult transferValueWithData(AionAddress recipient, BigInteger value, byte[] data) {
        kernel.generateBlock();
        Transaction transaction = AvmTransactionUtil.call(from, recipient, kernel.getNonce(from), value, data, BillingRules.BASIC_TRANSACTION_COST, energyPrice);
        return avm.run(TransactionAccountBalanceTest.kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
    }

    private TransactionResult transferValueWithDataAndEnergyLimit(AionAddress recipient, BigInteger value, byte[] data, long energyLimit) {
        kernel.generateBlock();
        Transaction transaction = AvmTransactionUtil.call(from, recipient, kernel.getNonce(from), value, data, energyLimit , energyPrice);
        return avm.run(TransactionAccountBalanceTest.kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber()-1)[0].getResult();
    }

    private AionAddress createNewAccountWithBalance(BigInteger balance) {
        AionAddress account = Helpers.randomAddress();
        kernel.createAccount(account);
        kernel.adjustBalance(account, balance);
        return account;
    }

}

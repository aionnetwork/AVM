package org.aion.avm.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * The tests in this class are here to verify the AKI-254 patch to BigDecimal.
 *
 * There are two BigDecimal constructors in question: BigDecimal(String) and BigDecimal(String, MathContext).
 * These hits run the same cases over each of the two constructors.
 */
public class BigDecimalStringConstructorTest {
    private static final long ENERGY_LIMIT = 2_000_000L;
    private static final AionAddress DEPLOYER = TestingState.PREMINED_ADDRESS;
    private static TestingState kernel;
    private static AvmImpl avm;
    private static AionAddress contract;

    @BeforeClass
    public static void setup() {
        TestingBlock block = new TestingBlock(new byte[32], 1, Helpers.randomAddress(), System.currentTimeMillis(), new byte[0]);
        kernel = new TestingState(block);
        avm = CommonAvmFactory.buildAvmInstanceForConfiguration(new EmptyCapabilities(), new AvmConfiguration());
        contract = deployContract();
    }

    @AfterClass
    public static void teardown() {
        avm.shutdown();
    }

    //<------------------------- tests the BigDecimal(String) constructor ------------------------->

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 digits.
     */
    @Test
    public void testBaseTenBigDecimal() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBaseTenBigDecimal").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 digits with a leading '+' sign.
     */
    @Test
    public void testPositivelySignedBaseTenBigDecimal() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedBaseTenBigDecimal").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 digits with a leading '-' sign.
     */
    @Test
    public void testNegativelySignedBaseTenBigDecimal() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedBaseTenBigDecimal").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using less than 78 base 10 digits
     * with sign characters ('+' and '-') in positions that are not the zero'th index.
     */
    @Test
    public void testBigDecimalWithInvalidSignCharacters() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalWithInvalidSignCharacters").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using less than 78 base 10 digits
     * with a decimal character '.' indicating a fractional number.
     */
    @Test
    public void testBigDecimalFromFractionString() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalFromFractionString").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using less than 78 base 10 digits and an exponent.
     */
    @Test
    public void testBigDecimalFromExponentString() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalFromExponentString").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using less than 78 non-base 10 characters.
     */
    @Test
    public void testBigDecimalFromRandomCharacters() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalFromGarbageCharacters").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'small' BigDecimal in the sense that its first digit is '1'.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testSmallBigDecimalLength77() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createSmallBigDecimalLength77").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'small' BigDecimal in the sense that its first digit is '1'.
     * This BigDecimal is creating using a String that begins with the '+' character.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testPositivelySignedBigDecimalWith77Chars() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedBigDecimalWith77Chars").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'small' BigDecimal in the sense that its first digit is '1'.
     * This BigDecimal is creating using a String that begins with the '-' character.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testNegativelySignedBigDecimalWith77Chars() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedBigDecimalWith77Chars").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * We do this to demonstrate that if we then call toBigInteger() an exception is thrown because the BigInteger is too large.
     */
    @Test
    public void testLargeBigDecimalLength77() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createLargeBigDecimalLength77").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * This BigDecimal is creating using a String that begins with the '+' character.
     * We do this to demonstrate that if we then call toBigInteger() an exception is thrown because the BigInteger is too large.
     */
    @Test
    public void testPositivelySignedLargeBigDecimalLength77() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedLargeBigDecimalLength77").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * This BigDecimal is creating using a String that begins with the '-' character.
     * We do this to demonstrate that if we then call toBigInteger() an exception is thrown because the BigInteger is too large.
     */
    @Test
    public void testNegativelySignedLargeBigDecimalLength77() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedLargeBigDecimalLength77").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using 76 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testLargeBigDecimalLength76() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createLargeBigDecimalLength76").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using 76 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * This BigDecimal is creating using a String that begins with the '+' character.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testPositivelySignedLargeBigDecimalLength76() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedLargeBigDecimalLength76").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using 76 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * This BigDecimal is creating using a String that begins with the '-' character.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testNegativelySignedLargeBigDecimalLength76() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedLargeBigDecimalLength76").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using 79 base 10 characters.
     */
    @Test
    public void testBigDecimalLength79() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalLength79").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using 78 base 10 characters.
     * This BigDecimal is creating using a String that begins with the '+' character.
     */
    @Test
    public void testPositivelySignedBigDecimalLength78() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedBigDecimalLength78").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using 78 base 10 characters.
     * This BigDecimal is creating using a String that begins with the '-' character.
     */
    @Test
    public void testNegativelySignedBigDecimalLength78() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedBigDecimalLength78").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    //<------------------ tests the BigDecimal(String, MathContext) constructor ------------------->

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 digits.
     */
    @Test
    public void testBaseTenBigDecimalWithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBaseTenBigDecimalWithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 digits with a leading '+' sign.
     */
    @Test
    public void testPositivelySignedBaseTenBigDecimalWithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedBaseTenBigDecimalWithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 digits with a leading '-' sign.
     */
    @Test
    public void testNegativelySignedBaseTenBigDecimalWithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedBaseTenBigDecimalWithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using less than 78 base 10 digits
     * with sign characters ('+' and '-') in positions that are not the zero'th index.
     */
    @Test
    public void testBigDecimalWithInvalidSignCharactersWithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalWithInvalidSignCharactersWithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using less than 78 base 10 digits
     * with a decimal character '.' indicating a fractional number.
     */
    @Test
    public void testBigDecimalFromFractionStringWithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalFromFractionStringWithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using less than 78 base 10 digits and an exponent.
     */
    @Test
    public void testBigDecimalFromExponentStringWithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalFromExponentStringWithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using less than 78 non-base 10 characters.
     */
    @Test
    public void testBigDecimalFromRandomCharactersWithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalFromGarbageCharactersWithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'small' BigDecimal in the sense that its first digit is '1'.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testSmallBigDecimalLength77WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createSmallBigDecimalLength77WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'small' BigDecimal in the sense that its first digit is '1'.
     * This BigDecimal is creating using a String that begins with the '+' character.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testPositivelySignedSmallBigDecimalLength77WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedSmallBigDecimalLength77WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'small' BigDecimal in the sense that its first digit is '1'.
     * This BigDecimal is creating using a String that begins with the '-' character.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testNegativelySignedSmallBigDecimalLength77WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedSmallBigDecimalLength77WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * We do this to demonstrate that if we then call toBigInteger() an exception is thrown because the BigInteger is too large.
     */
    @Test
    public void testLargeBigDecimalLength77WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createLargeBigDecimalLength77WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * This BigDecimal is creating using a String that begins with the '+' character.
     * We do this to demonstrate that if we then call toBigInteger() an exception is thrown because the BigInteger is too large.
     */
    @Test
    public void testPositivelySignedLargeBigDecimalLength77WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedLargeBigDecimalLength77WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using less than 78 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * This BigDecimal is creating using a String that begins with the '-' character.
     * We do this to demonstrate that if we then call toBigInteger() an exception is thrown because the BigInteger is too large.
     */
    @Test
    public void testNegativelySignedLargeBigDecimalLength77WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedLargeBigDecimalLength77WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using 76 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testLargeBigDecimalLength76WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createLargeBigDecimalLength76WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using 76 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * This BigDecimal is creating using a String that begins with the '+' character.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testPositivelySignedLargeBigDecimalLength76WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedLargeBigDecimalLength76WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we can create a BigDecimal using 76 base 10 characters.
     * In this case we create a 'large' BigDecimal in the sense that its first digit is '9'.
     * This BigDecimal is creating using a String that begins with the '-' character.
     * We do this to demonstrate that we can then call toBigInteger() without any issue.
     */
    @Test
    public void testNegativelySignedLargeBigDecimalLength76WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedLargeBigDecimalLength76WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using 79 base 10 characters.
     */
    @Test
    public void testBigDecimalLength79WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createBigDecimalLength79WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using 78 base 10 characters.
     * This BigDecimal is creating using a String that begins with the '+' character.
     */
    @Test
    public void testPositivelySignedBigDecimalLength78WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createPositivelySignedBigDecimalLength78WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    /**
     * AKI-254: this test demonstrates we CANNOT create a BigDecimal using 78 base 10 characters.
     * This BigDecimal is creating using a String that begins with the '-' character.
     */
    @Test
    public void testNegativelySignedBigDecimalLength78WithMathContext() {
        byte[] data = new ABIStreamingEncoder().encodeOneString("createNegativelySignedBigDecimalLength78WithMathContext").toBytes();
        Transaction transaction = AvmTransactionUtil.call(DEPLOYER, contract, kernel.getNonce(DEPLOYER), BigInteger.ZERO, data, ENERGY_LIMIT, 1);
        TransactionResult result = avm.run(kernel, new Transaction[]{transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isFailed());
        assertEquals("Failed: exception thrown", result.transactionStatus.causeOfError);
    }

    private static AionAddress deployContract() {
        byte[] jar = new CodeAndArguments(UserlibJarBuilder.buildJarForMainAndClassesAndUserlib(BigDecimalConstructorTarget.class), null).encodeToBytes();
        Transaction transaction = AvmTransactionUtil.create(DEPLOYER, kernel.getNonce(DEPLOYER), BigInteger.ZERO, jar, 5_000_000, 1);
        TransactionResult result = avm.run(kernel, new Transaction[] {transaction}, ExecutionType.ASSUME_MAINCHAIN, kernel.getBlockNumber() - 1)[0].getResult();
        assertTrue(result.transactionStatus.isSuccess());
        return new AionAddress(result.copyOfTransactionOutput().orElseThrow());
    }
}

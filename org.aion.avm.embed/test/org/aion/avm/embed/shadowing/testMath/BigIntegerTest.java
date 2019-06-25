package org.aion.avm.embed.shadowing.testMath;

import avm.Address;

import org.aion.avm.embed.AvmRule;
import org.aion.avm.tooling.ABIUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class BigIntegerTest {

    @ClassRule
    public static AvmRule avmRule = new AvmRule(false);

    private static final Address sender = avmRule.getPreminedAccount();
    private static final BigInteger value = BigInteger.ZERO;
    private static Address contract;
    private static BigInteger testValue16Bytes;
    private static BigInteger testValue32Bytes;


    @BeforeClass
    public static void setup() {
        byte[] data = avmRule.getDappBytes(BigIntegerTarget.class, null);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().transactionStatus.isSuccess());
        contract = deployResult.getDappAddress();

        byte[] arr2 = new byte[32];
        Arrays.fill(arr2, Byte.MAX_VALUE);
        testValue32Bytes = new BigInteger(arr2);

        testValue16Bytes = new BigDecimal(Math.sqrt(testValue32Bytes.doubleValue())).toBigInteger();
    }

    @Test
    public void add() {
        Assert.assertArrayEquals(testValue16Bytes.add(testValue16Bytes).toByteArray(),
                (byte[]) callStatic("add", testValue16Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void multiply() {
        Assert.assertArrayEquals(testValue16Bytes.multiply(testValue16Bytes).toByteArray(),
                (byte[]) callStatic("multiply", testValue16Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void remainder() {
        Assert.assertArrayEquals(testValue32Bytes.remainder(BigInteger.TWO).toByteArray(),
                (byte[]) callStatic("remainder", testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void sqrt() {
        Assert.assertArrayEquals(testValue32Bytes.sqrt().toByteArray(),
                (byte[]) callStatic("sqrt", testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void negate() {
        Assert.assertArrayEquals(testValue32Bytes.negate().toByteArray(),
                (byte[]) callStatic("negate", testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void mod() {
        Assert.assertArrayEquals(testValue32Bytes.mod(BigInteger.TWO).toByteArray(),
                (byte[]) callStatic("mod", testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void modPow() {
        Assert.assertArrayEquals(testValue16Bytes.modPow(testValue16Bytes, BigInteger.TWO).toByteArray(),
                (byte[]) callStatic("modPow", testValue16Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void modInverse() {
        Assert.assertArrayEquals(testValue32Bytes.modInverse(BigInteger.TWO).toByteArray(),
                (byte[]) callStatic("modInverse", testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void shiftLeft() {
        Assert.assertArrayEquals(testValue16Bytes.shiftLeft(10).toByteArray(),
                (byte[]) callStatic("shiftLeft", testValue16Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void clearBit() {
        Assert.assertArrayEquals(testValue32Bytes.clearBit(testValue32Bytes.toByteArray().length / 2).toByteArray(),
                (byte[]) callStatic("clearBit", testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void bitCount() {
        Assert.assertEquals(testValue32Bytes.bitCount(),
                (int) callStatic("bitCount", testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void newBigInteger() {
        Assert.assertArrayEquals(testValue32Bytes.negate().toByteArray(),
                (byte[]) callStatic("newBigInteger", 0, 32, testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void min() {
        Assert.assertArrayEquals(testValue32Bytes.min(testValue32Bytes).toByteArray(),
                (byte[]) callStatic("min", testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void and() {
        Assert.assertArrayEquals(testValue32Bytes.and(testValue32Bytes).toByteArray(),
                (byte[]) callStatic("and", testValue32Bytes.toByteArray()).getDecodedReturnData());
    }
    //Exception cases

    @Test
    public void addOutOfRangeArg() {
        byte[] arr = new byte[33];
        Arrays.fill(arr, Byte.MAX_VALUE);
        Assert.assertTrue(callStatic("add", new BigInteger(arr).toByteArray()).getTransactionResult().transactionStatus.isFailed());
    }

    @Test
    public void multiplyOutOfRangeResult() {
        BigInteger val = BigDecimal.valueOf(2261564242916331700000000000000000000000.7976931348623157d).toBigInteger();
        Assert.assertTrue(callStatic("multiply", val.toByteArray()).getTransactionResult().transactionStatus.isFailed());
    }

    @Test
    public void bigDecimalToBigIntegerOutOfRange() {
        Assert.assertTrue(callStatic("bigDecimalToBigIntegerException", new byte[0]).getTransactionResult().transactionStatus.isFailed());
    }

    @Test
    public void newBigIntegerOutOfRange() {
        byte[] arr = new byte[33];
        Arrays.fill(arr, Byte.MAX_VALUE);
        Assert.assertTrue(callStatic("newBigInteger", 0, 33, new BigInteger(arr).toByteArray()).getTransactionResult().transactionStatus.isFailed());
    }

    @Test
    public void multiplyWithTryCatch() {
        Assert.assertTrue((boolean) callStatic("catchExceptionOfOperation", testValue16Bytes.toByteArray(), testValue32Bytes.toByteArray()).getDecodedReturnData());
    }

    @Test
    public void setBit() {
        Assert.assertTrue(callStatic("setBit", testValue32Bytes.toByteArray(), 270).getTransactionResult().transactionStatus.isFailed());
    }

    @Test
    public void shiftLeftException() {
        AvmRule.ResultWrapper wrapper = callStatic("shiftLeftException", 270);
        Assert.assertTrue(wrapper.getTransactionResult().transactionStatus.isSuccess());
        Assert.assertTrue((boolean) wrapper.getDecodedReturnData());
    }

    @Test
    public void NPEThrownCorrectly() {
        AvmRule.ResultWrapper wrapper = callStatic("NPEThrownCorrectly");
        Assert.assertTrue(wrapper.getTransactionResult().transactionStatus.isSuccess());
    }

    private AvmRule.ResultWrapper callStatic(String methodName, Object... args) {
        byte[] data = ABIUtil.encodeMethodArguments(methodName, args);
        return avmRule.call(sender, contract, value, data, 2_000_000, 1);
    }
}

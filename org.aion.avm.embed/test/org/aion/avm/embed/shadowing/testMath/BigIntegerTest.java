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
    private static BigInteger[] bigIntegerArray;

    @BeforeClass
    public static void setup() {
        int abiBigIntegerSupportedVersion = 1;
        byte[] data = avmRule.getDappBytes(BigIntegerTarget.class, null, abiBigIntegerSupportedVersion);
        AvmRule.ResultWrapper deployResult = avmRule.deploy(sender, value, data);
        assertTrue(deployResult.getTransactionResult().transactionStatus.isSuccess());
        contract = deployResult.getDappAddress();

        byte[] arr2 = new byte[32];
        Arrays.fill(arr2, Byte.MAX_VALUE);
        testValue32Bytes = new BigInteger(arr2);

        testValue16Bytes = new BigDecimal(Math.sqrt(testValue32Bytes.doubleValue())).toBigInteger();

        bigIntegerArray = new BigInteger[10];
        for(int i =0; i< bigIntegerArray.length; i++){
            bigIntegerArray[i] = BigInteger.TEN.pow(i);
        }
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

    @Test
    public void subtract() {
        Assert.assertEquals(testValue32Bytes.subtract(testValue32Bytes), callStatic("subtract", testValue32Bytes).getDecodedReturnData());

        BigInteger testValue = new BigInteger("13468");
        Assert.assertEquals(testValue.subtract(testValue), callStatic("subtract", testValue).getDecodedReturnData());

        testValue = new BigInteger("-87813468");
        Assert.assertEquals(testValue.subtract(testValue), callStatic("subtract", testValue).getDecodedReturnData());
    }

    @Test
    public void negateBigInteger() {
        byte[] arr = new byte[12];
        Arrays.fill(arr, Byte.MIN_VALUE);
        BigInteger testBytesMinValue = new BigInteger(arr);

        Assert.assertEquals(testValue32Bytes.negate(), callStatic("negateBigInteger", testValue32Bytes).getDecodedReturnData());
        Assert.assertEquals(testValue16Bytes.negate(), callStatic("negateBigInteger", testValue16Bytes).getDecodedReturnData());
        Assert.assertEquals(testBytesMinValue.negate(), callStatic("negateBigInteger", testBytesMinValue).getDecodedReturnData());
    }

    @Test
    public void shiftRight() {
        Assert.assertEquals(testValue32Bytes.shiftRight(5), callStatic("shiftRight", testValue32Bytes).getDecodedReturnData());
        Assert.assertEquals(testValue16Bytes.shiftRight(5), callStatic("shiftRight", testValue16Bytes).getDecodedReturnData());
    }

    @Test
    public void flipBit() {
        Assert.assertEquals(testValue32Bytes.flipBit(10), callStatic("flipBit", testValue32Bytes, 10).getDecodedReturnData());
        Assert.assertEquals(testValue16Bytes.flipBit(32), callStatic("flipBit", testValue16Bytes, 32).getDecodedReturnData());
    }

    @Test
    public void bitCountBigInteger() {
        Assert.assertEquals(testValue32Bytes.bitCount(), callStatic("bitCountBigInteger", testValue32Bytes).getDecodedReturnData());
        Assert.assertEquals(testValue16Bytes.bitCount(), callStatic("bitCountBigInteger", testValue16Bytes).getDecodedReturnData());
    }

    @Test
    public void signum() {
        Assert.assertEquals(testValue32Bytes.signum(), callStatic("signum", testValue32Bytes).getDecodedReturnData());
        Assert.assertEquals(testValue16Bytes.signum(), callStatic("signum", testValue16Bytes).getDecodedReturnData());

        byte[] arr = new byte[12];
        Arrays.fill(arr, Byte.MAX_VALUE);
        BigInteger testValue = new BigInteger(-1, arr);
        Assert.assertEquals(testValue.signum(), callStatic("signum", testValue).getDecodedReturnData());

        testValue = new BigInteger(1, arr);
        Assert.assertEquals(testValue.signum(), callStatic("signum", testValue).getDecodedReturnData());

        testValue = new BigInteger(0, new byte[]{0});
        Assert.assertEquals(testValue.signum(), callStatic("signum", testValue).getDecodedReturnData());
    }

    @Test
    public void getLowestSetBit() {
        Assert.assertEquals(testValue32Bytes.getLowestSetBit(), callStatic("getLowestSetBit", testValue32Bytes).getDecodedReturnData());
        Assert.assertEquals(testValue16Bytes.getLowestSetBit(), callStatic("getLowestSetBit", testValue16Bytes).getDecodedReturnData());
        byte[] arr = new byte[12];
        Arrays.fill(arr, Byte.MAX_VALUE);
        BigInteger testValue = new BigInteger(-1, arr);
        Assert.assertEquals(testValue.getLowestSetBit(), callStatic("getLowestSetBit", testValue).getDecodedReturnData());
    }

    @Test
    public void checkArrayLength() {
        Assert.assertArrayEquals(bigIntegerArray, (BigInteger[]) callStatic("checkArrayLength", bigIntegerArray, bigIntegerArray.length).getDecodedReturnData());
    }

    @Test
    public void compareTo() {
        Assert.assertTrue(callStatic("compareTo", (Object) bigIntegerArray).getTransactionResult().transactionStatus.isSuccess());
    }

    @Test
    public void negateArray() {
        BigInteger[] negated = new BigInteger[bigIntegerArray.length];
        for (int i = 0; i < bigIntegerArray.length; i++) {
            negated[i] = bigIntegerArray[i].negate();
        }
        Assert.assertArrayEquals(negated, (BigInteger[]) callStatic("negateArray", (Object) bigIntegerArray).getDecodedReturnData());
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

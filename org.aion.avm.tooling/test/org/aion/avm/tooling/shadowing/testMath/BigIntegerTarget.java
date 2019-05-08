package org.aion.avm.tooling.shadowing.testMath;

import org.aion.avm.tooling.abi.Callable;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BigIntegerTarget {

    @Callable
    public static byte[] add(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.add(testValue);
        return result.toByteArray();
    }

    @Callable
    public static byte[] multiply(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.multiply(testValue);
        return result.toByteArray();
    }

    @Callable
    public static byte[] remainder(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.remainder(BigInteger.TWO);
        return result.toByteArray();
    }

    @Callable
    public static byte[] sqrt(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.sqrt();
        return result.toByteArray();

    }

    @Callable
    public static byte[] negate(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.negate();
        return result.toByteArray();
    }

    @Callable
    public static byte[] mod(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.mod(BigInteger.TWO);
        return result.toByteArray();
    }

    @Callable
    public static byte[] modPow(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.modPow(testValue, BigInteger.TWO);
        return result.toByteArray();
    }

    @Callable
    public static byte[] modInverse(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.modInverse(BigInteger.TWO);
        return result.toByteArray();
    }

    @Callable
    public static byte[] shiftLeft(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.shiftLeft(10);
        return result.toByteArray();
    }

    @Callable
    public static byte[] clearBit(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.clearBit(val.length / 2);
        return result.toByteArray();
    }

    @Callable
    public static byte[] setBit(byte[] val, int num) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.setBit(num);
        return result.toByteArray();
    }

    @Callable
    public static byte[] min(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.min(testValue);
        return result.toByteArray();
    }

    @Callable
    public static byte[] and(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        BigInteger result = testValue.and(testValue);
        return result.toByteArray();
    }

    @Callable
    public static boolean shiftLeftException(int count) {
        boolean isThrown = false;
        try {
            BigInteger.ONE.shiftLeft(count);
        } catch (ArithmeticException e){
            isThrown = true;
        }
        return isThrown;
    }

    @Callable
    public static int bitCount(byte[] val) {
        BigInteger testValue = new BigInteger(val);
        return testValue.bitCount();
    }

    @Callable
    public static void bigDecimalToBigIntegerException(byte[] val) {
        BigDecimal.valueOf(Double.MAX_VALUE).toBigInteger();
    }

    @Callable
    public static byte[] newBigInteger(int off, int len, byte[] val) {
        return new BigInteger(-1, val, off, len).toByteArray();
    }

    @Callable
    public static boolean catchExceptionOfOperation(byte[] val1, byte[] val2) {
        boolean didFail = false;
        try {
            new BigInteger(val1).multiply(new BigInteger(val2));
        } catch (ArithmeticException e) {
            didFail = true;
        }
        return didFail;
    }

    @Callable
    public static void NPEThrownCorrectly() {
        BigInteger result;
        try {
            result = BigInteger.TEN.multiply(null);
            throw new RuntimeException("Instead of NPE got " + result);
        } catch (NullPointerException npe) {
            // Expected
        }

        try {
            result = BigInteger.TEN.divide(null);
            throw new RuntimeException("Instead of NPE got " + result);
        } catch (NullPointerException npe) {
           // Expected
        }

        try {
            result = BigInteger.TEN.add(null);
            throw new RuntimeException("Instead of NPE got " + result);
        } catch (NullPointerException npe) {
            // Expected
        }

        try {
            result = BigInteger.TEN.subtract(null);
            throw new RuntimeException("Instead of NPE got " + result);
        } catch (NullPointerException npe) {
            // Expected
        }
    }
}

package org.aion.avm.core.assumptions;

import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;


/**
 * A small test to verify that the java.math.BigInteger behaves as we assume.
 * This is mostly just for our convenience of looking up these case, when we are curious.
 */
public class BigIntegerAssumptionsTest {
    /**
     * A BigInteger cannot be created over null.
     */
    @Test(expected=NullPointerException.class)
    public void testNullBytesFailure() {
        new BigInteger((byte[])null);
    }

    /**
     * A BigInteger cannot be created over empty byte[].
     */
    @Test(expected=NumberFormatException.class)
    public void testEmptyBytesFailure() {
        new BigInteger(new byte[0]);
    }

    /**
     * A BigInteger _can_ be created over byte[0] if a sign byte is given.
     */
    @Test
    public void testEmptyBytesWithSign() {
        Assert.assertEquals(0L, new BigInteger(1, new byte[0]).longValue());
        Assert.assertEquals(0L, new BigInteger(0, new byte[0]).longValue());
        Assert.assertEquals(0L, new BigInteger(-1, new byte[0]).longValue());
    }

    /**
     * The common case of BigInteger creation.
     */
    @Test
    public void testBytes() {
        Assert.assertEquals(0L, new BigInteger(new byte[] {0}).longValue());
        Assert.assertEquals(127L, new BigInteger(new byte[] {127}).longValue());
        Assert.assertEquals(-128L, new BigInteger(new byte[] {-128}).longValue());
    }

    /**
     * BigInteger creation with explicit positive sign.
     */
    @Test
    public void testBytesWithSign() {
        Assert.assertEquals(0L, new BigInteger(1, new byte[] {0}).longValue());
        Assert.assertEquals(127L, new BigInteger(1, new byte[] {127}).longValue());
        Assert.assertEquals(128L, new BigInteger(1, new byte[] {-128}).longValue());
    }

    /**
     * BigInteger bytes after explicitly changing sign.
     */
    @Test
    public void testToBytesWithSign() {
        Assert.assertArrayEquals(new byte[] {0, -128}, new BigInteger(1, new byte[] {-128}).toByteArray());
    }

    /**
     * Tests that a leading zero is ok with a positive byte.
     */
    @Test
    public void testPositiveWithZeroByte() {
        Assert.assertEquals(127L, new BigInteger(1, new byte[] {0, 127}).longValue());
    }

    /**
     * Tests that a leading zero is ok with a negative byte.
     */
    @Test
    public void testNegativeWithZeroByte() {
        Assert.assertEquals(128L, new BigInteger(1, new byte[] {0, -128}).longValue());
    }
}

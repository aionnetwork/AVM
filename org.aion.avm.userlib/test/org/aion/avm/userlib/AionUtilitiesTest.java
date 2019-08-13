package org.aion.avm.userlib;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;

public class AionUtilitiesTest {

    @Test
    public void testBigIntegerPadding() {
        BigInteger value = new BigInteger(new byte[]{10, 100, 120});
        byte[] topic = AionUtilities.padLeft(value.toByteArray());
        Assert.assertEquals(32, topic.length);
        BigInteger topicValue = new BigInteger(topic);
        Assert.assertEquals(value, topicValue);


        byte[] arr = new byte[32];
        Arrays.fill(arr, Byte.MAX_VALUE);
        value = new BigInteger(arr);
        topic = AionUtilities.padLeft(value.toByteArray());
        Assert.assertEquals(32, topic.length);
        topicValue = new BigInteger(topic);
        Assert.assertEquals(value, topicValue);

        arr = new byte[33];
        Arrays.fill(arr, Byte.MAX_VALUE);
        value = new BigInteger(arr);
        topic = AionUtilities.padLeft(value.toByteArray());
        Assert.assertEquals(33, topic.length);
        topicValue = new BigInteger(topic);
        Assert.assertEquals(value, topicValue);
    }

    @Test
    public void nullInputTest() {
        boolean exceptionThrown = false;
        byte[] value = null;
        try {
            AionUtilities.padLeft(value);
        } catch (NullPointerException e) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }
}

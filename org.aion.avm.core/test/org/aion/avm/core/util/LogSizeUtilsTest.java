package org.aion.avm.core.util;

import org.junit.Assert;
import org.junit.Test;


/**
 * Just a simple test to verify a few of the cases around the issue-358 specification on log topic size.
 */
public class LogSizeUtilsTest {
    @Test(expected = NullPointerException.class)
    public void testNull() throws Exception {
        byte[] input = null;
        LogSizeUtils.truncatePadTopic(input);
    }

    @Test
    public void test32() throws Exception {
        byte[] input = createArray(LogSizeUtils.TOPIC_SIZE);
        byte[] output = LogSizeUtils.truncatePadTopic(input);
        Assert.assertArrayEquals(input, output);
    }

    @Test
    public void testSmall() throws Exception {
        byte[] input = createArray(LogSizeUtils.TOPIC_SIZE / 2);
        byte[] output = LogSizeUtils.truncatePadTopic(input);
        Assert.assertEquals(LogSizeUtils.TOPIC_SIZE, output.length);
        byte[] prefixOfOutput = new byte[input.length];
        System.arraycopy(output, 0, prefixOfOutput, 0, prefixOfOutput.length);
        Assert.assertArrayEquals(input, prefixOfOutput);
        for (int i = input.length; i < LogSizeUtils.TOPIC_SIZE; ++i) {
            Assert.assertEquals((byte)0, output[i]);
        }
    }

    @Test
    public void testLarge() throws Exception {
        byte[] input = createArray(LogSizeUtils.TOPIC_SIZE * 2);
        byte[] output = LogSizeUtils.truncatePadTopic(input);
        Assert.assertEquals(LogSizeUtils.TOPIC_SIZE, output.length);
        byte[] prefixOfInput= new byte[output.length];
        System.arraycopy(input, 0, prefixOfInput, 0, prefixOfInput.length);
        Assert.assertArrayEquals(output, prefixOfInput);
    }

    @Test
    public void testEmpty() throws Exception {
        byte[] input = createArray(0);
        byte[] output = LogSizeUtils.truncatePadTopic(input);
        Assert.assertEquals(LogSizeUtils.TOPIC_SIZE, output.length);
        for (int i = 0; i < LogSizeUtils.TOPIC_SIZE; ++i) {
            Assert.assertEquals((byte)0, output[i]);
        }
    }


    private static byte[] createArray(int length) {
        byte[] array = new byte[length];
        for (int i = 0; i < length; ++i) {
            array[i] = (byte)i;
        }
        return array;
    }
}

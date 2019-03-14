package org.aion.avm.api.tests;

import org.aion.avm.api.Result;
import org.junit.Assert;
import org.junit.Test;


public class ResultTest {
    @Test
    public void testToString() {
        byte[] data = createByteArray(0);
        byte[] data1 = createByteArray(1);

        String result = new Result(true, data).toString();
        Assert.assertEquals(
                "success:true, returnData:000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f",
                result);

        result = new Result(false, data).toString();
        Assert.assertEquals(
                "success:false, returnData:000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f",
                result);

        result = new Result(true, data1).toString();
        Assert.assertEquals(
                "success:true, returnData:0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20",
                result);
    }

    @Test
    public void testEquals() {
        byte[] data = createByteArray(0);
        byte[] data1 = createByteArray(1);

        boolean result = new Result(true, data).equals(new Result(true, data));
        Assert.assertTrue(result);

        result = new Result(true, data).equals(new Result(true, data1));
        Assert.assertFalse(result);

        result = new Result(true, data).equals(new Result(false, data));
        Assert.assertFalse(result);
    }

    @Test
    public void testHashCode() {
        byte[] data = createByteArray(0);
        byte[] data1 = createByteArray(1);

        int result = new Result(true, data).hashCode();
        Assert.assertEquals(497, result);

        result = new Result(false, data).hashCode();
        Assert.assertEquals(496, result);

        result = new Result(true, data1).hashCode();
        Assert.assertEquals(529, result);
    }

    private byte[] createByteArray(int startValue) {
        byte[] data = new byte[32];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte)(i + startValue);
        }

        return data;
    }
}
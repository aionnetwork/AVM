package avm.tests;

import avm.Address;
import org.junit.Assert;
import org.junit.Test;


public class AddressTest {
    @Test
    public void testToString() {
        byte[] data = createByteArray(0);
        byte[] data1 = createByteArray(1);

        String result = new Address(data).toString();
        Assert.assertEquals("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", result);

        result = new Address(data1).toString();
        Assert.assertEquals("0102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f20", result);
    }

    @Test
    public void testEquals() {
        byte[] data = createByteArray(0);
        byte[] data1 = createByteArray(1);

        boolean result = new Address(data).equals(new Address(data));
        Assert.assertTrue(result);

        result = new Address(data).equals(new Address(data1));
        Assert.assertFalse(result);
    }

    @Test
    public void testHashCode() {
        byte[] data = createByteArray(0);
        byte[] data1 = createByteArray(1);

        int result = new Address(data).hashCode();
        Assert.assertEquals(496, result);

        result = new Address(data1).hashCode();
        Assert.assertEquals(528, result);
    }

    private byte[] createByteArray(int startValue) {
        byte[] data = new byte[32];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte)(i + startValue);
        }

        return data;
    }

}
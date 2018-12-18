package org.aion.avm.api;

import org.junit.Assert;
import org.junit.Test;


public class ABIEncoderTest {
    @Test
    public void testPrimitiveEncode() {
        byte[] encoded = ABIEncoder.encodeOneObject(Byte.valueOf((byte)-1));
        Assert.assertArrayEquals(new byte[] {66, -1}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Character.valueOf('a'));
        Assert.assertArrayEquals(new byte[] {67, 97}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Boolean.valueOf(true));
        Assert.assertArrayEquals(new byte[] {90, 1}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Short.valueOf((short)1000));
        Assert.assertArrayEquals(new byte[] {83, 3, -24}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Integer.valueOf(1000));
        Assert.assertArrayEquals(new byte[] {73, 0, 0, 3, -24}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Float.valueOf(1000.0F));
        Assert.assertArrayEquals(new byte[] {70, 68, 122, 0, 0}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Long.valueOf(1000L));
        Assert.assertArrayEquals(new byte[] {76, 0, 0, 0, 0, 0, 0, 3, -24}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Double.valueOf(1000.0));
        Assert.assertArrayEquals(new byte[] {68, 64, -113, 64, 0, 0, 0, 0, 0}, encoded);
    }
}

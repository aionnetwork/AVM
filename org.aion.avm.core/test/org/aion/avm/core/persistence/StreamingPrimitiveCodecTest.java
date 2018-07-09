package org.aion.avm.core.persistence;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;


public class StreamingPrimitiveCodecTest {
    @Test
    public void serializeOneOfEach() {
        byte one = 5;
        short two= 5;
        char three = 5;
        int four = 5;
        long five = 5;
        byte[] six = new byte[] {1,2,3,4,5};
        
        byte[] result = StreamingPrimitiveCodec.buildEncoder()
            .encodeByte(one)
            .encodeShort(two)
            .encodeChar(three)
            .encodeInt(four)
            .encodeLong(five)
            .encodeBytes(six)
            .toBytes();
        Assert.assertEquals(Byte.BYTES + Short.BYTES + Character.BYTES + Integer.BYTES + Long.BYTES + six.length, result.length);
        
        StreamingPrimitiveCodec.Decoder decoder = StreamingPrimitiveCodec.buildDecoder(result);
        Assert.assertEquals(one, decoder.decodeByte());
        Assert.assertEquals(two, decoder.decodeShort());
        Assert.assertEquals(three, decoder.decodeChar());
        Assert.assertEquals(four, decoder.decodeInt());
        Assert.assertEquals(five, decoder.decodeLong());
        byte[] found = new byte[six.length];
        decoder.decodeBytesInto(found);
        Assert.assertTrue(Arrays.equals(six, found));
    }

    @Test
    public void serializeEachExtreme() {
        byte[] result = StreamingPrimitiveCodec.buildEncoder()
            .encodeByte(Byte.MAX_VALUE)
            .encodeByte(Byte.MIN_VALUE)
            .encodeShort(Short.MAX_VALUE)
            .encodeShort(Short.MIN_VALUE)
            .encodeChar(Character.MAX_VALUE)
            .encodeChar(Character.MIN_VALUE)
            .encodeInt(Integer.MAX_VALUE)
            .encodeInt(Integer.MIN_VALUE)
            .encodeLong(Long.MAX_VALUE)
            .encodeLong(Long.MIN_VALUE)
            .toBytes();
        Assert.assertEquals(2 * Byte.BYTES + 2 * Short.BYTES + 2 * Character.BYTES + 2 * Integer.BYTES + 2 * Long.BYTES, result.length);
        
        StreamingPrimitiveCodec.Decoder decoder = StreamingPrimitiveCodec.buildDecoder(result);
        Assert.assertEquals(Byte.MAX_VALUE, decoder.decodeByte());
        Assert.assertEquals(Byte.MIN_VALUE, decoder.decodeByte());
        Assert.assertEquals(Short.MAX_VALUE, decoder.decodeShort());
        Assert.assertEquals(Short.MIN_VALUE, decoder.decodeShort());
        Assert.assertEquals(Character.MAX_VALUE, decoder.decodeChar());
        Assert.assertEquals(Character.MIN_VALUE, decoder.decodeChar());
        Assert.assertEquals(Integer.MAX_VALUE, decoder.decodeInt());
        Assert.assertEquals(Integer.MIN_VALUE, decoder.decodeInt());
        Assert.assertEquals(Long.MAX_VALUE, decoder.decodeLong());
        Assert.assertEquals(Long.MIN_VALUE, decoder.decodeLong());
    }
}

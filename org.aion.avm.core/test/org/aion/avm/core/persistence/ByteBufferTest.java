package org.aion.avm.core.persistence;


import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;


public class ByteBufferTest {

    @Test
    public void putDoubleTest() {
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        Double NaN1 = Double.NaN;
        Double NaN2 = Double.longBitsToDouble(0x7ff8000000000000L);
        Double NaN3 = Double.longBitsToDouble(0x7ff4000000000000L);

        ByteBufferObjectSerializer byteBufferObjectSerializer = new ByteBufferObjectSerializer(buffer, null, null, null, null);

        byteBufferObjectSerializer.writeDouble(NaN1);
        byteBufferObjectSerializer.writeDouble(NaN2);
        byteBufferObjectSerializer.writeDouble(NaN3);

        buffer.putLong(1452L);
        buffer.flip();

        ByteBufferObjectDeserializer byteBufferObjectDeserializer = new ByteBufferObjectDeserializer(buffer, null, null, null, null);

        Double deserialized1 = byteBufferObjectDeserializer.readDouble();
        Double deserialized2 = byteBufferObjectDeserializer.readDouble();
        Double deserialized3 = byteBufferObjectDeserializer.readDouble();

        Assert.assertEquals(Double.NaN, deserialized1, 0.0);
        Assert.assertEquals(Double.NaN, deserialized2, 0.0);
        Assert.assertEquals(Double.NaN, deserialized3, 0.0);

        Assert.assertEquals(Double.doubleToLongBits(NaN1), Double.doubleToLongBits(deserialized1));
        Assert.assertEquals(Double.doubleToLongBits(NaN2), Double.doubleToLongBits(deserialized2));
        Assert.assertEquals(Double.doubleToLongBits(NaN3), Double.doubleToLongBits(deserialized3));
    }

}

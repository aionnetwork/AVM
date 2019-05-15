package org.aion.avm.core.persistence;


import i.CodecIdioms;
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

    @Test
    public void putArraysTest(){
        ByteBuffer buffer = ByteBuffer.allocate(500_000);

        byte[] byteArray = new byte[]{0x03, (byte) 0x67, (byte) 0xf7, (byte) 0x14};
        boolean[] booleanArray = new boolean[] {true, false, true, true};
        Float fl = 0.04f;
        String str = "MyTestString";

        //serialize
        ByteBufferObjectSerializer byteBufferObjectSerializer = new ByteBufferObjectSerializer(buffer, null, null, null, null);
        CodecIdioms.serializeByteArray(byteBufferObjectSerializer, byteArray);
        CodecIdioms.serializeBooleanArray(byteBufferObjectSerializer, booleanArray);
        byteBufferObjectSerializer.writeFloat(fl);
        CodecIdioms.serializeString(byteBufferObjectSerializer, str);

        buffer.rewind();

        //deserialize
        ByteBufferObjectDeserializer byteBufferObjectDeserializer = new ByteBufferObjectDeserializer(buffer, null, null, null, null);
        byte[] byteArrayRes = CodecIdioms.deserializeByteArray(byteBufferObjectDeserializer);
        boolean[] booleanArrayRes = CodecIdioms.deserializeBooleanArray(byteBufferObjectDeserializer);
        Float flRes = byteBufferObjectDeserializer.readFloat();
        String strRes = CodecIdioms.deserializeString(byteBufferObjectDeserializer);

        Assert.assertArrayEquals(byteArray, byteArrayRes);
        Assert.assertArrayEquals(booleanArray, booleanArrayRes);
        Assert.assertEquals(fl, flRes);
        Assert.assertEquals(str, strRes);
    }
}

package org.aion.avm.core.persistence;

import java.util.Arrays;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.Helper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SingleInstanceDeserializerTest {
    private static final SingleInstanceDeserializer.IAutomatic NULL_AUTOMATIC = new SingleInstanceDeserializer.IAutomatic() {
        @Override
        public void partialAutomaticDeserializeInstance(StreamingPrimitiveCodec.Decoder decoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        }
        @Override
        public org.aion.avm.shadow.java.lang.Object decodeStub(StreamingPrimitiveCodec.Decoder decoder) {
            // We just check null, or not, by looking at the next byte:  if 0x1, return an ObjectArray, if 0x0, return null.
            byte next = decoder.decodeByte();
            return (0x0 == next) 
                    ? null
                    : new ObjectArray(1);
        }};

    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        new Helper(SingleInstanceDeserializerTest.class.getClassLoader(), 1_000_000L, 1);
    }

    @After
    public void tearDown() {
        Helper.clearTestingState();
    }

    @Test
    public void deserializeByteArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x1,
                0x2,
                0x3,
        };
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(expected);
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        ByteArray bytes = new ByteArray(null, 1l);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new byte[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeShortArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x1,
                0x0, 0x2,
                0x0, 0x3,
        };
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(expected);
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        ShortArray bytes = new ShortArray(null, 1l);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new short[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeCharArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x1,
                0x0, 0x2,
                0x0, 0x3,
        };
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(expected);
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        CharArray bytes = new CharArray(null, 1l);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new char[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeIntArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x0, 0x0, 0x1,
                0x0, 0x0, 0x0, 0x2,
                0x0, 0x0, 0x0, 0x3,
        };
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(expected);
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        IntArray bytes = new IntArray(null, 1l);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new int[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeFloatArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x3f, (byte)0x80, 0x0, 0x0,
                0x40, 0x0, 0x0, 0x0,
                0x40, 0x40, 0x0, 0x0,
        };
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(expected);
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        FloatArray bytes = new FloatArray(null, 1l);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new float[] {1.0f,2.0f,3.0f}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeLongArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x2,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x3,
        };
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(expected);
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        LongArray bytes = new LongArray(null, 1l);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new long[] {1,2,3}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeDoubleArray() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x3f, (byte)0xf0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
                0x40, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
                0x40, 0x8, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        };
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(expected);
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        DoubleArray bytes = new DoubleArray(null, 1l);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new double[] {1.0,2.0,3.0}, bytes.getUnderlying()));
    }

    @Test
    public void deserializeShadowString() {
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x4, //UTF-8 length
                0x54,
                0x45,
                0x53,
                0x54,
        };
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(expected);
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        org.aion.avm.shadow.java.lang.String bytes = new org.aion.avm.shadow.java.lang.String((String)null);
        bytes.deserializeSelf(null, target);
        Assert.assertEquals("TEST", bytes.getV());
    }

    @Test
    public void serializeObjectArray() {
        // (note that we are using the fake stub encoding, in NULL_AUTOMATIC).
        byte[] expected = {
                0x0, 0x0, 0x0, 0x2, //hashcode
                0x0, 0x0, 0x0, 0x2, //length
                0x1, // instance
                0x0, // null
        };
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(expected);
        SingleInstanceDeserializer target = new SingleInstanceDeserializer(NULL_AUTOMATIC, decoder);
        ObjectArray bytes = new ObjectArray(null, 1l);
        bytes.deserializeSelf(null, target);
        Assert.assertEquals(2, bytes.length());
        Assert.assertNotNull(bytes.get(0));
        Assert.assertNull(bytes.get(1));
    }
}

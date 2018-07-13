package org.aion.avm.core.persistence;

import java.util.Arrays;

import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.core.persistence.StreamingPrimitiveCodec.Decoder;
import org.aion.avm.internal.Helper;
import org.aion.avm.shadow.java.lang.Object;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SingleInstanceDeserializerTest {
    private static final SingleInstanceDeserializer.IAutomatic NULL_AUTOMATIC = new SingleInstanceDeserializer.IAutomatic() {
        @Override
        public void partialAutomaticDeserializeInstance(Decoder decoder, Object instance, Class<?> firstManualClass) {
        }};

    @Before
    public void setup() {
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
        ByteArray bytes = new ByteArray(null);
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
        ShortArray bytes = new ShortArray(null);
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
        CharArray bytes = new CharArray(null);
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
        IntArray bytes = new IntArray(null);
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
        FloatArray bytes = new FloatArray(null);
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
        LongArray bytes = new LongArray(null);
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
        DoubleArray bytes = new DoubleArray(null);
        bytes.deserializeSelf(null, target);
        Assert.assertTrue(Arrays.equals(new double[] {1.0,2.0,3.0}, bytes.getUnderlying()));
    }
}

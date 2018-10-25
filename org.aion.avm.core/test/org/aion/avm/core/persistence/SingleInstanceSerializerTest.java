package org.aion.avm.core.persistence;

import java.util.Arrays;
import java.util.function.Consumer;

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


public class SingleInstanceSerializerTest {
    private static final SingleInstanceSerializer.IAutomatic NULL_AUTOMATIC = new SingleInstanceSerializer.IAutomatic() {
        @Override
        public void partialAutomaticSerializeInstance(SerializedRepresentationCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        }
        @Override
        public void encodeAsStub(SerializedRepresentationCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object object, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
            // For this test, we will just write a 0x1 or 0x0 depending on whether or not object was null (since this is a unit test of the flow, not the encoding).
            if (null != object) {
                encoder.encodeByte((byte)0x1);
            } else {
                encoder.encodeByte((byte)0x0);
            }
        }};
    private static final Consumer<org.aion.avm.shadow.java.lang.Object> NULL_OBJECT_SINK = (object) -> {};

    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        new Helper(SingleInstanceSerializerTest.class.getClassLoader(), 1_000_000L, 1);
    }

    @After
    public void tearDown() {
        Helper.clearTestingState();
    }

    @Test
    public void serializeByteArray() {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer target = new SingleInstanceSerializer(NULL_AUTOMATIC, encoder, NULL_OBJECT_SINK);
        ByteArray bytes = new ByteArray(new byte[] {1,2,3});
        bytes.serializeSelf(null, target);
        byte[] result = encoder.toSerializedRepresentation().data;
        
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x1,
                0x2,
                0x3,
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void serializeShortArray() {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer target = new SingleInstanceSerializer(NULL_AUTOMATIC, encoder, NULL_OBJECT_SINK);
        ShortArray bytes = new ShortArray(new short[] {1,2,3});
        bytes.serializeSelf(null, target);
        byte[] result = encoder.toSerializedRepresentation().data;
        
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x1,
                0x0, 0x2,
                0x0, 0x3,
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void serializeCharArray() {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer target = new SingleInstanceSerializer(NULL_AUTOMATIC, encoder, NULL_OBJECT_SINK);
        CharArray bytes = new CharArray(new char[] {1,2,3});
        bytes.serializeSelf(null, target);
        byte[] result = encoder.toSerializedRepresentation().data;
        
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x1,
                0x0, 0x2,
                0x0, 0x3,
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void serializeIntArray() {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer target = new SingleInstanceSerializer(NULL_AUTOMATIC, encoder, NULL_OBJECT_SINK);
        IntArray bytes = new IntArray(new int[] {1,2,3});
        bytes.serializeSelf(null, target);
        byte[] result = encoder.toSerializedRepresentation().data;
        
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x0, 0x0, 0x1,
                0x0, 0x0, 0x0, 0x2,
                0x0, 0x0, 0x0, 0x3,
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void serializeFloatArray() {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer target = new SingleInstanceSerializer(NULL_AUTOMATIC, encoder, NULL_OBJECT_SINK);
        FloatArray bytes = new FloatArray(new float[] {1.0f,2.0f,3.0f});
        bytes.serializeSelf(null, target);
        byte[] result = encoder.toSerializedRepresentation().data;
        
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x3f, (byte)0x80, 0x0, 0x0,
                0x40, 0x0, 0x0, 0x0,
                0x40, 0x40, 0x0, 0x0,
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void serializeLongArray() {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer target = new SingleInstanceSerializer(NULL_AUTOMATIC, encoder, NULL_OBJECT_SINK);
        LongArray bytes = new LongArray(new long[] {1,2,3});
        bytes.serializeSelf(null, target);
        byte[] result = encoder.toSerializedRepresentation().data;
        
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x2,
                0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x3,
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void serializeDoubleArray() {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer target = new SingleInstanceSerializer(NULL_AUTOMATIC, encoder, NULL_OBJECT_SINK);
        DoubleArray bytes = new DoubleArray(new double[] {1.0,2.0,3.0});
        bytes.serializeSelf(null, target);
        byte[] result = encoder.toSerializedRepresentation().data;
        
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x3, //length
                0x3f, (byte)0xf0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
                0x40, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
                0x40, 0x8, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void serializeShadowString() {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer target = new SingleInstanceSerializer(NULL_AUTOMATIC, encoder, NULL_OBJECT_SINK);
        org.aion.avm.shadow.java.lang.String bytes = new org.aion.avm.shadow.java.lang.String("TEST");
        bytes.serializeSelf(null, target);
        byte[] result = encoder.toSerializedRepresentation().data;
        
        byte[] expected = {
                0x0, 0x0, 0x0, 0x1, //hashcode
                0x0, 0x0, 0x0, 0x4, //UTF-8 length
                0x54,
                0x45,
                0x53,
                0x54,
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }

    @Test
    public void serializeObjectArray() {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer target = new SingleInstanceSerializer(NULL_AUTOMATIC, encoder, NULL_OBJECT_SINK);
        ObjectArray holder = new ObjectArray(2);
        ObjectArray bytes = new ObjectArray(new Object[] {holder, null});
        bytes.serializeSelf(null, target);
        byte[] result = encoder.toSerializedRepresentation().data;
        
        // (note that we are using the fake stub encoding, in NULL_AUTOMATIC).
        byte[] expected = {
                0x0, 0x0, 0x0, 0x2, //hashcode
                0x0, 0x0, 0x0, 0x2, //length
                0x1, // instance
                0x0, // null
        };
        Assert.assertTrue(Arrays.equals(expected, result));
    }
}

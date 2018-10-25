package org.aion.avm.core.persistence;

import java.util.function.Function;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.Helper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class LoopbackCodecTest {
    @Before
    public void setup() {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        // Setup a helper since a few of these tests need to operate on shadow objects (mostly just since that is all the codec operates on).
        new Helper(ReflectionStructureCodecTarget.class.getClassLoader(), 1_000_000L, 1);
    }

    @After
    public void tearDown() {
        Helper.clearTestingState();
    }

    /**
     * Encodes one of each primitive and then decodes them.
     */
    @Test
    public void primitiveStream() {
        LoopbackCodec codec = new LoopbackCodec(null, null, null);
        codec.writeByte((byte)1);
        codec.writeChar((char)2);
        codec.writeInt(3);
        codec.writeLong(4l);
        codec.writeShort((short)5);
        codec.switchToDecode();
        
        Assert.assertEquals(1, codec.readByte());
        Assert.assertEquals(2, codec.readChar());
        Assert.assertEquals(3, codec.readInt());
        Assert.assertEquals(4, codec.readLong());
        Assert.assertEquals(5, codec.readShort());
    }

    /**
     * Encodes a primitive and tries to decode it, incorrectly.
     */
    @Test (expected = ClassCastException.class)
    public void incorrectPrimitiveDeserialization() {
        LoopbackCodec codec = new LoopbackCodec(null, null, null);
        codec.writeInt(1);
        codec.switchToDecode();
        
        // Fails on ClassCastException.
        Assert.assertEquals(1, codec.readLong());
    }

    /**
     * Tries to decode more than it encoded.
     * Note that we don't define what the actual error should be so we are just testing what we know the underlying implementation throws.
     */
    @Test (expected = ArrayIndexOutOfBoundsException.class)
    public void incompleteSerialization() {
        LoopbackCodec codec = new LoopbackCodec(null, null, null);
        codec.switchToDecode();
        
        // Fails on ArrayIndexOutOfBoundsException.
        Assert.assertEquals(1, codec.readLong());
    }

    @Test
    public void objectIdentitySerialization() {
        LoopbackCodec.AutomaticDeserializer deserializer = new IdentityDeserializer();
        LoopbackCodec codec = new LoopbackCodec(null, deserializer, null);
        org.aion.avm.shadow.java.lang.Object shadow = new org.aion.avm.shadow.java.lang.Object();
        codec.writeStub(shadow);
        codec.switchToDecode();
        
        Assert.assertEquals(shadow, codec.readStub());
    }

    @Test
    public void mixedSerialization() {
        LoopbackCodec.AutomaticDeserializer deserializer = new IdentityDeserializer();
        LoopbackCodec codec = new LoopbackCodec(null, deserializer, null);
        org.aion.avm.shadow.java.lang.Object shadow = new org.aion.avm.shadow.java.lang.Object();
        codec.writeInt(1);
        codec.writeStub(shadow);
        codec.writeLong(2L);
        codec.switchToDecode();
        
        Assert.assertEquals(1, codec.readInt());
        Assert.assertEquals(shadow, codec.readStub());
        Assert.assertEquals(2L, codec.readLong());
    }

    @Test
    public void takeOwnership_mixedSerialization() {
        LoopbackCodec.AutomaticDeserializer deserializer = new IdentityDeserializer();
        LoopbackCodec codec = new LoopbackCodec(null, deserializer, null);
        org.aion.avm.shadow.java.lang.Object shadow = new org.aion.avm.shadow.java.lang.Object();
        codec.writeInt(1);
        codec.writeStub(shadow);
        codec.writeLong(2L);
        
        HeapRepresentation representation = codec.takeOwnershipOfData();
        Assert.assertEquals(ByteSizes.INT + ByteSizes.REFERENCE + ByteSizes.LONG, representation.getBillableSize());
        
        // We expect 3 elements:  Integer, shadow.Object, Long.
        Integer one = (Integer)representation.primitives[0];
        org.aion.avm.shadow.java.lang.Object two = representation.references[0];
        Long three = (Long)representation.primitives[1];
        Assert.assertEquals(2, representation.primitives.length);
        Assert.assertEquals(1, representation.references.length);
        
        Assert.assertEquals(1, one.intValue());
        Assert.assertTrue(shadow == two);
        Assert.assertEquals(2L, three.longValue());
    }


    private static class IdentityDeserializer implements LoopbackCodec.AutomaticDeserializer {
        @Override
        public void partiallyAutoDeserialize(HeapRepresentationCodec.Decoder decoder, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
            Assert.fail("Not called in this test");
        }
    }
}

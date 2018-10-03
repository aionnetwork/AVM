package org.aion.avm.core.persistence;

import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.function.Function;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.RuntimeAssertionError;
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
        
        Assert.assertEquals(1, codec.readByte());
        Assert.assertEquals(2, codec.readChar());
        Assert.assertEquals(3, codec.readInt());
        Assert.assertEquals(4, codec.readLong());
        Assert.assertEquals(5, codec.readShort());
        codec.verifyDone();
    }

    /**
     * Encodes a primitive and tries to decode it, incorrectly.
     */
    @Test (expected = ClassCastException.class)
    public void incorrectPrimitiveDeserialization() {
        LoopbackCodec codec = new LoopbackCodec(null, null, null);
        codec.writeInt(1);
        
        // Fails on ClassCastException.
        Assert.assertEquals(1, codec.readLong());
        codec.verifyDone();
    }

    /**
     * Encodes something but doesn't finish decoding it.
     */
    @Test (expected = RuntimeAssertionError.class)
    public void incompleteDeserialization() {
        LoopbackCodec codec = new LoopbackCodec(null, null, null);
        codec.writeInt(1);
        
        // Fails on RuntimeAssertionError.
        codec.verifyDone();
    }

    /**
     * Tries to decode more than it encoded.
     */
    @Test (expected = NoSuchElementException.class)
    public void incompleteSerialization() {
        LoopbackCodec codec = new LoopbackCodec(null, null, null);
        
        // Fails on NoSuchElementException.
        Assert.assertEquals(1, codec.readLong());
        codec.verifyDone();
    }

    @Test
    public void objectIdentitySerialization() {
        LoopbackCodec.AutomaticDeserializer deserializer = new IdentityDeserializer();
        LoopbackCodec codec = new LoopbackCodec(null, deserializer, null);
        org.aion.avm.shadow.java.lang.Object shadow = new org.aion.avm.shadow.java.lang.Object();
        codec.writeStub(shadow);
        
        Assert.assertEquals(shadow, codec.readStub());
        codec.verifyDone();
    }

    @Test
    public void mixedSerialization() {
        LoopbackCodec.AutomaticDeserializer deserializer = new IdentityDeserializer();
        LoopbackCodec codec = new LoopbackCodec(null, deserializer, null);
        org.aion.avm.shadow.java.lang.Object shadow = new org.aion.avm.shadow.java.lang.Object();
        codec.writeInt(1);
        codec.writeStub(shadow);
        codec.writeLong(2L);
        
        Assert.assertEquals(1, codec.readInt());
        Assert.assertEquals(shadow, codec.readStub());
        Assert.assertEquals(2L, codec.readLong());
        codec.verifyDone();
    }

    @Test
    public void takeOwnership_mixedSerialization() {
        LoopbackCodec.AutomaticDeserializer deserializer = new IdentityDeserializer();
        LoopbackCodec codec = new LoopbackCodec(null, deserializer, null);
        org.aion.avm.shadow.java.lang.Object shadow = new org.aion.avm.shadow.java.lang.Object();
        codec.writeInt(1);
        codec.writeStub(shadow);
        codec.writeLong(2L);
        
        Queue<Object> internals = codec.takeOwnershipOfData();
        codec.verifyDone();
        
        // We expect 3 elements:  Integer, shadow.Object, Long.
        Integer one = (Integer)internals.remove();
        org.aion.avm.shadow.java.lang.Object two = (org.aion.avm.shadow.java.lang.Object)internals.remove();
        Long three = (Long)internals.remove();
        Assert.assertTrue(internals.isEmpty());
        
        Assert.assertEquals(1, one.intValue());
        Assert.assertTrue(shadow == two);
        Assert.assertEquals(2L, three.longValue());
    }


    private static class IdentityDeserializer implements LoopbackCodec.AutomaticDeserializer {
        @Override
        public void partiallyAutoDeserialize(Queue<Object> dataQueue, Function<org.aion.avm.shadow.java.lang.Object, org.aion.avm.shadow.java.lang.Object> deserializeHelper, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
            Assert.fail("Not called in this test");
        }
    }
}

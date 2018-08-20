package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.persistence.ReflectionStructureCodec.IFieldPopulator;
import org.aion.avm.internal.Helper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SerializedInstanceStubTest {
    private Field instanceIdField;
    private IFieldPopulator fieldPopulator;

    @Before
    public void setup() throws Exception {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        new Helper(ReflectionStructureCodecTarget.class.getClassLoader(), 1_000_000L, 1);
        this.instanceIdField = org.aion.avm.shadow.java.lang.Object.class.getField("instanceId");
        this.fieldPopulator = new BasicPopulator();
    }

    @After
    public void tearDown() {
        Helper.clearTestingState();
    }

    @Test
    public void testNull() throws Exception {
        org.aion.avm.shadow.java.lang.Object inputInstance = null;
        
        // Size this.
        int byteSize = SerializedInstanceStub.sizeOfInstanceStub(inputInstance, this.instanceIdField);
        // Null is a direct special-case, serialized as 4 bytes (stub type).
        Assert.assertEquals(4, byteSize);
        
        // Encode this.
        StreamingPrimitiveCodec.Encoder encoder = new StreamingPrimitiveCodec.Encoder();
        boolean shouldEnqueue = SerializedInstanceStub.serializeInstanceStub(encoder, inputInstance, this.instanceIdField, () -> 1L);
        
        // Nulls should never be enqueued.
        Assert.assertFalse(shouldEnqueue);
        
        // Decode this.
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(encoder.toBytes());
        org.aion.avm.shadow.java.lang.Object instance = SerializedInstanceStub.deserializeInstanceStub(decoder, this.fieldPopulator);
        Assert.assertEquals(testingNull(), instance.toString());
    }

    @Test
    public void testConstant() throws Exception {
        org.aion.avm.shadow.java.lang.Object inputInstance = org.aion.avm.shadow.java.lang.Boolean.avm_TYPE;
        
        // Size this.
        int byteSize = SerializedInstanceStub.sizeOfInstanceStub(inputInstance, this.instanceIdField);
        // Constants are a special-case, encoded as 4 bytes (stub type) + 8 bytes (constant id).
        Assert.assertEquals(4 + 8, byteSize);
        
        // Encode this.
        StreamingPrimitiveCodec.Encoder encoder = new StreamingPrimitiveCodec.Encoder();
        boolean shouldEnqueue = SerializedInstanceStub.serializeInstanceStub(encoder, inputInstance, this.instanceIdField, () -> 1L);
        
        // Constants should never be enqueued.
        Assert.assertFalse(shouldEnqueue);
        
        // Decode this.
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(encoder.toBytes());
        org.aion.avm.shadow.java.lang.Object instance = SerializedInstanceStub.deserializeInstanceStub(decoder, this.fieldPopulator);
        Assert.assertEquals(testingConstant(-18L), instance.toString());
    }

    @Test
    public void testClass() throws Exception {
        org.aion.avm.shadow.java.lang.Class<?> inputInstance = new org.aion.avm.shadow.java.lang.Class<>(String.class);
        
        // Size this.
        int byteSize = SerializedInstanceStub.sizeOfInstanceStub(inputInstance, this.instanceIdField);
        // Classes are stored as 4 bytes (stub type) + 4 bytes (type name length) + n bytes (type name UTF-8).
        Assert.assertEquals(4 + 4 + String.class.getName().getBytes(StandardCharsets.UTF_8).length, byteSize);
        
        // Encode this.
        StreamingPrimitiveCodec.Encoder encoder = new StreamingPrimitiveCodec.Encoder();
        boolean shouldEnqueue = SerializedInstanceStub.serializeInstanceStub(encoder, inputInstance, this.instanceIdField, () -> 1L);
        
        // Classes should never be enqueued.
        Assert.assertFalse(shouldEnqueue);
        
        // Decode this.
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(encoder.toBytes());
        org.aion.avm.shadow.java.lang.Object instance = SerializedInstanceStub.deserializeInstanceStub(decoder, this.fieldPopulator);
        Assert.assertEquals(testingClass(String.class.getName()), instance.toString());
    }

    @Test
    public void testInstance() throws Exception {
        org.aion.avm.shadow.java.lang.Object inputInstance = new org.aion.avm.shadow.java.lang.Object();
        
        // Size this.
        int byteSize = SerializedInstanceStub.sizeOfInstanceStub(inputInstance, this.instanceIdField);
        // Normal references are stored as 4 bytes (type name length) + n bytes (type name UTF-8) + 8 bytes (instance id).
        Assert.assertEquals(4 + org.aion.avm.shadow.java.lang.Object.class.getName().getBytes(StandardCharsets.UTF_8).length + 8, byteSize);
        
        // Encode this.
        StreamingPrimitiveCodec.Encoder encoder = new StreamingPrimitiveCodec.Encoder();
        boolean shouldEnqueue = SerializedInstanceStub.serializeInstanceStub(encoder, inputInstance, this.instanceIdField, () -> 1L);
        
        // Instances should be enqueued.
        Assert.assertTrue(shouldEnqueue);
        
        // Decode this.
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(encoder.toBytes());
        org.aion.avm.shadow.java.lang.Object instance = SerializedInstanceStub.deserializeInstanceStub(decoder, this.fieldPopulator);
        Assert.assertEquals(testingInstance(1L), instance.toString());
    }


    private static String testingInstance(long instanceId) {
        return "instance_" + instanceId;
    }

    private static String testingClass(String className) {
        return "class_" + className;
    }

    private static String testingConstant(long instanceId) {
        return "constant_" + instanceId;
    }

    private static String testingNull() {
        return "null";
    }


    private static class BasicPopulator implements IFieldPopulator {
        @Override
        public org.aion.avm.shadow.java.lang.Object createRegularInstance(String className, long instanceId) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            return new org.aion.avm.shadow.java.lang.String(testingInstance(instanceId));
        }
        @Override
        public org.aion.avm.shadow.java.lang.Object createClass(String className) throws ClassNotFoundException {
            return new org.aion.avm.shadow.java.lang.String(testingClass(className));
        }
        @Override
        public org.aion.avm.shadow.java.lang.Object createConstant(long instanceId) {
            return new org.aion.avm.shadow.java.lang.String(testingConstant(instanceId));
        }
        @Override
        public org.aion.avm.shadow.java.lang.Object createNull() {
            return new org.aion.avm.shadow.java.lang.String(testingNull());
        }
        @Override
        public void setBoolean(Field field, org.aion.avm.shadow.java.lang.Object object, boolean val) throws IllegalArgumentException, IllegalAccessException {
            Assert.fail("Not called in test");
        }
        @Override
        public void setDouble(Field field, org.aion.avm.shadow.java.lang.Object object, double val) throws IllegalArgumentException, IllegalAccessException {
            Assert.fail("Not called in test");
        }
        @Override
        public void setLong(Field field, org.aion.avm.shadow.java.lang.Object object, long val) throws IllegalArgumentException, IllegalAccessException {
            Assert.fail("Not called in test");
        }
        @Override
        public void setFloat(Field field, org.aion.avm.shadow.java.lang.Object object, float val) throws IllegalArgumentException, IllegalAccessException {
            Assert.fail("Not called in test");
        }
        @Override
        public void setInt(Field field, org.aion.avm.shadow.java.lang.Object object, int val) throws IllegalArgumentException, IllegalAccessException {
            Assert.fail("Not called in test");
        }
        @Override
        public void setChar(Field field, org.aion.avm.shadow.java.lang.Object object, char val) throws IllegalArgumentException, IllegalAccessException {
            Assert.fail("Not called in test");
        }
        @Override
        public void setShort(Field field, org.aion.avm.shadow.java.lang.Object object, short val) throws IllegalArgumentException, IllegalAccessException {
            Assert.fail("Not called in test");
        }
        @Override
        public void setByte(Field field, org.aion.avm.shadow.java.lang.Object object, byte val) throws IllegalArgumentException, IllegalAccessException {
            Assert.fail("Not called in test");
        }
        @Override
        public void setObject(Field field, org.aion.avm.shadow.java.lang.Object object, org.aion.avm.shadow.java.lang.Object val) throws IllegalArgumentException, IllegalAccessException {
            Assert.fail("Not called in test");
        }
    }
}

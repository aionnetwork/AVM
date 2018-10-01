package org.aion.avm.core.persistence;

import java.lang.reflect.Field;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.persistence.ReflectionStructureCodec.IFieldPopulator;
import org.aion.avm.core.persistence.keyvalue.KeyValueNode;
import org.aion.avm.core.persistence.keyvalue.KeyValueObjectGraph;
import org.aion.avm.internal.ConstantPersistenceToken;
import org.aion.avm.internal.Helper;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.KernelInterfaceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SerializedInstanceStubTest {
    private Field persistenceTokenField;
    private IFieldPopulator fieldPopulator;
    private IObjectGraphStore graphStore;

    @Before
    public void setup() throws Exception {
        // Force the initialization of the NodeEnvironment singleton.
        Assert.assertNotNull(NodeEnvironment.singleton);
        
        new Helper(ReflectionStructureCodecTarget.class.getClassLoader(), 1_000_000L, 1);
        this.persistenceTokenField = org.aion.avm.shadow.java.lang.Object.class.getField("persistenceToken");
        this.fieldPopulator = new BasicPopulator();
        KernelInterfaceImpl kernel = new KernelInterfaceImpl();
        byte[] address = new byte[] {1,2,3};
        this.graphStore = new KeyValueObjectGraph(kernel, address);
    }

    @After
    public void tearDown() {
        Helper.clearTestingState();
    }

    @Test
    public void testNull() throws Exception {
        org.aion.avm.shadow.java.lang.Object inputInstance = null;
        
        // Encode this.
        ExtentBasedCodec.Encoder encoder = new ExtentBasedCodec.Encoder();
        boolean shouldEnqueue = SerializedInstanceStub.serializeAsReference(encoder, inputInstance, this.graphStore, this.persistenceTokenField);
        
        // Nulls should never be enqueued.
        Assert.assertFalse(shouldEnqueue);
        
        // This should be a single INode reference.
        Extent extent = encoder.toExtent();
        Assert.assertEquals(1, extent.references.length);
        
        // Decode this.
        org.aion.avm.shadow.java.lang.Object instance = SerializedInstanceStub.deserializeReferenceAsInstance(extent.references[0], this.fieldPopulator);
        Assert.assertEquals(testingNull(), instance.toString());
    }

    @Test
    public void testConstant() throws Exception {
        org.aion.avm.shadow.java.lang.Object inputInstance = org.aion.avm.shadow.java.lang.Boolean.avm_TYPE;
        this.persistenceTokenField.set(inputInstance, new ConstantPersistenceToken(-18L));
        
        // Encode this.
        ExtentBasedCodec.Encoder encoder = new ExtentBasedCodec.Encoder();
        boolean shouldEnqueue = SerializedInstanceStub.serializeAsReference(encoder, inputInstance, this.graphStore, this.persistenceTokenField);
        
        // Constants should never be enqueued.
        Assert.assertFalse(shouldEnqueue);
        
        // This should be a single INode reference.
        Extent extent = encoder.toExtent();
        Assert.assertEquals(1, extent.references.length);
        
        // Decode this.
        org.aion.avm.shadow.java.lang.Object instance = SerializedInstanceStub.deserializeReferenceAsInstance(extent.references[0], this.fieldPopulator);
        Assert.assertEquals(testingConstant(-18L), instance.toString());
    }

    @Test
    public void testClass() throws Exception {
        // Note that we need to use the Helper in order to set the persistenceToken.
        org.aion.avm.shadow.java.lang.Class<?> inputInstance = Helper.wrapAsClass(String.class);
        
        // Encode this.
        ExtentBasedCodec.Encoder encoder = new ExtentBasedCodec.Encoder();
        boolean shouldEnqueue = SerializedInstanceStub.serializeAsReference(encoder, inputInstance, this.graphStore, this.persistenceTokenField);
        
        // Classes should never be enqueued.
        Assert.assertFalse(shouldEnqueue);
        
        // This should be a single INode reference.
        Extent extent = encoder.toExtent();
        Assert.assertEquals(1, extent.references.length);
        
        // Decode this.
        org.aion.avm.shadow.java.lang.Object instance = SerializedInstanceStub.deserializeReferenceAsInstance(extent.references[0], this.fieldPopulator);
        Assert.assertEquals(testingClass(String.class.getName()), instance.toString());
    }

    @Test
    public void testInstance() throws Exception {
        org.aion.avm.shadow.java.lang.Object inputInstance = new org.aion.avm.shadow.java.lang.Object();
        
        // Encode this.
        ExtentBasedCodec.Encoder encoder = new ExtentBasedCodec.Encoder();
        boolean shouldEnqueue = SerializedInstanceStub.serializeAsReference(encoder, inputInstance, this.graphStore, this.persistenceTokenField);
        
        // Instances should be enqueued.
        Assert.assertTrue(shouldEnqueue);
        
        // This should be a single INode reference.
        Extent extent = encoder.toExtent();
        Assert.assertEquals(1, extent.references.length);
        
        // Decode this.
        org.aion.avm.shadow.java.lang.Object instance = SerializedInstanceStub.deserializeReferenceAsInstance(extent.references[0], this.fieldPopulator);
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
        public org.aion.avm.shadow.java.lang.Object instantiateReference(INode rawNode) {
            // We handle these in differently descriptive way (note that this implementation is tightly coupled to the KeyValueObjectGraph).
            org.aion.avm.shadow.java.lang.Object result = null;
            if (rawNode instanceof KeyValueNode) {
                KeyValueNode node = (KeyValueNode) rawNode;
                long instanceId = node.getInstanceId();
                result = new org.aion.avm.shadow.java.lang.String(testingInstance(instanceId));
            } else if (rawNode instanceof ClassNode) {
                ClassNode node = (ClassNode) rawNode;
                result = new org.aion.avm.shadow.java.lang.String(testingClass(node.className));
            } else if (rawNode instanceof ConstantNode) {
                ConstantNode node = (ConstantNode) rawNode;
                result = new org.aion.avm.shadow.java.lang.String(testingConstant(node.constantId));
            } else {
                // This better be null.
                RuntimeAssertionError.assertTrue(null == rawNode);
                result = new org.aion.avm.shadow.java.lang.String(testingNull());
            }
            return result;
        }
        @Override
        public void setBoolean(Field field, org.aion.avm.shadow.java.lang.Object object, boolean val) {
            Assert.fail("Not called in test");
        }
        @Override
        public void setDouble(Field field, org.aion.avm.shadow.java.lang.Object object, double val) {
            Assert.fail("Not called in test");
        }
        @Override
        public void setLong(Field field, org.aion.avm.shadow.java.lang.Object object, long val) {
            Assert.fail("Not called in test");
        }
        @Override
        public void setFloat(Field field, org.aion.avm.shadow.java.lang.Object object, float val) {
            Assert.fail("Not called in test");
        }
        @Override
        public void setInt(Field field, org.aion.avm.shadow.java.lang.Object object, int val) {
            Assert.fail("Not called in test");
        }
        @Override
        public void setChar(Field field, org.aion.avm.shadow.java.lang.Object object, char val) {
            Assert.fail("Not called in test");
        }
        @Override
        public void setShort(Field field, org.aion.avm.shadow.java.lang.Object object, short val) {
            Assert.fail("Not called in test");
        }
        @Override
        public void setByte(Field field, org.aion.avm.shadow.java.lang.Object object, byte val) {
            Assert.fail("Not called in test");
        }
        @Override
        public void setObject(Field field, org.aion.avm.shadow.java.lang.Object object, org.aion.avm.shadow.java.lang.Object val) {
            Assert.fail("Not called in test");
        }
    }
}

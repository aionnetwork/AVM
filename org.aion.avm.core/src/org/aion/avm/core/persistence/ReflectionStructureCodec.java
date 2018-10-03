package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Responsible for the high-level reading/writing of actual object instances.  Internally, uses reflection to determine how to make sense
 * of object data.
 * This class is also responsible for much of the partial graph management:  Creating and tracking stub instances and knowing how to assign
 * instanceIds to objects being serialized.
 * 
 * See issue-127 for more information.
 * 
 * Note that the "automatic" entry-points for both serialization and deserialization are also here (see issue-132 for more details on that).
 * 
 * TODO:  Test the benefit of caching the reflected field access instances for user-defined classes (there is a great deal of re-use).
 */
public class ReflectionStructureCodec implements IDeserializer, SingleInstanceDeserializer.IAutomatic, SingleInstanceSerializer.IAutomatic {
    private static IDeserializer DONE_MARKER = new GraphWalkingMarker();

    // NOTE:  This fieldCache is passed in from outside so we can modify it for later use (it is used for multiple instances of this).
    private final ReflectedFieldCache fieldCache;
    private final IFieldPopulator populator;
    private final IStorageFeeProcessor feeProcessor;
    private final IObjectGraphStore graphStore;
    // We cache the method entry-point we will use to invoke the load operation on any instance.
    private final Method deserializeSelf;
    private final Method serializeSelf;
    // We only hold the deserializerField because we need to check if it is null when traversing the graph for objects to serialize.
    private final Field deserializerField;
    private final Field persistenceTokenField;

    public ReflectionStructureCodec(ReflectedFieldCache fieldCache, IFieldPopulator populator, IStorageFeeProcessor feeProcessor, IObjectGraphStore graphStore) {
        this.fieldCache = fieldCache;
        this.populator = populator;
        this.feeProcessor = feeProcessor;
        this.graphStore = graphStore;
        try {
            this.deserializeSelf = org.aion.avm.shadow.java.lang.Object.class.getDeclaredMethod("deserializeSelf", java.lang.Class.class, IObjectDeserializer.class);
            this.serializeSelf = org.aion.avm.shadow.java.lang.Object.class.getDeclaredMethod("serializeSelf", java.lang.Class.class, IObjectSerializer.class);
            this.deserializerField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("deserializer");
            this.deserializerField.setAccessible(true);
            this.persistenceTokenField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("persistenceToken");
            this.persistenceTokenField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public void serializeClass(ExtentBasedCodec.Encoder encoder, Class<?> clazz, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        try {
            // Note that only direct class statics are serialized with the class:  superclass statics are saved in the superclass.
            // Hence, just call the serializer, directly.
            safeSerializeOneClass(encoder, clazz, null, nextObjectQueue);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public void deserializeClass(ExtentBasedCodec.Decoder decoder, Class<?> clazz) {
        // Note that only direct class statics are deserialized with the class:  superclass statics are loaded from the superclass.
        // Hence, just call the deserializer, directly.
        safeDeserializeOneClass(decoder, clazz, null);
    }


    private void safeSerialize(ExtentBasedCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) throws IllegalArgumentException, IllegalAccessException {
        // This method is recursive since we are looking to find the root where we need to begin:
        // -for Objects this is the shadow Object
        // -for interfaces, it is when we hit null (since they have no super-class but the interface may have statics)
        boolean isAtTop = (org.aion.avm.shadow.java.lang.Object.class == clazz)
                || (null == clazz);
        if (isAtTop) {
            // This is the root so we want to terminate here.
            // There are no statics in this class and we have no automatic decoding of any of its instance variables.
        } else if (clazz == firstManualClass) {
            // We CANNOT deserialize this, since it is the first manual class, but our caller can, so pass null as the manual class to them.
            safeSerialize(encoder, clazz.getSuperclass(), object, null, nextObjectQueue);
        } else {
            // Call the superclass and serialize this class.
            safeSerialize(encoder, clazz.getSuperclass(), object, firstManualClass, nextObjectQueue);
            // If we got null as the first manual class, we can automatically deserialize.
            if (null == firstManualClass) {
                safeSerializeOneClass(encoder, clazz, object, nextObjectQueue);
            }
        }
    }

    private void safeSerializeOneClass(ExtentBasedCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) throws IllegalArgumentException, IllegalAccessException {
        // Note that we serialize objects and classes the same way, just looking for instance versus static fields.
        int expectedModifier = (null == object)
                ? Modifier.STATIC
                : 0x0;
        
        for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
            if (expectedModifier == (Modifier.STATIC & field.getModifiers())) {
                Class<?> type = field.getType();
                if (boolean.class == type) {
                    boolean val = field.getBoolean(object);
                    encoder.encodeByte((byte) (val ? 0x1 : 0x0));
                } else if (byte.class == type) {
                    byte val = field.getByte(object);
                    encoder.encodeByte(val);
                } else if (short.class == type) {
                    short val = field.getShort(object);
                    encoder.encodeShort(val);
                } else if (char.class == type) {
                    char val = field.getChar(object);
                    encoder.encodeChar(val);
                } else if (int.class == type) {
                    int val = field.getInt(object);
                    encoder.encodeInt(val);
                } else if (float.class == type) {
                    float actual = field.getFloat(object);
                    int val = Float.floatToIntBits(actual);
                    encoder.encodeInt(val);
                } else if (long.class == type) {
                    long val = field.getLong(object);
                    encoder.encodeLong(val);
                } else if (double.class == type) {
                    double actual = field.getDouble(object);
                    long val = Double.doubleToLongBits(actual);
                    encoder.encodeLong(val);
                } else {
                    org.aion.avm.shadow.java.lang.Object contents = (org.aion.avm.shadow.java.lang.Object)field.get(object);
                    // This should be a shadow object.
                    if (contents != null && !org.aion.avm.shadow.java.lang.Object.class.isAssignableFrom(contents.getClass())) {
                        throw RuntimeAssertionError.unreachable("Attempted to encode non-shadow object: " + contents.getClass());
                    }
                    // Shape:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
                    // Null:  (int)0.
                    deflateInstanceAsStub(encoder, contents, nextObjectQueue);
                }
            }
        } 
    }

    private void deflateInstanceAsStub(ExtentBasedCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object contents, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        try {
            boolean isNormalInstance = SerializedInstanceStub.serializeAsReference(encoder, contents, this.graphStore, this.persistenceTokenField);
            
            if (isNormalInstance) {
                // The helper thinks we should enqueue this instance, since it is a normal instance type.
                // If this instance has been loaded, set it to not loaded and add it to the queue.
                if (null == this.deserializerField.get(contents)) {
                    this.deserializerField.set(contents, DONE_MARKER);
                    nextObjectQueue.accept(contents);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // If there are any problems with this access, we must have resolved it before getting to this point.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    private void safeDeserialize(ExtentBasedCodec.Decoder decoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // This method is recursive since we are looking to find the root where we need to begin:
        // -for Objects this is the shadow Object
        // -for interfaces, it is when we hit null (since they have no super-class but the interface may have statics)
        boolean isAtTop = (org.aion.avm.shadow.java.lang.Object.class == clazz)
                || (null == clazz);
        if (isAtTop) {
            // This is the root so we want to terminate here.
            // There are no statics in this class and we have no automatic decoding of any of its instance variables.
        } else if (clazz == firstManualClass) {
            // We CANNOT deserialize this, since it is the first manual class, but our caller can, so pass null as the manual class to them.
            safeDeserialize(decoder, clazz.getSuperclass(), object, null);
        } else {
            // Call the superclass and serialize this class.
            safeDeserialize(decoder, clazz.getSuperclass(), object, firstManualClass);
            // If we got null as the first manual class, we can automatically deserialize.
            if (null == firstManualClass) {
                safeDeserializeOneClass(decoder, clazz, object);
            }
        }
    }

    private void safeDeserializeOneClass(ExtentBasedCodec.Decoder decoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) {
        // Note that we deserialize objects and classes the same way, just looking for instance versus static fields.
        int expectedModifier = (null == object)
                ? Modifier.STATIC
                : 0x0;
        
        for (Field field : this.fieldCache.getDeclaredFieldsForClass(clazz)) {
            if (expectedModifier == (Modifier.STATIC & field.getModifiers())) {
                Class<?> type = field.getType();
                if (boolean.class == type) {
                    boolean val = (0x1 == decoder.decodeByte());
                    this.populator.setBoolean(field, object, val);
                } else if (byte.class == type) {
                    byte val = decoder.decodeByte();
                    this.populator.setByte(field, object, val);
                } else if (short.class == type) {
                    short val = decoder.decodeShort();
                    this.populator.setShort(field, object, val);
                } else if (char.class == type) {
                    char val = decoder.decodeChar();
                    this.populator.setChar(field, object, val);
                } else if (int.class == type) {
                    int val = decoder.decodeInt();
                    this.populator.setInt(field, object, val);
                } else if (float.class == type) {
                    int val = decoder.decodeInt();
                    float actual = Float.intBitsToFloat(val);
                    this.populator.setFloat(field, object, actual);
                } else if (long.class == type) {
                    long val = decoder.decodeLong();
                    this.populator.setLong(field, object, val);
                } else if (double.class == type) {
                    long val = decoder.decodeLong();
                    double actual = Double.longBitsToDouble(val);
                    this.populator.setDouble(field, object, actual);
                } else {
                    // Shape:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
                    // Null:  (int)0.
                    org.aion.avm.shadow.java.lang.Object instanceToStore = inflateStubAsInstance(decoder.decodeReference());
                    this.populator.setObject(field, object, instanceToStore);
                }
            }
        } 
    }

    private org.aion.avm.shadow.java.lang.Object inflateStubAsInstance(INode oneNode) {
        return SerializedInstanceStub.deserializeReferenceAsInstance(oneNode, this.populator);
    }

    @Override
    public void startDeserializeInstance(org.aion.avm.shadow.java.lang.Object instance, IPersistenceToken persistenceToken) {
        // The persistenceToken cannot be null since that implies we are deserializing a new object.
        RuntimeAssertionError.assertTrue(null != persistenceToken);
        // This MUST be a NodeToken.
        IRegularNode node = ((NodePersistenceToken)persistenceToken).node;
        
        // This is called from the shadow Object "lazyLoad()".  We just want to load the data for this instance and then create the deserializer to pass back to them.
        Extent extent = node.loadRegularData();
        this.feeProcessor.readOneInstanceFromStorage(extent.getBillableSize());
        deserializeInstance(instance, extent);
    }

    public void serializeInstance(org.aion.avm.shadow.java.lang.Object instance, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectSink) {
        try {
            IPersistenceToken persistenceToken = (IPersistenceToken)this.persistenceTokenField.get(instance);
            // Note that, even if this is a new instance, someone would have already assigned a persistence token so this can't be null.
            RuntimeAssertionError.assertTrue(null != persistenceToken);
            
            Extent extent = internalSerializeInstance(instance, nextObjectSink);
            // NOTE:  Writing to storage, inline with the fee calculation, assumes that it is possible to rollback changes to the storage if
            // we run out of energy, part-way.
            this.feeProcessor.writeOneInstanceToStorage(extent.getBillableSize());
            ((NodePersistenceToken)persistenceToken).node.saveRegularData(extent);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // If there are any problems with this access, we must have resolved it before getting to this point.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    // Note that this is only public so tests can use it.
    public Extent internalSerializeInstance(org.aion.avm.shadow.java.lang.Object instance, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectSink) {
        ExtentBasedCodec.Encoder encoder = new ExtentBasedCodec.Encoder();
        SingleInstanceSerializer singleSerializer = new SingleInstanceSerializer(this, encoder, nextObjectSink);
        try {
            this.serializeSelf.invoke(instance, null, singleSerializer);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // If there are any problems with this access, we must have resolved it before getting to this point.
            throw RuntimeAssertionError.unexpected(e);
        }
        return encoder.toExtent();
    }

    // Note that this is only public so tests can use it.
    public void deserializeInstance(org.aion.avm.shadow.java.lang.Object instance, Extent extent) {
        // To see it referenced here, we must have saved this data, in the past.
        RuntimeAssertionError.assertTrue(null != extent);
        
        ExtentBasedCodec.Decoder decoder = new ExtentBasedCodec.Decoder(extent);
        SingleInstanceDeserializer singleDeserializer = new SingleInstanceDeserializer(this, decoder);
        try {
            this.deserializeSelf.invoke(instance, null, singleDeserializer);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // If there are any problems with this access, we must have resolved it before getting to this point.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void partialAutomaticDeserializeInstance(ExtentBasedCodec.Decoder decoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        try {
            safeDeserialize(decoder, instance.getClass(), instance, firstManualClass);
        } catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void partialAutomaticSerializeInstance(ExtentBasedCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        try {
            safeSerialize(encoder, instance.getClass(), instance, firstManualClass, nextObjectQueue);
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void encodeAsStub(ExtentBasedCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object object, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        deflateInstanceAsStub(encoder, object, nextObjectQueue);
    }

    @Override
    public org.aion.avm.shadow.java.lang.Object decodeStub(INode node) {
        return inflateStubAsInstance(node);
    }


    /**
     * An interface which must be provided when using the ReflectionStructureCodec to deserialize data.
     * The implementation is responsible for building/finding various kinds of instances as well as populating field with actual data.
     * Note that no exceptions are exposed by this interface since any failures should be static, only.
     */
    public static interface IFieldPopulator {
        /**
         * Called to create/find a regular instance of the given className and instanceId.
         * Regular instances are non-null and are neither Class objects, nor explicit constants of the shadow JCL.
         * Failure to create the instance is considered fatal.
         * 
         * @param className The name of the type.
         * @param persistenceToken The token of this specific instance.
         * @return The created or found shadow object instance.
         */
        org.aion.avm.shadow.java.lang.Object instantiateReference(INode node);
        
        void setBoolean(Field field, org.aion.avm.shadow.java.lang.Object object, boolean val);
        void setDouble(Field field, org.aion.avm.shadow.java.lang.Object object, double val);
        void setLong(Field field, org.aion.avm.shadow.java.lang.Object object, long val);
        void setFloat(Field field, org.aion.avm.shadow.java.lang.Object object, float val);
        void setInt(Field field, org.aion.avm.shadow.java.lang.Object object, int val);
        void setChar(Field field, org.aion.avm.shadow.java.lang.Object object, char val);
        void setShort(Field field, org.aion.avm.shadow.java.lang.Object object, short val);
        void setByte(Field field, org.aion.avm.shadow.java.lang.Object object, byte val);
        void setObject(Field field, org.aion.avm.shadow.java.lang.Object object, org.aion.avm.shadow.java.lang.Object val);
    }
}

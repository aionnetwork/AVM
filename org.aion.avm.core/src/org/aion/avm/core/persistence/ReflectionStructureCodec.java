package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

import org.aion.avm.internal.ClassPersistenceToken;
import org.aion.avm.internal.ConstantPersistenceToken;
import org.aion.avm.internal.IDeserializer;
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
public class ReflectionStructureCodec implements SingleInstanceDeserializer.IAutomatic, SingleInstanceSerializer.IAutomatic, ISuspendableInstanceLoader {
    private static IDeserializer DONE_MARKER = new GraphWalkingMarker();

    // NOTE:  This fieldCache is passed in from outside so we can modify it for later use (it is used for multiple instances of this).
    private final ReflectedFieldCache fieldCache;
    private final IFieldPopulator populator;
    private final IStorageFeeProcessor feeProcessor;
    private final IObjectGraphStore graphStore;
    // We only hold the deserializerField because we need to check if it is null when traversing the graph for objects to serialize.
    private final Field deserializerField;
    private final Field persistenceTokenField;
    // We scan all the objects we loaded as roots since we don't want to hide changes to them if reachable via other paths (issue-249).
    private final List<org.aion.avm.shadow.java.lang.Object> loadedObjectInstances;

    // ISuspendableInstanceLoader state.
    private boolean isActiveInstanceLoader;

    private final NotLoadedDeserializer initialDeserializer;
    private final PreLoadedDeserializer preLoadedDeserializer;
    private final IdentityHashMap<org.aion.avm.shadow.java.lang.Object, Integer> objectSizesLoadedForCallee;

    // We allow the external LoadedDApp to save the state of pre-call statics in this instance (since it can't have state associated with a single invocation).
    private SerializedRepresentation preCallStaticData;

    public ReflectionStructureCodec(ReflectedFieldCache fieldCache, IFieldPopulator populator, IStorageFeeProcessor feeProcessor, IObjectGraphStore graphStore) {
        this.fieldCache = fieldCache;
        this.populator = populator;
        this.feeProcessor = feeProcessor;
        this.graphStore = graphStore;
        try {
            this.deserializerField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("deserializer");
            this.deserializerField.setAccessible(true);
            this.persistenceTokenField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("persistenceToken");
            this.persistenceTokenField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
        this.loadedObjectInstances = new LinkedList<>();
        this.isActiveInstanceLoader = true;
        this.initialDeserializer = new NotLoadedDeserializer();
        this.preLoadedDeserializer = new PreLoadedDeserializer();
        this.objectSizesLoadedForCallee = new IdentityHashMap<>();
    }

    public void setPreCallStaticData(SerializedRepresentation preCallStaticData) {
        // This shouldn't be set twice.
        RuntimeAssertionError.assertTrue(null == this.preCallStaticData);
        this.preCallStaticData = preCallStaticData;
    }

    public SerializedRepresentation getPreCallStaticData() {
        // Note that this might be null, if this was a create call and, therefore, had no pre-call statics.
        return this.preCallStaticData;
    }

    public void serializeClass(SerializedRepresentationCodec.Encoder encoder, Class<?> clazz, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        try {
            // Note that only direct class statics are serialized with the class:  superclass statics are saved in the superclass.
            // Hence, just call the serializer, directly.
            safeSerializeOneClass(encoder, clazz, null, nextObjectQueue);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public void deserializeClass(SerializedRepresentationCodec.Decoder decoder, Class<?> clazz) {
        // Note that only direct class statics are deserialized with the class:  superclass statics are loaded from the superclass.
        // Hence, just call the deserializer, directly.
        safeDeserializeOneClass(decoder, clazz, null);
    }


    private void safeSerialize(SerializedRepresentationCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) throws IllegalArgumentException, IllegalAccessException {
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

    private void safeSerializeOneClass(SerializedRepresentationCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) throws IllegalArgumentException, IllegalAccessException {
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

    private void deflateInstanceAsStub(SerializedRepresentationCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object contents, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        try {
            boolean isNormalInstance = serializeAsReference(encoder, contents);
            
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

    private void safeDeserialize(SerializedRepresentationCodec.Decoder decoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
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

    private void safeDeserializeOneClass(SerializedRepresentationCodec.Decoder decoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) {
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
                    org.aion.avm.shadow.java.lang.Object instanceToStore = deserializeReferenceAsInstance(decoder.decodeReference());
                    this.populator.setObject(field, object, instanceToStore);
                }
            }
        } 
    }

    public void serializeInstance(org.aion.avm.shadow.java.lang.Object instance, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectSink) {
        NodePersistenceToken persistenceToken = (NodePersistenceToken) safeExtractPersistenceToken(instance);
        
        // We need to serialize this instance to an extent to either save it out or realize it didn't change.
        SerializedRepresentation extent = internalSerializeInstance(instance, nextObjectSink);
        
        // NOTE:  Writing to storage, inline with the fee calculation, assumes that it is possible to rollback changes to the storage if
        // we run out of energy, part-way.
        // Determine if this is a new instance or an update.
        // TODO:  Verify that we are not seeing a "new instance" which was already billed as new in a callee frame.  If this becomes reachable
        // in this frame, but was billed in the callee frame, we will probably misinterpret it as a new instance, again.
        if (persistenceToken.isNewlyWritten) {
            // If this is new, we always want to write it.
            persistenceToken.node.saveRegularData(extent);
            this.feeProcessor.writeFirstOneInstanceToStorage(extent.getBillableSize());
        } else {
            // The instance already existed so check to see if we actually need to write it back.
            SerializedRepresentation originalExtent = persistenceToken.node.loadOriginalData();
            if (!originalExtent.equals(extent)) {
                persistenceToken.node.saveRegularData(extent);
                this.feeProcessor.writeUpdateOneInstanceToStorage(extent.getBillableSize());
            }
        }
    }

    private int serializeAndWriteBackInstance(org.aion.avm.shadow.java.lang.Object instance, NodePersistenceToken persistenceToken, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectSink) {
        // Note that, even if this is a new instance, someone would have already assigned a persistence token so this can't be null.
        RuntimeAssertionError.assertTrue(null != persistenceToken);
        
        SerializedRepresentation extent = internalSerializeInstance(instance, nextObjectSink);
        // NOTE:  Writing to storage, inline with the fee calculation, assumes that it is possible to rollback changes to the storage if
        // we run out of energy, part-way.
        persistenceToken.node.saveRegularData(extent);
        return extent.getBillableSize();
    }

    // Note that this is only public so tests can use it.
    public SerializedRepresentation internalSerializeInstance(org.aion.avm.shadow.java.lang.Object instance, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectSink) {
        SerializedRepresentationCodec.Encoder encoder = new SerializedRepresentationCodec.Encoder();
        SingleInstanceSerializer singleSerializer = new SingleInstanceSerializer(this, encoder, nextObjectSink);
        instance.serializeSelf(null, singleSerializer);
        return encoder.toSerializedRepresentation();
    }

    // Note that this is only public so tests can use it.
    public void deserializeInstance(org.aion.avm.shadow.java.lang.Object instance, SerializedRepresentation extent) {
        // To see it referenced here, we must have saved this data, in the past.
        RuntimeAssertionError.assertTrue(null != extent);
        
        SerializedRepresentationCodec.Decoder decoder = new SerializedRepresentationCodec.Decoder(extent);
        SingleInstanceDeserializer singleDeserializer = new SingleInstanceDeserializer(this, decoder);
        instance.deserializeSelf(null, singleDeserializer);
    }

    @Override
    public void partialAutomaticDeserializeInstance(SerializedRepresentationCodec.Decoder decoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        try {
            safeDeserialize(decoder, instance.getClass(), instance, firstManualClass);
        } catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void partialAutomaticSerializeInstance(SerializedRepresentationCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        try {
            safeSerialize(encoder, instance.getClass(), instance, firstManualClass, nextObjectQueue);
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void encodeAsStub(SerializedRepresentationCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object object, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        deflateInstanceAsStub(encoder, object, nextObjectQueue);
    }

    @Override
    public org.aion.avm.shadow.java.lang.Object decodeStub(INode node) {
        return deserializeReferenceAsInstance(node);
    }

    @Override
    public void loaderDidBecomeActive() {
        RuntimeAssertionError.assertTrue(!this.isActiveInstanceLoader);
        this.isActiveInstanceLoader = true;
    }

    @Override
    public void loaderDidBecomeInactive() {
        RuntimeAssertionError.assertTrue(this.isActiveInstanceLoader);
        this.isActiveInstanceLoader = false;
    }

    /**
     * Called to walk any additional roots the code knows about, beyond class statics, before the final serialization pass.
     * In our case, we want to walk the objects we loaded, since they might have changed in a way which was hidden by graph changes.
     * 
     * @param instanceSink Where to enqueue any other objects we find while walking the roots.
     */
    public void reserializeAdditionalRoots(Consumer<org.aion.avm.shadow.java.lang.Object> instanceSink) {
        try {
            for (org.aion.avm.shadow.java.lang.Object root : this.loadedObjectInstances) {
                // Everything we loaded here is a real normal instance which was already serialized so just check the IDeserializer to see if it has already been marked.
                if (null == this.deserializerField.get(root)) {
                    this.deserializerField.set(root, DONE_MARKER);
                    instanceSink.accept(root);
                }
            }
            // Clear this so we can assert we are done with it in the finishCommit().
            this.loadedObjectInstances.clear();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any such problem would have been detected much sooner.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public void finishCommit() {
        RuntimeAssertionError.assertTrue(this.loadedObjectInstances.isEmpty());
        Queue<org.aion.avm.shadow.java.lang.Object> instancesToWrite = new LinkedList<>(this.objectSizesLoadedForCallee.keySet());
        this.objectSizesLoadedForCallee.clear();
        Consumer<org.aion.avm.shadow.java.lang.Object> instanceSink = (instance) -> {
            // These should only be new objects but the persistence token is set right before this call so we can't verify it.
            instancesToWrite.add(instance);
        };
        
        // Here, we just need to walk the remaining objects we loaded for a callee but never touched, ourselves:  we still need to write them back.
        while (!instancesToWrite.isEmpty()) {
            org.aion.avm.shadow.java.lang.Object toWrite = instancesToWrite.poll();
            NodePersistenceToken persistenceToken = (NodePersistenceToken) safeExtractPersistenceToken(toWrite);
            // We don't need the size that this returns since these write-backs are free (another invoke already paid for them).
            // TODO:  This instanceSink should probably be null since we shouldn't discover new objects at this point.  However, we currently need
            // this in order to find new instances which may have been first created in callee frames.  If we can solve that problem more directly,
            // this instanceSink can probably be removed.
            serializeAndWriteBackInstance(toWrite, persistenceToken, instanceSink);
        }
    }

    public IDeserializer getInitialLoadDeserializer() {
        return this.initialDeserializer;
    }

    /**
     * Serializes a reference to the given object into the given encoder.
     * 
     * @param encoder The instance stub will be serialized using this encoder.
     * @param target The object reference to encode.
     * @return True if the instance was the type of object which should, itself, be serialized.
     */
    private boolean serializeAsReference(SerializedRepresentationCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object target) {
        INode referenceToTarget = null;
        boolean isRegularReference = false;
        
        if (null == target) {
            // Just leave the null.
            referenceToTarget = null;
        } else {
            // Request the token.
            IPersistenceToken persistenceToken = safeExtractPersistenceToken(target);
            // Check:
            // -constant
            // -class
            // -regular or null
            // -null
            if (persistenceToken instanceof ConstantPersistenceToken) {
                referenceToTarget = this.graphStore.buildConstantNode(((ConstantPersistenceToken)persistenceToken).identityHashCode);
            } else if (persistenceToken instanceof ClassPersistenceToken) {
                referenceToTarget = this.graphStore.buildClassNode(((ClassPersistenceToken)persistenceToken).className);
            } else if (persistenceToken instanceof NodePersistenceToken) {
                referenceToTarget = ((NodePersistenceToken)persistenceToken).node;
                isRegularReference = true;
            } else if (null == persistenceToken) {
                // Note that we used to have an assumption that these were lazily assigned to classes so verify we aren't in that case.
                RuntimeAssertionError.assertTrue(!(target instanceof org.aion.avm.shadow.java.lang.Class));
                
                // Create the node and set it.
                IRegularNode regularNode = this.graphStore.buildNewRegularNode(target.getClass().getName());
                try {
                    // This token was null, meaning a new instance, so this is a newly written token.
                    boolean isNewlyWritten = true;
                    this.persistenceTokenField.set(target, new NodePersistenceToken(regularNode, isNewlyWritten));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // Any failure related to this would have happened much earlier.
                    throw RuntimeAssertionError.unexpected(e);
                }
                referenceToTarget = regularNode;
                isRegularReference = true;
            } else {
                RuntimeAssertionError.unreachable("Unknown token type");
            }
            RuntimeAssertionError.assertTrue(null != referenceToTarget);
        }
        encoder.encodeReference(referenceToTarget);
        return isRegularReference;
    }

    private org.aion.avm.shadow.java.lang.Object deserializeReferenceAsInstance(INode oneNode) {
        return this.populator.instantiateReference(oneNode);
    }

    private IPersistenceToken safeExtractPersistenceToken(org.aion.avm.shadow.java.lang.Object instance) {
        try {
            return (IPersistenceToken)this.persistenceTokenField.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any failure related to this would have happened much earlier.
            throw RuntimeAssertionError.unexpected(e);
        }
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


    /**
     * The deserializer installed in a stub which has only been referenced, not yet loaded.
     * Note that this isn't static since it still depends on the state/capabilities of the outer class instance.
     */
    private class NotLoadedDeserializer implements IDeserializer {
        @Override
        public void startDeserializeInstance(org.aion.avm.shadow.java.lang.Object instance, IPersistenceToken persistenceToken) {
            // The persistenceToken cannot be null since that implies we are deserializing a new object.
            RuntimeAssertionError.assertTrue(null != persistenceToken);
            // This MUST be a NodePersistenceToken.
            IRegularNode node = ((NodePersistenceToken)persistenceToken).node;
            
            // This is called from the shadow Object "lazyLoad()".  We just want to load the data for this instance and then create the deserializer to pass back to them.
            SerializedRepresentation extent = node.loadOriginalData();
            deserializeInstance(instance, extent);
            
            int instanceBytes = extent.getBillableSize();
            // Check to see if we are currently active or if this deserialization was triggered by a callee while we are dormant.
            if (ReflectionStructureCodec.this.isActiveInstanceLoader) {
                // We are on top so we directly loaded this in response to this DApp running.
                
                // This means that we need to bill them for it.
                ReflectionStructureCodec.this.feeProcessor.readOneInstanceFromStorage(instanceBytes);
                
                // Save this instance into our root set to scan for re-save, when done (issue-249: fixes hidden changes being skipped).
                ReflectionStructureCodec.this.loadedObjectInstances.add(instance);
            } else {
                // Someone else is actually loading this and we are a caller of theirs so we have to pretend this didn't happen.
                
                // First, record the billing fee we _would_ have charged them (this map is also used for the "free write-back" set).
                ReflectionStructureCodec.this.objectSizesLoadedForCallee.put(instance, instanceBytes);
                
                // Install our pre-loaded deserializer so we can bill them for this, later.
                try {
                    ReflectionStructureCodec.this.deserializerField.set(instance, ReflectionStructureCodec.this.preLoadedDeserializer);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // Failures here would have happened during startup.
                    throw RuntimeAssertionError.unexpected(e);
                }
            }
        }
    }


    /**
     * The deserializer installed in an object instance which was fully loaded, but not while this instance loader was active.
     * This kind of deserializer is special since it knows how to delay the cost of loading, if not yet referenced, as well as
     * handling the logic of how this should be written-back.
     * Note that this isn't static since it still depends on the state/capabilities of the outer class instance.
     */
    private class PreLoadedDeserializer implements IDeserializer {
        @Override
        public void startDeserializeInstance(org.aion.avm.shadow.java.lang.Object instance, IPersistenceToken persistenceToken) {
            RuntimeAssertionError.assertTrue(persistenceToken instanceof NodePersistenceToken);
            
            // See if we are the active loader.
            if (ReflectionStructureCodec.this.isActiveInstanceLoader) {
                // We are on top so we can now bill them for this load (we can check the billing instance map which we populated during the load).
                int instanceBytes = ReflectionStructureCodec.this.objectSizesLoadedForCallee.remove(instance);
                ReflectionStructureCodec.this.feeProcessor.readOneInstanceFromStorage(instanceBytes);
                
                // This also needs to be moved to the root set to scan for re-save (the issue-249 case).
                // It is no longer required in the billing map.
                ReflectionStructureCodec.this.loadedObjectInstances.add(instance);
            } else {
                // We still aren't active but we already did the load so just re-install the deserializer.
                // NOTE:  This assumes that the derializer is cleared _before_ calling this, not after.
                try {
                    ReflectionStructureCodec.this.deserializerField.set(instance, this);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    // Failures here would have happened during startup.
                    throw RuntimeAssertionError.unexpected(e);
                }
            }
        }
    }
}

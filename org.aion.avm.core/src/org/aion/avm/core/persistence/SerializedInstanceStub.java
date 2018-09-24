package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.aion.avm.core.persistence.ReflectionStructureCodec.IFieldPopulator;
import org.aion.avm.core.persistence.graph.InstanceIdToken;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * A class responsible for the somewhat specialized "instance stub" serialization/deserialization.
 * This is how a field (static or instance) which refers to a reference type is encoded in the storage system.
 */
public class SerializedInstanceStub {
    // There are no constants for stub descriptors greater than 0 since that is a string length field.
    private static final int STUB_DESCRIPTOR_NULL = 0;
    private static final int STUB_DESCRIPTOR_CONSTANT = -1;
    private static final int STUB_DESCRIPTOR_CLASS = -2;

    // Note that this is probably just a temporary measure but, in order to better track bugs in the reentrant case, all instance stubs created
    // in the callee space are given this special instance ID.
    public static final IPersistenceToken REENTRANT_CALLEE_INSTANCE_TOKEN = new IPersistenceToken() {
        @Override
        public boolean isNormalInstance() {
            // This is just a placeholder instance so it should never be called.
            throw RuntimeAssertionError.unreachable("Not a real token");
        }};

    /**
     * Serializes a given object reference as an instance stub.  Note that this helper will apply an instanceId to the instance if it doesn't already have one and is a type which should.
     * 
     * @param encoder The instance stub will be serialized using this encoder.
     * @param instance The object instance to encode.
     * @param persistenceTokenField The reflection Field reference to use when reading or writing an IPersistenceToken into the instance.
     * @param instanceIdProducer Called when a new instanceId is required for the instance.  If this is called, the instanceId should be considered used, by the implementation, and not given out again.
     * @return True if the instance was the type of object which should, itself, be serialized.
     */
    public static boolean serializeInstanceStub(StreamingPrimitiveCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Field persistenceTokenField, Supplier<Long> instanceIdProducer) throws IllegalArgumentException, IllegalAccessException {
        boolean shouldEnqueueInstance = false;
        // See issue-147 for more information regarding this interpretation:
        // - null: (int)0.
        // - -1: (int)-1, (long) instanceId (of constant - negative).
        // - -2: (int)-2, (int) buffer length, (n) UTF-8 class name buffer
        // - >0:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
        // Reason for order of evaluation:
        // - null goes first, since it is easy to detect on either side (and probably a common case).
        // - constants go second since they are arbitrary objects, including some Class objects, and already have the correct instanceId.
        // - Classes go third since we will we don't to look at their instanceIds (we will see the 0 and take the wrong action).
        // - normal references go last (includes those with 0 or >0 instanceIds).
        if (null == instance) {
            // Null has the least data.
            encoder.encodeInt(STUB_DESCRIPTOR_NULL);
        } else {
            // Check the instanceId to see if this is a special-case.
            IPersistenceToken persistenceToken = (IPersistenceToken)persistenceTokenField.get(instance);
            // This serialization call is only used when writing to disk so we can't see REENTRANT_CALLEE_INSTANCE_TOKEN.
            RuntimeAssertionError.assertTrue(REENTRANT_CALLEE_INSTANCE_TOKEN != persistenceToken);
            long instanceId = (null != persistenceToken)
                        ? ((InstanceIdToken)persistenceToken).instanceId
                        : 0L;
            if (instanceId < 0) {
                // Constants.
                encoder.encodeInt(STUB_DESCRIPTOR_CONSTANT);
                // Write the constant instanceId.
                encoder.encodeLong(instanceId);
            } else if (instance instanceof org.aion.avm.shadow.java.lang.Class) {
                // Non-constant Class reference.
                encoder.encodeInt(STUB_DESCRIPTOR_CLASS);
                
                // Get the class name.
                String className = ((org.aion.avm.shadow.java.lang.Class<?>)instance).getRealClass().getName();
                byte[] utf8Name = className.getBytes(StandardCharsets.UTF_8);
                
                // Write the length and the bytes.
                encoder.encodeInt(utf8Name.length);
                encoder.encodeBytes(utf8Name);
            } else {
                // Common case of a normal reference (may or may not already have an instanceId assigned.
                // This a normal reference (although might need an instanceId assigned).
                String typeName = instance.getClass().getName();
                byte[] utf8Name = typeName.getBytes(StandardCharsets.UTF_8);
                if (0 == instanceId) {
                    // We have to assign this.
                    instanceId = instanceIdProducer.get();
                    persistenceToken = new InstanceIdToken(instanceId);
                    persistenceTokenField.set(instance, persistenceToken);
                }
                
                // Now, serialize the standard form.
                encoder.encodeInt(utf8Name.length);
                encoder.encodeBytes(utf8Name);
                encoder.encodeLong(instanceId);
                
                // The common case implies that we should try to serialize the instance, itself.
                shouldEnqueueInstance = true;
            }
        }
        return shouldEnqueueInstance;
    }

    /**
     * Creates a new instance stub instance (that is, an object which is in memory but it fields aren't yet loaded) based on the data available in decoder.
     * 
     * @param decoder Contains the primitive data describing the instance stub to instantiate.
     * @param populator Used to create the actual object instance, itself.
     * @return The object instance (built by populator).
     */
    public static org.aion.avm.shadow.java.lang.Object deserializeInstanceStub(StreamingPrimitiveCodec.Decoder decoder, IFieldPopulator populator) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        // See issue-147 for more information regarding this interpretation:
        // - null: (int)0.
        // - -1: (int)-1, (long) instanceId (of constant - negative).
        // - -2: (int)-2, (int) buffer length, (n) UTF-8 class name buffer
        // - >0:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
        // Reason for order of evaluation:
        // - null goes first, since it is easy to detect on either side (and probably a common case).
        // - constants go second since they are arbitrary objects, including some Class objects, and already have the correct instanceId.
        // - Classes go third since we will we don't to look at their instanceIds (we will see the 0 and take the wrong action).
        // - normal references go last (includes those with 0 or >0 instanceIds).
        org.aion.avm.shadow.java.lang.Object instanceToStore = null;
        int stubDescriptor = decoder.decodeInt();
        if (STUB_DESCRIPTOR_NULL == stubDescriptor) {
            // This is a null object:
            // -nothing else to read.
            instanceToStore = populator.createNull();
        } else if (STUB_DESCRIPTOR_CONSTANT == stubDescriptor) {
            // This is a constant reference:
            // -load the constant instance ID.
            long instanceId = decoder.decodeLong();
            // Constants have negative instance IDs.
            RuntimeAssertionError.assertTrue(instanceId < 0);
            
            IPersistenceToken persistenceToken = new InstanceIdToken(instanceId);
            instanceToStore = populator.createConstant(persistenceToken);
            // We can't fail to find these.
            RuntimeAssertionError.assertTrue(null != instanceToStore);
        } else if (STUB_DESCRIPTOR_CLASS == stubDescriptor) {
            // This is a reference to a Class.
            // -load the size of the class name.
            int classNameLength = decoder.decodeInt();
            // -load the bytes as a string.
            byte[] utf8Name = new byte[classNameLength];
            decoder.decodeBytesInto(utf8Name);
            String className = new String(utf8Name, StandardCharsets.UTF_8);
            
            // Create an instance of the Class (we rely on the helper since this needs to be interned, per-contract).
            instanceToStore = populator.createClass(className);
        } else {
            // This is a normal object:
            // -descriptor is type name length.
            int typeNameLength = stubDescriptor;
            // -load that many bytes as the name
            byte[] utf8Name = new byte[typeNameLength];
            decoder.decodeBytesInto(utf8Name);
            String className = new String(utf8Name, StandardCharsets.UTF_8);
            // -load the instanceId.
            long instanceId = decoder.decodeLong();
            
            // This instance might already exist so ask our helper which will instantiate, if need be.
            IPersistenceToken persistenceToken = new InstanceIdToken(instanceId);
            instanceToStore = populator.createRegularInstance(className, persistenceToken);
        }
        return instanceToStore;
    }

    /**
     * Determines the size of a serialized reference to the given instance.
     * 
     * @param instance The object instance to measure.
     * @param persistenceTokenField The reflection Field reference to use when reading or writing an IPersistenceToken into the instance.
     * @return The serialized size fo the reference to this instance.
     */
    public static int sizeOfInstanceStub(org.aion.avm.shadow.java.lang.Object instance, Field persistenceTokenField) throws IllegalArgumentException, IllegalAccessException {
        int sizeInBytes = 0;
        // See issue-147 for more information regarding this interpretation:
        // - null: (int)0.
        // - -1: (int)-1, (long) instanceId (of constant - negative).
        // - -2: (int)-2, (int) buffer length, (n) UTF-8 class name buffer
        // - >0:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
        // Reason for order of evaluation:
        // - null goes first, since it is easy to detect on either side (and probably a common case).
        // - constants go second since they are arbitrary objects, including some Class objects, and already have the correct instanceId.
        // - Classes go third since we will we don't to look at their instanceIds (we will see the 0 and take the wrong action).
        // - normal references go last (includes those with 0 or >0 instanceIds).
        if (null == instance) {
            // Just encoding the null stub constant as an int.
            sizeInBytes += ByteSizes.INT;
        } else {
            // Check the instanceId to see if this is a special-case.
            IPersistenceToken persistenceToken = (IPersistenceToken)persistenceTokenField.get(instance);
            /*
             * NOTE:  This method is called both to capture the size of statics/objects being read _from_ the caller space and also to capture the
             * size of statics/objects being written back _to_ the caller space, from the callee space.
             * 
             * In the caller->callee (read from) case, we see normal instance references, nulls, constants, and classes.
             * In the callee->caller (write to) case, we see reentrant instant references, nulls, constants, and classes.
             * 
             * Note the distinction:  the first case NEVER sees REENTRANT_CALLEE_INSTANCE_TOKEN while the second ONLY sees it.
             * 
             * Note that we don't see any instances of REENTRANT_CALLEE_INSTANCE_TOKEN, once we get going (since all of these are the callee copies,
             * which are not part of caller space and are not being moved there) but we do see them during sizing.  These all correspond to normal
             * instances (not constants or classes), so we size them accordingly.
             */
            // We need to check constants, first, since they can otherwise be confused for these other cases (unfortunately, we do a type check to
            // avoid the null or REENTRANT_CALLEE_INSTANCE_TOKEN cases).
            boolean isConstant = ((persistenceToken instanceof InstanceIdToken) && (((InstanceIdToken)persistenceToken).instanceId < 0L));
            
            if (isConstant) {
                // Constants.
                // Encode the constant stub constant as an int.
                sizeInBytes += ByteSizes.INT;
                // Then encode the instanceId as a long.
                sizeInBytes += ByteSizes.LONG;
            } else if (instance instanceof org.aion.avm.shadow.java.lang.Class) {
                // Non-constant Class reference.
                // Encode the class stub constant as an int.
                sizeInBytes += ByteSizes.INT;
                
                // Get the class name.
                String className = ((org.aion.avm.shadow.java.lang.Class<?>)instance).getRealClass().getName();
                byte[] utf8Name = className.getBytes(StandardCharsets.UTF_8);
                
                // Write the length and the bytes.
                sizeInBytes += ByteSizes.INT + utf8Name.length;
            } else {
                // This a normal reference so get the type.
                String typeName = instance.getClass().getName();
                byte[] utf8Name = typeName.getBytes(StandardCharsets.UTF_8);
                
                // Serialize as the type name length, byte, and the the instanceId.
                sizeInBytes += ByteSizes.INT + utf8Name.length + ByteSizes.LONG;
            }
        }
        return sizeInBytes;
    }

    /**
     * Used to determine if reentrant (memory-memory) calls are supposed to create an instance stub of the given object or if both spaces can
     * refer to it, directly.
     * This is important for cases like "Class" or constants where there is no real notion of how to make a "duplicate copy".
     * 
     * @param instance The object instance to check.
     * @param persistenceTokenField The reflected access to persistenceToken.
     * @return True if a copy should be made, false if the instance should be shared.
     */
    public static boolean objectUsesReentrantCopy(org.aion.avm.shadow.java.lang.Object instance, Field persistenceTokenField) {
        boolean shouldCopy = false;
        if (null == instance) {
            // Copy null doesn't make sense.
            shouldCopy = false;
        } else if (instance instanceof org.aion.avm.shadow.java.lang.Class) {
            // Classes don't get copied.
            shouldCopy = false;
        } else {
            // Check if this has a constant instance ID.
            try {
                IPersistenceToken persistenceToken = (IPersistenceToken)persistenceTokenField.get(instance);
                // Copy new instances (null) and normal instances (NOT constants or classes).
                // We want the stubs is in the reentrant space to be copied, obviously.
                shouldCopy = (null == persistenceToken)
                        || (REENTRANT_CALLEE_INSTANCE_TOKEN == persistenceToken)
                        || persistenceToken.isNormalInstance();
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // Any failure related to this would have happened much earlier.
                RuntimeAssertionError.unexpected(e);
            }
        }
        return shouldCopy;
    }
}

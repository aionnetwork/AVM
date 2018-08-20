package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.aion.avm.core.persistence.ReflectionStructureCodec.IFieldPopulator;
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

    /**
     * Serializes a given object reference as an instance stub.  Note that this helper will apply an instanceId to the instance if it doesn't already have one and is a type which should.
     * 
     * @param encoder The instance stub will be serialized using this encoder.
     * @param instance The object instance to encode.
     * @param instanceIdField The reflection Field reference to use when reading or writing an instanceId into the instance.
     * @param instanceIdProducer Called when a new instanceId is required for the instance.  If this is called, the instanceId should be considered used, by the implementation, and not given out again.
     * @return True if the instance was the type of object which should, itself, be serialized.
     */
    public static boolean serializeInstanceStub(StreamingPrimitiveCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Field instanceIdField, Supplier<Long> instanceIdProducer) throws IllegalArgumentException, IllegalAccessException {
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
            long instanceId = instanceIdField.getLong(instance);
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
                    instanceIdField.setLong(instance, instanceId);
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
            
            instanceToStore = populator.createConstant(instanceId);
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
            instanceToStore = populator.createRegularInstance(className, instanceId);
        }
        return instanceToStore;
    }

    /**
     * Determines the size of a serialized reference to the given instance.
     * 
     * @param instance The object instance to measure.
     * @param instanceIdField The reflection Field reference to use when reading the instanceId from the instance.
     * @return The serialized size fo the reference to this instance.
     */
    public static int sizeOfInstanceStub(org.aion.avm.shadow.java.lang.Object instance, Field instanceIdField) throws IllegalArgumentException, IllegalAccessException {
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
            sizeInBytes += StreamingPrimitiveCodec.ByteSizes.INT;
        } else {
            // Check the instanceId to see if this is a special-case.
            long instanceId = instanceIdField.getLong(instance);
            if (instanceId < 0) {
                // Constants.
                // Encode the constant stub constant as an int.
                sizeInBytes += StreamingPrimitiveCodec.ByteSizes.INT;
                // Then encode the instanceId as a long.
                sizeInBytes += StreamingPrimitiveCodec.ByteSizes.LONG;
            } else if (instance instanceof org.aion.avm.shadow.java.lang.Class) {
                // Non-constant Class reference.
                // Encode the class stub constant as an int.
                sizeInBytes += StreamingPrimitiveCodec.ByteSizes.INT;
                
                // Get the class name.
                String className = ((org.aion.avm.shadow.java.lang.Class<?>)instance).getRealClass().getName();
                byte[] utf8Name = className.getBytes(StandardCharsets.UTF_8);
                
                // Write the length and the bytes.
                sizeInBytes += StreamingPrimitiveCodec.ByteSizes.INT + utf8Name.length;
            } else {
                // This a normal reference so get the type.
                String typeName = instance.getClass().getName();
                byte[] utf8Name = typeName.getBytes(StandardCharsets.UTF_8);
                
                // Serialize as the type name length, byte, and the the instanceId.
                sizeInBytes += StreamingPrimitiveCodec.ByteSizes.INT + utf8Name.length + StreamingPrimitiveCodec.ByteSizes.LONG;
            }
        }
        return sizeInBytes;
    }
}

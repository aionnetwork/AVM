package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.aion.avm.core.persistence.ReflectionStructureCodec.IFieldPopulator;
import org.aion.avm.core.persistence.graph.InstanceIdToken;
import org.aion.avm.core.persistence.keyvalue.KeyValueNode;
import org.aion.avm.internal.ClassPersistenceToken;
import org.aion.avm.internal.ConstantPersistenceToken;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * A class responsible for the somewhat specialized "instance stub" serialization/deserialization.
 * This is how a field (static or instance) which refers to a reference type is encoded in the storage system.
 */
public class SerializedInstanceStub {
    // Note that this is probably just a temporary measure but, in order to better track bugs in the reentrant case, all instance stubs created
    // in the callee space are given this special instance ID.
    public static final IPersistenceToken REENTRANT_CALLEE_INSTANCE_TOKEN = new IPersistenceToken() {
    };

    /**
     * Serializes a given object reference as an instance stub.  Note that this helper will apply an instanceId to the instance if it doesn't already have one and is a type which should.
     * 
     * @param encoder The instance stub will be serialized using this encoder.
     * @param target The object reference to encode.
     * @param persistenceTokenField The reflection Field reference to use when reading or writing an IPersistenceToken into the instance.
     * @param instanceIdProducer Called when a new instanceId is required for the instance.  If this is called, the instanceId should be considered used, by the implementation, and not given out again.
     * @return True if the instance was the type of object which should, itself, be serialized.
     */
    public static boolean serializeInstanceStub(ExtentBasedCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object target, Field persistenceTokenField, Supplier<Long> instanceIdProducer) {
        INode referenceToTarget = null;
        boolean isRegularReference = false;
        
        if (null == target) {
            // Just leave the null.
            referenceToTarget = null;
        } else {
            // Check the instanceId to see if this is a special-case.
            IPersistenceToken persistenceToken = safeExtractPersistenceToken(persistenceTokenField, target);
            // This serialization call is only used when writing to disk so we can't see REENTRANT_CALLEE_INSTANCE_TOKEN.
            RuntimeAssertionError.assertTrue(REENTRANT_CALLEE_INSTANCE_TOKEN != persistenceToken);
            if (persistenceToken instanceof ConstantPersistenceToken) {
                referenceToTarget = new ConstantNode(((ConstantPersistenceToken)persistenceToken).stableConstantId);
            } else if (persistenceToken instanceof ClassPersistenceToken) {
                RuntimeAssertionError.assertTrue(target instanceof org.aion.avm.shadow.java.lang.Class);
                String className = ((org.aion.avm.shadow.java.lang.Class<?>)target).getRealClass().getName();
                referenceToTarget = new ClassNode(className);
            } else {
                // Common case of a normal reference (may or may not already have an instanceId assigned.
                // This a normal reference (although might need an instanceId assigned).
                String typeName = target.getClass().getName();
                long instanceId = 0L;
                if (null == persistenceToken) {
                    // We have to assign this.
                    instanceId = instanceIdProducer.get();
                    persistenceToken = new InstanceIdToken(instanceId);
                    try {
                        persistenceTokenField.set(target, persistenceToken);
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        // Any failure related to this would have happened much earlier.
                        throw RuntimeAssertionError.unexpected(e);
                    }
                } else {
                    instanceId = ((InstanceIdToken)persistenceToken).instanceId;
                }
                
                referenceToTarget = new KeyValueNode(typeName, instanceId);
               
                // The common case implies that we should try to serialize the instance, itself.
                isRegularReference = true;
            }
            RuntimeAssertionError.assertTrue(null != referenceToTarget);
        }
        encoder.encodeReference(referenceToTarget);
        return isRegularReference;
    }

    /**
     * Creates a new instance stub instance (that is, an object which is in memory but it fields aren't yet loaded) based on the data available in decoder.
     * 
     * @param decoder Contains the primitive data describing the instance stub to instantiate.
     * @param populator Used to create the actual object instance, itself.
     * @return The object instance (built by populator).
     */
    public static org.aion.avm.shadow.java.lang.Object deserializeInstanceStub(ExtentBasedCodec.Decoder decoder, IFieldPopulator populator) {
        org.aion.avm.shadow.java.lang.Object instanceToStore = null;
        
        INode reference = decoder.decodeReference();
        if (null == reference) {
            instanceToStore = populator.createNull();
        } else if (reference instanceof ConstantNode) {
            long instanceId = ((ConstantNode)reference).constantId;
            IPersistenceToken persistenceToken = new InstanceIdToken(instanceId);
            instanceToStore = populator.createConstant(persistenceToken);
            // We can't fail to find these.
            RuntimeAssertionError.assertTrue(null != instanceToStore);
        } else if (reference instanceof ClassNode) {
            String className = ((ClassNode)reference).className;
            instanceToStore = populator.createClass(className);
        } else if (reference instanceof KeyValueNode) {
            KeyValueNode node = (KeyValueNode)reference;
            IPersistenceToken persistenceToken = new InstanceIdToken(node.getInstanceId());
            instanceToStore = populator.createRegularInstance(node.getInstanceClassName(), persistenceToken);
        } else {
            RuntimeAssertionError.unreachable("Unknown node type");
        }
        return instanceToStore;
    }

    /**
     * Determines the size of a serialized reference to the given instance.
     * 
     * @param instance The object instance to measure.
     * @param persistenceTokenField The reflection Field reference to use when reading or writing an IPersistenceToken into the instance.
     * @return The serialized size of the reference to this instance.
     */
    public static int sizeOfInstanceStub(org.aion.avm.shadow.java.lang.Object instance, Field persistenceTokenField) {
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
            IPersistenceToken persistenceToken = safeExtractPersistenceToken(persistenceTokenField, instance);
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
            
            if (persistenceToken instanceof ConstantPersistenceToken) {
                // Constants.
                // Encode the constant stub constant as an int.
                sizeInBytes += ByteSizes.INT;
                // Then encode the instanceId as a long.
                sizeInBytes += ByteSizes.LONG;
            } else if (persistenceToken instanceof ClassPersistenceToken) {
                // Non-constant Class reference.
                RuntimeAssertionError.assertTrue(instance instanceof org.aion.avm.shadow.java.lang.Class);
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
        } else {
            IPersistenceToken persistenceToken = safeExtractPersistenceToken(persistenceTokenField, instance);
            if (persistenceToken instanceof ClassPersistenceToken) {
                // Classes don't get copied.
                shouldCopy = false;
            } else if (persistenceToken instanceof ConstantPersistenceToken) {
                // Constants don't get copied.
                shouldCopy = false;
            } else {
                // This last case is for all the cases which we _should_ copy:
                // -null (new object, not yet assigned - normal instance)
                // -reentrant special token (this was copied, from a parent call, so we should also copy it)
                // -is a normal instanceId token
                // Note that we don't expect any other cases here so we can just assert these and return true.
                RuntimeAssertionError.assertTrue((null == persistenceToken)
                        || (REENTRANT_CALLEE_INSTANCE_TOKEN == persistenceToken)
                        || (persistenceToken instanceof InstanceIdToken)
                );
                shouldCopy = true;
            }
        }
        return shouldCopy;
    }

    private static IPersistenceToken safeExtractPersistenceToken(Field persistenceTokenField, org.aion.avm.shadow.java.lang.Object instance) {
        try {
            return (IPersistenceToken)persistenceTokenField.get(instance);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any failure related to this would have happened much earlier.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
}

package org.aion.avm.core.persistence;

import java.lang.reflect.Field;

import org.aion.avm.core.persistence.ReflectionStructureCodec.IFieldPopulator;
import org.aion.avm.internal.ClassPersistenceToken;
import org.aion.avm.internal.ConstantPersistenceToken;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * A class responsible for the somewhat specialized "instance stub" serialization/deserialization.
 * This is how a field (static or instance) which refers to a reference type is encoded in the storage system.
 */
public class SerializedInstanceStub {
    /**
     * Serializes a reference to the given object into the given encoder.
     * 
     * @param encoder The instance stub will be serialized using this encoder.
     * @param target The object reference to encode.
     * @param graphStore The object graph, used to create the appropriate abstract node type for the reference.
     * @param persistenceTokenField The reflection Field reference to use when reading or writing an IPersistenceToken into the instance.
     * @return True if the instance was the type of object which should, itself, be serialized.
     */
    public static boolean serializeAsReference(ExtentBasedCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object target, IObjectGraphStore graphStore, Field persistenceTokenField) {
        INode referenceToTarget = null;
        boolean isRegularReference = false;
        
        if (null == target) {
            // Just leave the null.
            referenceToTarget = null;
        } else {
            // Request the token.
            IPersistenceToken persistenceToken = safeExtractPersistenceToken(persistenceTokenField, target);
            // Check:
            // -constant
            // -class
            // -regular or null
            // -null
            if (persistenceToken instanceof ConstantPersistenceToken) {
                referenceToTarget = graphStore.buildConstantNode(((ConstantPersistenceToken)persistenceToken).stableConstantId);
            } else if (persistenceToken instanceof ClassPersistenceToken) {
                referenceToTarget = graphStore.buildClassNode(((ClassPersistenceToken)persistenceToken).className);
            } else if (persistenceToken instanceof NodePersistenceToken) {
                referenceToTarget = ((NodePersistenceToken)persistenceToken).node;
                isRegularReference = true;
            } else if (null == persistenceToken) {
                // Note that we used to have an assumption that these were lazily assigned to classes so verify we aren't in that case.
                RuntimeAssertionError.assertTrue(!(target instanceof org.aion.avm.shadow.java.lang.Class));
                
                // Create the node and set it.
                IRegularNode regularNode = graphStore.buildNewRegularNode(target.getClass().getName());
                try {
                    persistenceTokenField.set(target, new NodePersistenceToken(regularNode));
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

    public static org.aion.avm.shadow.java.lang.Object deserializeReferenceAsInstance(INode oneNode, IFieldPopulator populator) {
        return populator.instantiateReference(oneNode);
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
                // -reentrant caller reference token (this was copied, from a parent call, so we should also copy it)
                // -is a normal instanceId token
                // Note that we don't expect any other cases here so we can just assert these and return true.
                RuntimeAssertionError.assertTrue((null == persistenceToken)
                        || (persistenceToken instanceof ReentrantCallerReferenceToken)
                        || (persistenceToken instanceof NodePersistenceToken)
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

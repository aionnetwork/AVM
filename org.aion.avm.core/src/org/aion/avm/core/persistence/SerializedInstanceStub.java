package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

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
    // Note that this is probably just a temporary measure but, in order to better track bugs in the reentrant case, all instance stubs created
    // in the callee space are given this special instance ID.
    public static final IPersistenceToken REENTRANT_CALLEE_INSTANCE_TOKEN = new IPersistenceToken() {
    };

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
            sizeInBytes = ByteSizes.INT;
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
            // TODO:  Sizing data should be moved out.  This logic is duplicated from the INode hierarchy (where it also doesn't really belong).
            if (persistenceToken instanceof ConstantPersistenceToken) {
                // Encode the constant stub constant as an int.
                int constantSize = ByteSizes.INT;
                // Then encode the instanceId as a long.
                int instanceIdSize = ByteSizes.LONG;
                sizeInBytes = constantSize + instanceIdSize;
            } else if (persistenceToken instanceof ClassPersistenceToken) {
                // Non-constant Class reference.
                RuntimeAssertionError.assertTrue(instance instanceof org.aion.avm.shadow.java.lang.Class);
                // Encode the class stub constant as an int.
                int constantSize = ByteSizes.INT;
                
                // Get the class name.
                byte[] utf8Name = ((ClassPersistenceToken)persistenceToken).className.getBytes(StandardCharsets.UTF_8);
                
                // Write the length and the bytes.
                int nameSize = ByteSizes.INT + utf8Name.length;
                sizeInBytes = constantSize + nameSize;
            } else if ((null == persistenceToken) || (REENTRANT_CALLEE_INSTANCE_TOKEN == persistenceToken) || (persistenceToken instanceof NodePersistenceToken)) {
                byte[] utf8Name = instance.getClass().getName().getBytes(StandardCharsets.UTF_8);
                
                // Serialize as the type name length, byte, and the the instanceId.
                sizeInBytes = ByteSizes.INT + utf8Name.length + ByteSizes.LONG;
            } else {
                RuntimeAssertionError.unreachable("Unknown token type during sizing");
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

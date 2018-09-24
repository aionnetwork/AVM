package org.aion.avm.core.persistence.keyvalue;

import java.nio.charset.StandardCharsets;

import org.aion.avm.core.persistence.ClassNode;
import org.aion.avm.core.persistence.ConstantNode;
import org.aion.avm.core.persistence.Extent;
import org.aion.avm.core.persistence.INode;
import org.aion.avm.core.persistence.StreamingPrimitiveCodec;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Serialized extent structure:
 * 1)  number of references
 * 2)  references (encoded as InstanceStubs)
 * 3)  number of bytes of primitive data
 * 4)  primitive data bytes
 * 
 * This is specific to the key-value implementation.
 */
public class KeyValueExtentCodec {
    // Used to fix the billing size calculation:  in the future, we will derive this from the logical content of the Extent but, for now, callers
    // just need a way to remove the overhead introduced by this codec (2 "size" ints).
    public static final int OVERHEAD_BYTES = 8;

    // There are no constants for stub descriptors greater than 0 since that is a string length field.
    private static final int STUB_DESCRIPTOR_NULL = 0;
    private static final int STUB_DESCRIPTOR_CONSTANT = -1;
    private static final int STUB_DESCRIPTOR_CLASS = -2;

    public static byte[] encode(Extent extent) {
        StreamingPrimitiveCodec.Encoder encoder = new StreamingPrimitiveCodec.Encoder();
        
        // We put the references first since the storage system might be more interested in them, since they make up the graph.
        encoder.encodeInt(extent.references.length);
        for (INode node : extent.references) {
            // Check type and encode meaning.
            
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
            if (null == node) {
                // Null has the least data - descriptor only.
                encoder.encodeInt(STUB_DESCRIPTOR_NULL);
            } else {
                // Check the type of this INode to see if it was created as a constant, class, or regular object.
                if (node instanceof ConstantNode) {
                    // Write the descriptor.
                    encoder.encodeInt(STUB_DESCRIPTOR_CONSTANT);
                    // Write the constant index.
                    encoder.encodeLong(((ConstantNode)node).constantId);
                } else if (node instanceof ClassNode) {
                    // Write the descriptor.
                    encoder.encodeInt(STUB_DESCRIPTOR_CLASS);
                    // Write the class name.
                    byte[] utf8Name = ((ClassNode)node).className.getBytes(StandardCharsets.UTF_8);
                    encoder.encodeInt(utf8Name.length);
                    encoder.encodeBytes(utf8Name);
                } else {
                    RuntimeAssertionError.assertTrue(node instanceof KeyValueNode);
                    
                    // Now, the common case (note that this class name length overlaps with the descriptor, ensuring it is always positive).
                    KeyValueNode regularNode = (KeyValueNode)node;
                    byte[] utf8Name = regularNode.getInstanceClassName().getBytes(StandardCharsets.UTF_8);
                    encoder.encodeInt(utf8Name.length);
                    encoder.encodeBytes(utf8Name);
                    encoder.encodeLong(regularNode.getInstanceId());
                }
            }
        }
        
        encoder.encodeInt(extent.data.length);
        encoder.encodeBytes(extent.data);
        
        return encoder.toBytes();
    }

    public static Extent decode(byte[] data) {
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(data);
        
        int referenceCount = decoder.decodeInt();
        INode[] references = new INode[referenceCount];
        for (int i = 0; i < referenceCount; ++i) {
            // Interpret as instance stub.
            int lengthOrDescriptor = decoder.decodeInt();
            switch (lengthOrDescriptor) {
                case STUB_DESCRIPTOR_NULL: {
                    references[i] = null;
                    break;
                }
                case STUB_DESCRIPTOR_CONSTANT: {
                    long constantId = decoder.decodeLong();
                    references[i] = new ConstantNode(constantId);
                    break;
                }
                case STUB_DESCRIPTOR_CLASS: {
                    int length = decoder.decodeInt();
                    byte[] utf8Name = new byte[length];
                    decoder.decodeBytesInto(utf8Name);
                    String className = new String(utf8Name, StandardCharsets.UTF_8);
                    references[i] = new ClassNode(className);
                    break;
                }
                default: {
                    byte[] utf8Name = new byte[lengthOrDescriptor];
                    decoder.decodeBytesInto(utf8Name);
                    String instanceClassName = new String(utf8Name, StandardCharsets.UTF_8);
                    long instanceId = decoder.decodeLong();
                    references[i] = new KeyValueNode(instanceClassName, instanceId);
                    break;
                }
            }
        }
        
        int byteCount = decoder.decodeInt();
        byte[] primitiveData = new byte[byteCount];
        decoder.decodeBytesInto(primitiveData);
        
        return new Extent(primitiveData, references);
    }
}

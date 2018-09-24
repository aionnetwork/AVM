package org.aion.avm.core.persistence;

import java.nio.charset.StandardCharsets;

import org.aion.avm.core.persistence.keyvalue.KeyValueNode;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * Currently just a wrapper around PrimitiveStreamingCodec, as part of a broader plumbing change where we will grow a higher-level API, here,
 * and be less rigidly attached to the serialized form of object references, in the core graph serializer code.
 */
public class ExtentBasedCodec {
    // There are no constants for stub descriptors greater than 0 since that is a string length field.
    private static final int STUB_DESCRIPTOR_NULL = 0;
    private static final int STUB_DESCRIPTOR_CONSTANT = -1;
    private static final int STUB_DESCRIPTOR_CLASS = -2;

    public static class Encoder {
        private final StreamingPrimitiveCodec.Encoder primitives;
        
        public Encoder() {
            this.primitives = new StreamingPrimitiveCodec.Encoder();
        }
        
        public Encoder encodeByte(byte input) {
            this.primitives.encodeByte(input);
            return this;
        }
        
        public Encoder encodeShort(short input) {
            this.primitives.encodeShort(input);
            return this;
        }
        
        public Encoder encodeChar(char input) {
            this.primitives.encodeChar(input);
            return this;
        }
        
        public Encoder encodeInt(int input) {
            this.primitives.encodeInt(input);
            return this;
        }
        
        public Encoder encodeLong(long input) {
            this.primitives.encodeLong(input);
            return this;
        }
        
        public Encoder encodeBytes(byte[] array) {
            this.primitives.encodeBytes(array);
            return this;
        }
        
        public Encoder encodeReference(INode reference) {
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
            if (null == reference) {
                // Null has the least data.
                this.primitives.encodeInt(STUB_DESCRIPTOR_NULL);
            } else if (reference instanceof ConstantNode) {
                // Constants.
                ConstantNode node = (ConstantNode) reference;
                this.primitives.encodeInt(STUB_DESCRIPTOR_CONSTANT);
                // Write the constant instanceId.
                this.primitives.encodeLong(node.constantId);
            } else if (reference instanceof ClassNode) {
                // Non-constant Class reference.
                ClassNode node = (ClassNode) reference;
                this.primitives.encodeInt(STUB_DESCRIPTOR_CLASS);
                
                // Get the class name.
                String className = node.getClassName();
                byte[] utf8Name = className.getBytes(StandardCharsets.UTF_8);
                
                // Write the length and the bytes.
                this.primitives.encodeInt(utf8Name.length);
                this.primitives.encodeBytes(utf8Name);
            } else if (reference instanceof KeyValueNode) {
                // Common case of a normal reference.
                KeyValueNode node = (KeyValueNode) reference;
                String typeName = node.getInstanceClassName();
                byte[] utf8Name = typeName.getBytes(StandardCharsets.UTF_8);
                long instanceId = node.getInstanceId();
                
                // Now, serialize the standard form.
                this.primitives.encodeInt(utf8Name.length);
                this.primitives.encodeBytes(utf8Name);
                this.primitives.encodeLong(instanceId);
            } else {
                RuntimeAssertionError.unreachable("Unknown reference type");
            }
            return this;
        }
        
        public byte[] toBytes() {
            return this.primitives.toBytes();
        }
    }


    public static class Decoder {
        private final StreamingPrimitiveCodec.Decoder primitives;
        
        public Decoder(byte[] data) {
            this.primitives = new StreamingPrimitiveCodec.Decoder(data);
        }
        
        public byte decodeByte() {
            return this.primitives.decodeByte();
        }
        
        public short decodeShort() {
            return this.primitives.decodeShort();
        }
        
        public char decodeChar() {
            return this.primitives.decodeChar();
        }
        
        public int decodeInt() {
            return this.primitives.decodeInt();
        }
        
        public long decodeLong() {
            return this.primitives.decodeLong();
        }
        
        public INode decodeReference() {
            INode node = null;
            int stubDescriptor = this.primitives.decodeInt();
            if (STUB_DESCRIPTOR_NULL == stubDescriptor) {
                // This is a null object:
                // -nothing else to read.
                node = null;
            } else if (STUB_DESCRIPTOR_CONSTANT == stubDescriptor) {
                // This is a constant reference:
                // -load the constant instance ID.
                long instanceId = this.primitives.decodeLong();
                // Constants have negative instance IDs.
                RuntimeAssertionError.assertTrue(instanceId < 0);
                
                node = new ConstantNode(instanceId);
            } else if (STUB_DESCRIPTOR_CLASS == stubDescriptor) {
                // This is a reference to a Class.
                // -load the size of the class name.
                int classNameLength = this.primitives.decodeInt();
                // -load the bytes as a string.
                byte[] utf8Name = new byte[classNameLength];
                this.primitives.decodeBytesInto(utf8Name);
                String className = new String(utf8Name, StandardCharsets.UTF_8);
                
                node = new ClassNode(className);
            } else {
                // This is a normal object:
                // -descriptor is type name length.
                int typeNameLength = stubDescriptor;
                // -load that many bytes as the name
                byte[] utf8Name = new byte[typeNameLength];
                this.primitives.decodeBytesInto(utf8Name);
                String className = new String(utf8Name, StandardCharsets.UTF_8);
                // -load the instanceId.
                long instanceId = this.primitives.decodeLong();
                
                node = new KeyValueNode(className, instanceId);
            }
            return node;
        }
        
        public void decodeBytesInto(byte[] buffer) {
            this.primitives.decodeBytesInto(buffer);
        }
    }
}

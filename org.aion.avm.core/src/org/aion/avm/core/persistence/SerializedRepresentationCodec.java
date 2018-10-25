package org.aion.avm.core.persistence;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains the Encoder/Decoder which can encode/decode SerializedRepresentation objects, respectively.
 * That is, they present a high-level encoding/decoding interface onto a serialized form represented as a SerializedRepresentation.
 */
public class SerializedRepresentationCodec {
    public static class Encoder {
        // We encode primitives with a primitive codec and we build the refs using a list.
        private final StreamingPrimitiveCodec.Encoder primitives;
        private final List<INode> references;
        
        public Encoder() {
            this.primitives = new StreamingPrimitiveCodec.Encoder();
            this.references = new ArrayList<>();
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
            this.references.add(reference);
            return this;
        }
        
        public SerializedRepresentation toSerializedRepresentation() {
            byte[] data = this.primitives.toBytes();
            INode[] refs = this.references.toArray(new INode[this.references.size()]);
            return new SerializedRepresentation(data, refs);
        }
    }


    public static class Decoder {
        // We decode primitives with a primitive code and the refs with just a cursor into the array.
        private final StreamingPrimitiveCodec.Decoder primitives;
        private final INode[] references;
        private int cursor;
        
        public Decoder(SerializedRepresentation extent) {
            this.primitives = new StreamingPrimitiveCodec.Decoder(extent.data);
            this.references = extent.references;
            this.cursor = 0;
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
            INode next = this.references[this.cursor];
            this.cursor += 1;
            return next;
        }
        
        public void decodeBytesInto(byte[] buffer) {
            this.primitives.decodeBytesInto(buffer);
        }
    }
}

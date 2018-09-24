package org.aion.avm.core.persistence;


/**
 * Currently just a wrapper around PrimitiveStreamingCodec, as part of a broader plumbing change where we will grow a higher-level API, here,
 * and be less rigidly attached to the serialized form of object references, in the core graph serializer code.
 */
public class ExtentBasedCodec {
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
        
        public void decodeBytesInto(byte[] buffer) {
            this.primitives.decodeBytesInto(buffer);
        }
    }
}

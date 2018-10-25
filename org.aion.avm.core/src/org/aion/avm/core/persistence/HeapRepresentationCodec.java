package org.aion.avm.core.persistence;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains the Encoder/Decoder which can encode/decode HeapRepresentation objects, respectively.
 * Specifically, the HeapRepresentation is used by the reentrant case to copy references from objects between the spaces.
 * This means that the representations remain very high-level and only some basic account is done on top of that.
 */
public class HeapRepresentationCodec {
    public static class Encoder {
        private final List<Object> primitives;
        private final List<org.aion.avm.shadow.java.lang.Object> references;
        private int billableSizeCounter;
        
        public Encoder() {
            this.primitives = new ArrayList<>();
            this.references = new ArrayList<>();
        }
        
        public Encoder encodeByte(byte input) {
            this.primitives.add(Byte.valueOf(input));
            this.billableSizeCounter += ByteSizes.BYTE;
            return this;
        }
        
        public Encoder encodeShort(short input) {
            this.primitives.add(Short.valueOf(input));
            this.billableSizeCounter += ByteSizes.SHORT;
            return this;
        }
        
        public Encoder encodeChar(char input) {
            this.primitives.add(Character.valueOf(input));
            this.billableSizeCounter += ByteSizes.CHAR;
            return this;
        }
        
        public Encoder encodeInt(int input) {
            this.primitives.add(Integer.valueOf(input));
            this.billableSizeCounter += ByteSizes.INT;
            return this;
        }
        
        public Encoder encodeLong(long input) {
            this.primitives.add(Long.valueOf(input));
            this.billableSizeCounter += ByteSizes.LONG;
            return this;
        }
        
        public Encoder encodeReference(org.aion.avm.shadow.java.lang.Object reference) {
            this.references.add(reference);
            this.billableSizeCounter += ByteSizes.REFERENCE;
            return this;
        }
        
        public HeapRepresentation toHeapRepresentation() {
            Object[] primitiveArray = this.primitives.toArray(new Object[this.primitives.size()]);
            org.aion.avm.shadow.java.lang.Object[] referenceArray = this.references.toArray(new org.aion.avm.shadow.java.lang.Object[this.references.size()]);
            return new HeapRepresentation(primitiveArray, referenceArray, this.billableSizeCounter);
        }
    }


    public static class Decoder {
        private final Object[] primitives;
        private final org.aion.avm.shadow.java.lang.Object[] references;
        private int primitiveCursor;
        private int referenceCursor;
        
        public Decoder(HeapRepresentation extent) {
            this.primitives = extent.primitives;
            this.references = extent.references;
            this.primitiveCursor = 0;
            this.referenceCursor = 0;
        }
        
        public byte decodeByte() {
            byte val = (Byte)this.primitives[this.primitiveCursor];
            this.primitiveCursor += 1;
            return val;
        }
        
        public short decodeShort() {
            short val = (Short)this.primitives[this.primitiveCursor];
            this.primitiveCursor += 1;
            return val;
        }
        
        public char decodeChar() {
            char val = (Character)this.primitives[this.primitiveCursor];
            this.primitiveCursor += 1;
            return val;
        }
        
        public int decodeInt() {
            int val = (Integer)this.primitives[this.primitiveCursor];
            this.primitiveCursor += 1;
            return val;
        }
        
        public long decodeLong() {
            long val = (Long)this.primitives[this.primitiveCursor];
            this.primitiveCursor += 1;
            return val;
        }
        
        public org.aion.avm.shadow.java.lang.Object decodeReference() {
            org.aion.avm.shadow.java.lang.Object val = this.references[this.referenceCursor];
            this.referenceCursor += 1;
            return val;
        }
    }
}

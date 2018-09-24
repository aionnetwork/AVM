package org.aion.avm.core.persistence;

import java.io.ByteArrayOutputStream;


/**
 * Responsible for the low-level reading/writing of primitive data from/into byte streams or arrays.
 * Both the Encoder and Decoder are kept in this class since, while they share no code, they are rigidly symmetric.
 * Note that the encoding follows the Java convention of big endian byte order for multi-byte primitives.
 * 
 * See issue-127 for more information.
 */
public class StreamingPrimitiveCodec {
    public static class Encoder {
        // We want a variable-length output so just use a stream and copy it to bytes at the end.
        private final ByteArrayOutputStream output;
        
        public Encoder() {
            this.output = new ByteArrayOutputStream();
        }
        
        public Encoder encodeByte(byte input) {
            this.output.write(input);
            return this;
        }
        
        public Encoder encodeShort(short input) {
            byte[] encoded = new byte[Short.BYTES];
            encoded[0] = (byte) (0xff & (input >> 8));
            encoded[1] = (byte) (0xff & (input >> 0));
            this.output.write(encoded, 0, encoded.length);
            return this;
        }
        
        public Encoder encodeChar(char input) {
            byte[] encoded = new byte[Character.BYTES];
            encoded[0] = (byte) (0xff & (input >> 8));
            encoded[1] = (byte) (0xff & (input >> 0));
            this.output.write(encoded, 0, encoded.length);
            return this;
        }
        
        public Encoder encodeInt(int input) {
            byte[] encoded = new byte[Integer.BYTES];
            encoded[0] = (byte) (0xff & (input >> 24));
            encoded[1] = (byte) (0xff & (input >> 16));
            encoded[2] = (byte) (0xff & (input >>  8));
            encoded[3] = (byte) (0xff & (input >>  0));
            this.output.write(encoded, 0, encoded.length);
            return this;
        }
        
        public Encoder encodeLong(long input) {
            byte[] encoded = new byte[Long.BYTES];
            encoded[0] = (byte) (0xff & (input >> 56));
            encoded[1] = (byte) (0xff & (input >> 48));
            encoded[2] = (byte) (0xff & (input >> 40));
            encoded[3] = (byte) (0xff & (input >> 32));
            encoded[4] = (byte) (0xff & (input >> 24));
            encoded[5] = (byte) (0xff & (input >> 16));
            encoded[6] = (byte) (0xff & (input >>  8));
            encoded[7] = (byte) (0xff & (input >>  0));
            this.output.write(encoded, 0, encoded.length);
            return this;
        }
        
        public Encoder encodeBytes(byte[] array) {
            this.output.write(array, 0, array.length);
            return this;
        }
        
        public byte[] toBytes() {
            return this.output.toByteArray();
        }
    }


    public static class Decoder {
        // We know the data we are decoding so just keep a cursor.
        private final byte[] decoding;
        private int cursor;
        
        public Decoder(byte[] bytesToDecode) {
            this.decoding = bytesToDecode;
            this.cursor = 0;
        }
        
        public byte decodeByte() {
            byte next = this.decoding[this.cursor];
            this.cursor += Byte.BYTES;
            return next;
        }
        
        public short decodeShort() {
            short value = (short)(
                      ((0xff & this.decoding[this.cursor + 0]) << 8)
                    | ((0xff & this.decoding[this.cursor + 1]) << 0)
            );
            this.cursor += Short.BYTES;
            return value;
        }
        
        public char decodeChar() {
            char value = (char)(
                    ((0xff & this.decoding[this.cursor + 0]) << 8)
                  | ((0xff & this.decoding[this.cursor + 1]) << 0)
            );
            this.cursor += Character.BYTES;
            return value;
        }
        
        public int decodeInt() {
            int value = (
                    ((0xff & this.decoding[this.cursor + 0]) << 24)
                  | ((0xff & this.decoding[this.cursor + 1]) << 16)
                  | ((0xff & this.decoding[this.cursor + 2]) <<  8)
                  | ((0xff & this.decoding[this.cursor + 3]) <<  0)
            );
          this.cursor += Integer.BYTES;
          return value;
        }
        
        public long decodeLong() {
            long value = (
                    ((long)(0xff & this.decoding[this.cursor + 0]) << 56)
                  | ((long)(0xff & this.decoding[this.cursor + 1]) << 48)
                  | ((long)(0xff & this.decoding[this.cursor + 2]) << 40)
                  | ((long)(0xff & this.decoding[this.cursor + 3]) << 32)
                  | ((long)(0xff & this.decoding[this.cursor + 4]) << 24)
                  | ((long)(0xff & this.decoding[this.cursor + 5]) << 16)
                  | ((long)(0xff & this.decoding[this.cursor + 6]) <<  8)
                  | ((long)(0xff & this.decoding[this.cursor + 7]) <<  0)
            );
            this.cursor += Long.BYTES;
            return value;
        }
        
        public void decodeBytesInto(byte[] buffer) {
            System.arraycopy(this.decoding, this.cursor, buffer, 0, buffer.length);
            this.cursor += buffer.length;
        }
    }


    /**
     * We provide this to make the in-memory size accounting for issue-195 coexist with the logic we use to serialize it for disk.
     * All values are in bytes.
     */
    public static class ByteSizes {
        public static final int BOOLEAN = 1;
        public static final int BYTE = 1;
        public static final int SHORT = 2;
        public static final int CHAR = 2;
        public static final int INT = 4;
        public static final int FLOAT = 4;
        public static final int LONG = 8;
        public static final int DOUBLE = 8;
    }
}

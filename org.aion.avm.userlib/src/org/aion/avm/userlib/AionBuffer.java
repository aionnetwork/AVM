package org.aion.avm.userlib;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;


public class AionBuffer {
    private static final int BYTE_MASK = 0xff;
    private static final int BYTE_SIZE = Byte.SIZE;
    private final byte[] buffer;
    private int position;
    private int limit;

    private AionBuffer(byte[] array) {
        this.buffer = array;
        this.position = 0;
        this.limit = array.length;
    }

    public static AionBuffer allocate(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Illegal capacity: " + capacity);
        }
        return new AionBuffer(new byte[capacity]);
    }

    public static AionBuffer wrap(byte[] array) {
        if (array == null) {
            throw new NullPointerException();
        }
        if (array.length < 1) {
            throw new IllegalArgumentException("Illegal capacity: " + array.length);
        }
        return new AionBuffer(array);
    }

    // ====================
    // relative get methods
    // ====================

    public AionBuffer get(byte[] dst) {
        if (dst == null) {
            throw new NullPointerException();
        }
        int remaining = this.limit - this.position;
        if (remaining < dst.length) {
            throw new BufferUnderflowException();
        }
        System.arraycopy(this.buffer, this.position, dst, 0, dst.length);
        this.position += dst.length;
        return this;
    }

    /**
     * Note that we store booleans as a 1-byte quantity (0x1 or 0x0).
     * @return The underlying byte, interpreted as a boolean (0x1 is true).
     */
    public boolean getBoolean() {
        byte value = internalGetByte();
        return (0x1 == value);
    }

    public byte getByte() {
        return internalGetByte();
    }

    private byte internalGetByte() {
        int remaining = this.limit - this.position;
        if (remaining < Byte.BYTES) {
            throw new BufferUnderflowException();
        }
        byte b = this.buffer[this.position];
        this.position += Byte.BYTES;
        return b;
    }

    public char getChar() {
        return (char) getShort();
    }

    public double getDouble() {
        return Double.longBitsToDouble(getLong());
    }

    public float getFloat() {
        return Float.intBitsToFloat(getInt());
    }

    public int getInt() {
        int remaining = this.limit - this.position;
        if (remaining < Integer.BYTES) {
            throw new BufferUnderflowException();
        }
        int i = this.buffer[this.position] << BYTE_SIZE;
        i = (i | (this.buffer[this.position + 1] & BYTE_MASK)) << BYTE_SIZE;
        i = (i | (this.buffer[this.position + 2] & BYTE_MASK)) << BYTE_SIZE;
        i |= (this.buffer[this.position + 3] & BYTE_MASK);
        this.position += Integer.BYTES;
        return i;
    }

    public long getLong() {
        int remaining = this.limit - this.position;
        if (remaining < Long.BYTES) {
            throw new BufferUnderflowException();
        }
        long l = this.buffer[this.position] << BYTE_SIZE;
        l = (l | (this.buffer[this.position + 1] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.position + 2] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.position + 3] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.position + 4] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.position + 5] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.position + 6] & BYTE_MASK)) << BYTE_SIZE;
        l |= this.buffer[this.position + 7] & BYTE_MASK;
        this.position += Long.BYTES;
        return l;
    }

    public short getShort() {
        int remaining = this.limit - this.position;
        if (remaining < Short.BYTES) {
            throw new BufferUnderflowException();
        }
        short s = (short) (this.buffer[this.position] << BYTE_SIZE);
        s |= (this.buffer[this.position + 1] & BYTE_MASK);
        this.position += Short.BYTES;
        return s;
    }

    // ====================
    // relative put methods
    // ====================

    public AionBuffer put(byte[] src) {
        if (src == null) {
            throw new NullPointerException();
        }
        int remaining = this.limit - this.position;
        if (remaining < src.length) {
            throw new BufferOverflowException();
        }
        System.arraycopy(src, 0, this.buffer, this.position, src.length);
        this.position += src.length;
        return this;
    }

    /**
     * Note that we store booleans as a 1-byte quantity (0x1 or 0x0).
     * @param flag The boolean to store as a byte (0x1 for true, 0x0 for false).
     */
    public AionBuffer putBoolean(boolean flag) {
        byte b = (byte)(flag ? 0x1 : 0x0);
        return internalPutByte(b);
    }

    public AionBuffer putByte(byte b) {
        return internalPutByte(b);
    }

    private AionBuffer internalPutByte(byte b) {
        int remaining = this.limit - this.position;
        if (remaining < Byte.BYTES) {
            throw new BufferOverflowException();
        }
        this.buffer[this.position] = b;
        this.position += Byte.BYTES;
        return this;
    }

    public AionBuffer putChar(char value) {
        return putShort((short) value);
    }

    public AionBuffer putDouble(double value) {
        return putLong(Double.doubleToRawLongBits(value));
    }

    public AionBuffer putFloat(float value) {
        return putInt(Float.floatToRawIntBits(value));
    }

    public AionBuffer putInt(int value) {
        int remaining = this.limit - this.position;
        if (remaining < Integer.BYTES) {
            throw new BufferOverflowException();
        }
        this.buffer[this.position] = (byte) ((value >> 24) & BYTE_MASK);
        this.buffer[this.position + 1] = (byte) ((value >> 16) & BYTE_MASK);
        this.buffer[this.position + 2] = (byte) ((value >> 8) & BYTE_MASK);
        this.buffer[this.position + 3] = (byte) (value & BYTE_MASK);
        this.position += Integer.BYTES;
        return this;
    }

    public AionBuffer putLong(long value) {
        int remaining = this.limit - this.position;
        if (remaining < Long.BYTES) {
            throw new BufferOverflowException();
        }
        this.buffer[this.position] = (byte) ((value >> 56) & BYTE_MASK);
        this.buffer[this.position + 1] = (byte) ((value >> 48) & BYTE_MASK);
        this.buffer[this.position + 2] = (byte) ((value >> 40) & BYTE_MASK);
        this.buffer[this.position + 3] = (byte) ((value >> 32) & BYTE_MASK);
        this.buffer[this.position + 4] = (byte) ((value >> 24) & BYTE_MASK);
        this.buffer[this.position + 5] = (byte) ((value >> 16) & BYTE_MASK);
        this.buffer[this.position + 6] = (byte) ((value >> 8) & BYTE_MASK);
        this.buffer[this.position + 7] = (byte) (value & BYTE_MASK);
        this.position += Long.BYTES;
        return this;
    }

    public AionBuffer putShort(short value) {
        int remaining = this.limit - this.position;
        if (remaining < Short.BYTES) {
            throw new BufferOverflowException();
        }
        this.buffer[this.position] = (byte) ((value >> 8) & BYTE_MASK);
        this.buffer[this.position + 1] = (byte) (value & BYTE_MASK);
        this.position += Short.BYTES;
        return this;
    }

    // =====================
    // query & misc. methods
    // =====================

    public byte[] getArray() {
        return this.buffer;
    }

    public int getCapacity() {
        return this.buffer.length;
    }

    public int getPosition() {
        return this.position;
    }

    public int getLimit() {
        return this.limit;
    }

    /**
     * Resets the position to 0 and the limit to the full capacity of the buffer.
     * Used when discarding state associated with a previous use of the buffer.
     * 
     * @return The receiver (for call chaining).
     */
    public AionBuffer clear() {
        this.position = 0;
        this.limit = this.buffer.length;
        return this;
    }

    /**
     * Sets the limit to the current position and resets the position to 0.
     * Primarily used when switching between writing and reading modes:
     *  write(X), write(Y), write(Z), flip(), read(X), read(Y), read(Z).
     * 
     * @return The receiver (for call chaining).
     */
    public AionBuffer flip() {
        this.limit = this.position;
        this.position = 0;
        return this;
    }

    /**
     * Sets the position back to 0.
     * Useful for cases where the previously processed contents want to be reprocessed.
     * 
     * @return The receiver (for call chaining).
     */
    public AionBuffer rewind() {
        this.position = 0;
        return this;
    }

    @Override
    public boolean equals(Object ob) {
        // The standard JCL ByteBuffer derives its equality from its state and internal data so do the same, here.
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof AionBuffer)) {
            return false;
        }
        AionBuffer other = (AionBuffer) ob;
        if (this.buffer.length != other.buffer.length) {
            return false;
        }
        if (this.position != other.position) {
            return false;
        }
        if (this.limit != other.limit) {
            return false;
        }
        // The comparison is not the full buffer, only up to the limit.
        for (int i = 0; i < this.limit; i++) {
            if (this.buffer[i] != other.buffer[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        // The standard JCL ByteBuffer derives its hash code from its internal data so do the same, here.
        int h = 1;
        // The comparison is not the full buffer, only up to the limit.
        for (int i = this.limit - 1; i >= 0; i--) {
            h = 31 * h + (int) this.buffer[i];
        }
        return h;
    }

    @Override
    public String toString() {
        return "AionBuffer [capacity = " + this.buffer.length + ", position = " + this.position + ", limit = " + this.limit + " ]";
    }
}

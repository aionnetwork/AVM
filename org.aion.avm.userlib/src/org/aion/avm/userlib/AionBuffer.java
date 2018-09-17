package org.aion.avm.userlib;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;


public class AionBuffer {
    private static final int BYTE_MASK = 0xff;
    private static final int BYTE_SIZE = Byte.SIZE;
    private final byte[] buffer;
    private final int capacity;
    private int size;

    private AionBuffer(byte[] array) {
        this.buffer = array;
        this.capacity = array.length;
        this.size = 0;
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
        if (this.size - dst.length < 0) {
            throw new BufferUnderflowException();
        }
        System.arraycopy(this.buffer, this.size - dst.length, dst, 0, dst.length);
        this.size -= dst.length;
        return this;
    }

    public byte getByte() {
        if (this.size == 0) {
            throw new BufferUnderflowException();
        }
        byte b = this.buffer[this.size - 1];
        this.size--;
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
        if (this.size < 4) {
            throw new BufferUnderflowException();
        }
        int i = this.buffer[this.size - 4] << BYTE_SIZE;
        i = (i | (this.buffer[this.size - 3] & BYTE_MASK)) << BYTE_SIZE;
        i = (i | (this.buffer[this.size - 2] & BYTE_MASK)) << BYTE_SIZE;
        i |= (this.buffer[this.size - 1] & BYTE_MASK);
        this.size -= 4;
        return i;
    }

    public long getLong() {
        if (this.size < 8) {
            throw new BufferUnderflowException();
        }
        long l = this.buffer[this.size - 8] << BYTE_SIZE;
        l = (l | (this.buffer[this.size - 7] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.size - 6] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.size - 5] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.size - 4] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.size - 3] & BYTE_MASK)) << BYTE_SIZE;
        l = (l | (this.buffer[this.size - 2] & BYTE_MASK)) << BYTE_SIZE;
        l |= this.buffer[this.size - 1] & BYTE_MASK;
        this.size -= 8;
        return l;
    }

    public short getShort() {
        if (this.size < 2) {
            throw new BufferUnderflowException();
        }
        short s = (short) (this.buffer[this.size - 2] << BYTE_SIZE);
        s |= (this.buffer[this.size - 1] & BYTE_MASK);
        this.size -= 2;
        return s;
    }

    // ====================
    // relative put methods
    // ====================

    public AionBuffer put(byte[] src) {
        if (src == null) {
            throw new NullPointerException();
        }
        if (this.size + src.length > this.capacity) {
            throw new BufferOverflowException();
        }
        System.arraycopy(src, 0, this.buffer, this.size, src.length);
        this.size += src.length;
        return this;
    }

    public AionBuffer putByte(byte b) {
        if (this.size == this.capacity) {
            throw new BufferOverflowException();
        }
        this.buffer[this.size] = b;
        this.size++;
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
        if (this.size + 4 > this.capacity) {
            throw new BufferOverflowException();
        }
        this.buffer[this.size] = (byte) ((value >> 24) & BYTE_MASK);
        this.buffer[this.size + 1] = (byte) ((value >> 16) & BYTE_MASK);
        this.buffer[this.size + 2] = (byte) ((value >> 8) & BYTE_MASK);
        this.buffer[this.size + 3] = (byte) (value & BYTE_MASK);
        this.size += 4;
        return this;
    }

    public AionBuffer putLong(long value) {
        if (this.size + 8 > this.capacity) {
            throw new BufferOverflowException();
        }
        this.buffer[this.size] = (byte) ((value >> 56) & BYTE_MASK);
        this.buffer[this.size + 1] = (byte) ((value >> 48) & BYTE_MASK);
        this.buffer[this.size + 2] = (byte) ((value >> 40) & BYTE_MASK);
        this.buffer[this.size + 3] = (byte) ((value >> 32) & BYTE_MASK);
        this.buffer[this.size + 4] = (byte) ((value >> 24) & BYTE_MASK);
        this.buffer[this.size + 5] = (byte) ((value >> 16) & BYTE_MASK);
        this.buffer[this.size + 6] = (byte) ((value >> 8) & BYTE_MASK);
        this.buffer[this.size + 7] = (byte) (value & BYTE_MASK);
        this.size += 8;
        return this;
    }

    public AionBuffer putShort(short value) {
        if (this.size + 2 > this.capacity) {
            throw new BufferOverflowException();
        }
        this.buffer[this.size] = (byte) ((value >> 8) & BYTE_MASK);
        this.buffer[this.size + 1] = (byte) (value & BYTE_MASK);
        this.size += 2;
        return this;
    }

    // =====================
    // query & misc. methods
    // =====================

    public byte[] array() {
        return this.buffer;
    }

    public int capacity() {
        return this.capacity;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    public boolean isFull() {
        return this.size == this.capacity;
    }

    public void clear() {
        this.size = 0;
    }

    @Override
    public boolean equals(Object ob) {
        if (this == ob) {
            return true;
        }
        if (!(ob instanceof AionBuffer)) {
            return false;
        }
        AionBuffer other = (AionBuffer) ob;
        if (this.capacity != other.capacity) {
            return false;
        }
        if (this.size != other.size) {
            return false;
        }
        for (int i = 0; i < this.size; i++) {
            if (this.buffer[i] != other.buffer[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (int i = this.size - 1; i >= 0; i--) {
            h = 31 * h + (int) this.buffer[i];
        }
        return h;
    }

    @Override
    public String toString() {
        return "AionBuffer [capacity = " + this.capacity + ", size = " + this.size + "]";
    }
}

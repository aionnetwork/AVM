package org.aion.avm.userlib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AionBufferTest {
    private static final int RANGE_1BYTE = BigInteger.TWO.pow(8).intValue();
    private static final int RANGE_2BYTES = BigInteger.TWO.pow(16).intValue();
    private static final int INT_SLICE = BigInteger.TWO.pow(20).intValue();
    private static final int LONG_SLICE = BigInteger.TWO.pow(22).intValue();
    private static final int FLOAT_SLICE = BigInteger.TWO.pow(20).intValue();
    private static final int DOUBLE_SLICE = BigInteger.TWO.pow(22).intValue();
    private static Random random;

    private enum PrimitiveType { BYTE, CHAR, SHORT, INT, FLOAT, LONG, DOUBLE }

    @Before
    public void setup() {
        random = new Random();
    }

    @After
    public void tearDown() {
        random = null;
    }

    /**
     * Tests allocating a minimum-sized buffer and then a larger buffer.
     */
    @Test
    public void testAllocateBuffer() {
        int capacity = 1;
        AionBuffer buffer = AionBuffer.allocate(capacity);
        assertEquals(capacity, buffer.capacity());
        assertEquals(0, buffer.size());

        capacity = 100_000;
        buffer = AionBuffer.allocate(capacity);
        assertEquals(capacity, buffer.capacity());
        assertEquals(0, buffer.size());
    }

    /**
     * Tests wrapping a minimum-sized array and then a larger array.
     */
    @Test
    public void testArrayWrap() {
        byte[] array = new byte[] { 0xf };
        AionBuffer buffer = AionBuffer.wrap(array);
        assertEquals(array.length, buffer.capacity());
        assertEquals(0, buffer.size());
        assertArrayEquals(array, buffer.array());

        array = new byte[100_000];
        random.nextBytes(array);
        buffer = AionBuffer.wrap(array);
        assertEquals(array.length, buffer.capacity());
        assertEquals(0, buffer.size());
        assertArrayEquals(array, buffer.array());
    }

    /**
     * Tests that a newly created buffer is empty and not full, for both allocating and wrapping.
     */
    @Test
    public void testNewBufferIsEmpty() {
        AionBuffer buffer = AionBuffer.allocate(25_000);
        assertTrue(buffer.isEmpty());
        assertFalse(buffer.isFull());

        byte[] array = new byte[100_000];
        random.nextBytes(array);
        buffer = AionBuffer.wrap(array);
        assertTrue(buffer.isEmpty());
        assertFalse(buffer.isFull());
    }

    /**
     * Tests that a buffer that has been filled to capacity are full and not empty.
     */
    @Test
    public void testFullBuffer() {
        AionBuffer buffer = fill(AionBuffer.allocate(25_000));
        assertTrue(buffer.isFull());
        assertFalse(buffer.isEmpty());
    }

    /**
     * Tests that bytes can be put into a buffer and retrieved back from it.
     */
    @Test
    public void testPutAndGetBytes() {
        AionBuffer buffer = AionBuffer.allocate(RANGE_1BYTE);
        byte[] bytes = getArrayOfAllBytes();
        byte[] expected = reverseBytes(bytes);
        for (byte b : bytes) {
            buffer.putByte(b);
        }
        for (byte b : expected) {
            assertEquals(b, buffer.getByte());
        }
    }

    /**
     * Tests that chars can be put into a buffer and retrieved back from it.
     */
    @Test
    public void testPutAndGetChars() {
        AionBuffer buffer = AionBuffer.allocate(RANGE_2BYTES * 2);
        char[] chars = getArrayOfAllChars();
        char[] expected = reverseChars(chars);
        for (char c : chars) {
            buffer.putChar(c);
        }
        for (char c : expected) {
            assertEquals(c, buffer.getChar());
        }
    }

    /**
     * Tests that shorts can be put into a buffer and retrieved back from it.
     */
    @Test
    public void testPutAndGetShorts() {
        AionBuffer buffer = AionBuffer.allocate(RANGE_2BYTES * 2);
        short[] shorts = getArrayOfAllShorts();
        short[] expected = reverseShorts(shorts);
        for (short s : shorts) {
            buffer.putShort(s);
        }
        for (short s : expected) {
            assertEquals(s, buffer.getShort());
        }
    }

    /**
     * Tests that ints can be put into a buffer and retrieved back from it.
     * (need space for 5 slices of size INT_SLICE ints - 4 bytes each)
     */
    @Test
    public void testPutAndGetInts() {
        AionBuffer buffer = AionBuffer.allocate(INT_SLICE * 5 * 4);
        int[] ints = getArrayOfDiverseInts();
        int[] expected = reverseInts(ints);
        for (int i : ints) {
            buffer.putInt(i);
        }
        for (int i : expected) {
            assertEquals(i, buffer.getInt());
        }
    }

    /**
     * Tests that longs can be put into a buffer and retrieved back from it.
     * (need space for 5 slices of size LONG_SLICE longs - 8 bytes each)
     */
    @Test
    public void testPutAndGetLongs() {
        AionBuffer buffer = AionBuffer.allocate(LONG_SLICE * 5 * 8);
        long[] longs = getArrayOfDiverseLongs();
        long[] expected = reverseLongs(longs);
        for (long l : longs) {
            buffer.putLong(l);
        }
        for (long l : expected) {
            assertEquals(l, buffer.getLong());
        }
    }

    /**
     * Tests that floats can be put into a buffer and retrieved back from it.
     * (need space for 6 special values plus FLOAT_SLICE floats - 4 bytes each)
     */
    @Test
    public void testPutAndGetFloats() {
        AionBuffer buffer = AionBuffer.allocate((FLOAT_SLICE + 6) * 4);
        float[] floats = getArrayOfSpecialAndRandomFloats();
        float[] expected = reverseFloats(floats);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        for (float f : expected) {
            assertEquals(f, buffer.getFloat(), 0);
        }
    }

    /**
     * Tests that doubles can be put into a buffer and retrieved back from it.
     * (need space for 6 special values plus DOUBLE_SLICE doubles - 8 bytes each)
     */
    @Test
    public void testPutAndGetDoubles() {
        AionBuffer buffer = AionBuffer.allocate((DOUBLE_SLICE + 6) * 8);
        double[] doubles = getArrayOfSpecialAndRandomDoubles();
        double[] expected = reverseDoubles(doubles);
        for (double d : doubles) {
            buffer.putDouble(d);
        }
        for (double d : expected) {
            assertEquals(d, buffer.getDouble(), 0);
        }
    }

    /**
     * Tests that the bytes in a byte array can be transferred to the buffer via the put() method.
     */
    @Test
    public void testTransferBytesToBuffer() {
        byte[] bytes = getArrayOfAllBytes();
        assertArrayEquals(bytes, AionBuffer.allocate(RANGE_1BYTE).put(bytes).array());
    }

    /**
     * Tests that the bytes in the buffer can be transferred to a byte array via the get() method.
     */
    @Test
    public void testTransferBytesFromBuffer() {
        AionBuffer buffer = AionBuffer.allocate(RANGE_1BYTE);
        byte[] bytes = getArrayOfAllBytes();
        byte[] destination = getRandomBytes(RANGE_1BYTE);
        assertFalse(Arrays.equals(bytes, destination));
        for (byte b : bytes) {
            buffer.putByte(b);
        }
        buffer.get(destination);
        assertArrayEquals(bytes, buffer.array());
    }

    /**
     * Tests that putting an array of length 0 into the buffer does not cause the buffer's size to
     * be incremented and no exceptions are thrown.
     */
    @Test
    public void testPutEmptyArray() {
        AionBuffer buffer = AionBuffer.allocate(55).put(new byte[0]);
        assertEquals(0, buffer.size());
    }

    /**
     * Tests that transferring data from the buffer into an array of length 0 does not cause the
     * buffer's size to be decremented and no exceptions are thrown.
     */
    @Test
    public void testGetEmptyArray() {
        AionBuffer buffer = AionBuffer.allocate(47).get(new byte[0]);
        assertEquals(0, buffer.size());
    }

    /**
     * Tests that a call to clear() will make the buffer into an empty buffer.
     */
    @Test
    public void testClear() {
        byte[] bytes = getArrayOfAllBytes();
        AionBuffer buffer = AionBuffer.allocate(RANGE_1BYTE).put(bytes);
        assertEquals(RANGE_1BYTE, buffer.size());
        assertTrue(buffer.isFull());
        assertFalse(buffer.isEmpty());

        buffer.clear();
        assertEquals(0, buffer.size());
        assertTrue(buffer.isEmpty());
        assertFalse(buffer.isFull());
    }

    /**
     * Tests that the buffer size increments and decrements appropriately depending on whether data
     * is added or removed from the buffer.
     */
    @Test
    public void testBufferSize() {
        int capacity = 10_000;
        AionBuffer buffer = AionBuffer.allocate(capacity);
        int[] quantities = getRandomInts(capacity * 10, capacity / 10);

        for (int quantity : quantities) {
            PrimitiveType primitiveType = getNextPrimitiveType();
            int size = buffer.size();
            if (random.nextBoolean()) {
                // Try put() unless it causes overflow, then try get(), otherwise just skip this round.
                if (isSpaceToPut(buffer, primitiveType, quantity)) {
                    putPrimitives(buffer, primitiveType, quantity);
                    assertEquals(size + getPrimitiveSize(primitiveType, quantity), buffer.size());
                } else if (isDataToGet(buffer, primitiveType, quantity)) {
                    getPrimitives(buffer, primitiveType, quantity);
                    assertEquals(size - getPrimitiveSize(primitiveType, quantity), buffer.size());
                }
            } else {
                // Try get() unless it causes underflow, then try put(), otherwise just skip this round.
                if (isDataToGet(buffer, primitiveType, quantity)) {
                    getPrimitives(buffer, primitiveType, quantity);
                    assertEquals(size - getPrimitiveSize(primitiveType, quantity), buffer.size());
                } else if (isSpaceToPut(buffer, primitiveType, quantity)) {
                    putPrimitives(buffer, primitiveType, quantity);
                    assertEquals(size + getPrimitiveSize(primitiveType, quantity), buffer.size());
                }
            }
        }
    }

    /**
     * Tests that the buffer size increments and decrements appropriately depending on whether we
     * call get() or put() - the two bulk add/remove calls.
     */
    @Test
    public void testBufferSizeOnBulkGetsAndPuts() {
        int capacity = 10_000;
        AionBuffer buffer = AionBuffer.allocate(capacity);
        int[] quantities = getRandomInts(capacity * 10, capacity / 10);

        for (int quantity : quantities) {
            int size = buffer.size();
            if (random.nextBoolean()) {
                // Try put() unless it causes overflow, then try get(), otherwise just skip this round.
                if (isSpaceToPut(buffer, PrimitiveType.BYTE, quantity)) {
                    buffer.put(getRandomBytes(quantity));
                    assertEquals(size + quantity, buffer.size());
                } else if (isDataToGet(buffer, PrimitiveType.BYTE, quantity)) {
                    buffer.get(new byte[quantity]);
                    assertEquals(size - quantity, buffer.size());
                }
            } else {
                // Try get() unless it causes underflow, then try put(), otherwise just skip this round.
                if (isDataToGet(buffer, PrimitiveType.BYTE, quantity)) {
                    buffer.get(new byte[quantity]);
                    assertEquals(size - quantity, buffer.size());
                } else if (isSpaceToPut(buffer, PrimitiveType.BYTE, quantity)) {
                    buffer.put(getRandomBytes(quantity));
                    assertEquals(size + quantity, buffer.size());
                }
            }
        }
    }

    /**
     * Tests a mixed bag of various get and put methods for the different primitive types.
     */
    @Test
    public void testMixedGetAndPutTypeCalls() {
        AionBuffer buffer = AionBuffer.allocate(10_000);

        for (int repeats = 0; repeats < 10; repeats++) {
            List<Object> primitivesInBuffer = new ArrayList<>();
            int i = 0;

            // Add primitives into the buffer.
            List<PrimitiveType> primitiveTypes = getRandomPutCallsUntilFull(buffer);
            for (PrimitiveType primitiveType : primitiveTypes) {
                primitivesInBuffer.add(putPrimitive(buffer, primitiveType));
            }
            assertFalse(buffer.isEmpty());

            // Remove them from the buffer.
            Collections.reverse(primitiveTypes);
            Collections.reverse(primitivesInBuffer);
            for (PrimitiveType primitiveType : primitiveTypes) {
                getAndCheckPrimitive(buffer, primitiveType, primitivesInBuffer.get(i++));
            }

            assertTrue(buffer.isEmpty());
        }
    }

    /**
     * Tests the equals method by creating two buffers of equal capacity, both of which are backed
     * by the exact same underlying array.
     *
     * Some meaningless puts and gets are done (so that the underlying array doesn't get modified -
     * this isn't necessary, but easier to reason about) so that only when the two buffers are the
     * same size will they be equal, and otherwise they will not be.
     */
    @Test
    public void testEqualsOnBuffersWithSameCapacity() {
        // must have an even capacity so there's overlap between the two buffers.
        int capacity = 30_000;
        assertEquals(0, capacity % 2);

        byte[] array = getRandomBytes(capacity);
        AionBuffer buffer1 = AionBuffer.wrap(array);
        AionBuffer buffer2 = AionBuffer.wrap(array);

        for (int i = 0; i < capacity; i++) {
            setBufferSize(buffer1, capacity - i);
            setBufferSize(buffer2, i);

            if (i == capacity / 2) {
                assertEquals(buffer1, buffer2);
            } else {
                assertNotEquals(buffer1, buffer2);
            }
        }
    }

    /**
     * Tests the equals method by creaitng two buffers whose capacities differ by one, both of which
     * are backed by equivalent arrays (excluding the last byte, of course).
     *
     * These buffers are always considered unequal because they differ in capacity. We cycle through
     * each size and ensure that no matter the size and the fact their current data is equivalent,
     * they are never equal.
     */
    @Test
    public void testEqualsOnBuffersWithDifferentCapacities() {
        int capacity = 30_000;
        byte[] array = getRandomBytes(capacity);
        AionBuffer buffer1 = AionBuffer.wrap(array);
        AionBuffer buffer2 = AionBuffer.wrap(Arrays.copyOfRange(array, 0, array.length - 1));

        for (int i = 0; i < capacity; i++) {
            setBufferSize(buffer1, i);
            setBufferSize(buffer2, i);
            assertNotEquals(buffer1, buffer2);
        }
    }

    /**
     * Tests that equals() against a null object returns false.
     */
    @Test
    public void testEqualsWithNull() {
        AionBuffer buffer = AionBuffer.allocate(6_000);
        assertNotEquals(buffer, null);
        assertNotEquals(null, buffer);
    }

    /**
     * Tests that equals() on the same reference returns true.
     */
    @Test
    public void testEqualsOnSameReference() {
        AionBuffer buffer = AionBuffer.allocate(6_000);
        setBufferSize(buffer, 3_456);
        assertEquals(buffer, buffer);
    }

    // =======================================
    // below are tests that trigger exceptions
    // =======================================

    @Test(expected = IllegalArgumentException.class)
    public void testBufferAllocateZeroBytes() {
        AionBuffer.allocate(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBufferAllocateNegativeBytes() {
        AionBuffer.allocate(-1);
    }

    @Test(expected = NullPointerException.class)
    public void testBufferWrapNull() {
        AionBuffer.wrap(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBufferWrapZeroLengthArray() {
        AionBuffer.wrap(new byte[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testTransferBytesToNullArray() {
        AionBuffer.allocate(10).get(null);
    }

    @Test(expected = BufferUnderflowException.class)
    public void testTransferBytesFromEmptyBuffer() {
        AionBuffer.allocate(10).get(new byte[1]);
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetByteFromEmptyBuffer() {
        AionBuffer.allocate(10).getByte();
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetCharFromBufferSizeOne() {
        AionBuffer.allocate(2).putByte(getRandomByte()).getChar();
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetShortFromBufferSizeOne() {
        AionBuffer.allocate(2).putByte(getRandomByte()).getShort();
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetIntFromBufferSizeThree() {
        AionBuffer.allocate(4).put(getRandomBytes(3)).getInt();
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetFloatFromBufferSizeThree() {
        AionBuffer.allocate(4).put(getRandomBytes(3)).getFloat();
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetLongFromBufferSizeSeven() {
        AionBuffer.allocate(8).put(getRandomBytes(7)).getLong();
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetDoubleFromBufferSizeSeven() {
        AionBuffer.allocate(8).put(getRandomBytes(7)).getDouble();
    }

    @Test(expected = NullPointerException.class)
    public void testPutNull() {
        AionBuffer.allocate(1).put(null);
    }

    @Test(expected = BufferOverflowException.class)
    public void testTransferArrayLargerThanBufferRemainingSpace() {
        int capacity = 50;
        byte[] bytes = getRandomBytes(15);
        setBufferSize(AionBuffer.allocate(capacity), capacity - bytes.length + 1).put(bytes);
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutByteIntoFullBuffer() {
        fill(AionBuffer.allocate(50)).putByte(getRandomByte());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutCharIntoBufferWithOneByteOfSpace() {
        int capacity = 72;
        setBufferSize(AionBuffer.allocate(capacity), capacity - 1).putChar(getRandomChar());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutShortIntoBufferWithOneByteOfSpace() {
        int capacity = 53;
        setBufferSize(AionBuffer.allocate(capacity), capacity - 1).putShort(getRandomShort());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutIntIntoBufferWithThreeBytesOfSpace() {
        int capacity = 103;
        setBufferSize(AionBuffer.allocate(capacity), capacity - 3).putInt(getRandomInt());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutFloatIntoBufferWithThreeBytesOfSpace() {
        int capacity = 88;
        setBufferSize(AionBuffer.allocate(capacity), capacity - 3).putFloat(getRandomFloat());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutLongIntoBufferWithSevenBytesOfSpace() {
        int capacity = 153;
        setBufferSize(AionBuffer.allocate(capacity), capacity - 7).putLong(getRandomLong());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutDoubleIntoBufferWithSevenBytesOfSpace() {
        int capacity = 131;
        setBufferSize(AionBuffer.allocate(capacity), capacity - 7).putDouble(getRandomDouble());
    }

    // <------------------------------------helpers below------------------------------------------>

    private static byte[] reverseBytes(byte[] bytes) {
        byte[] reverse = new byte[bytes.length];
        for (int i = bytes.length - 1; i >= 0; i--) {
            reverse[bytes.length - 1 - i] = bytes[i];
        }
        return reverse;
    }

    private static char[] reverseChars(char[] chars) {
        char[] reverse = new char[chars.length];
        for (int i = chars.length - 1; i >= 0; i--) {
            reverse[chars.length - 1 - i] = chars[i];
        }
        return reverse;
    }

    private static short[] reverseShorts(short[] shorts) {
        short[] reverse = new short[shorts.length];
        for (int i = shorts.length - 1; i >= 0; i--) {
            reverse[shorts.length - 1 - i] = shorts[i];
        }
        return reverse;
    }

    private static int[] reverseInts(int[] ints) {
        int[] reverse = new int[ints.length];
        for (int i = ints.length - 1; i >= 0; i--) {
            reverse[ints.length - 1 - i] = ints[i];
        }
        return reverse;
    }

    private static long[] reverseLongs(long[] longs) {
        long[] reverse = new long[longs.length];
        for (int i = longs.length - 1; i >= 0; i--) {
            reverse[longs.length - 1 - i] = longs[i];
        }
        return reverse;
    }

    private static float[] reverseFloats(float[] floats) {
        float[] reverse = new float[floats.length];
        for (int i = floats.length - 1; i >= 0; i--) {
            reverse[floats.length - 1 - i] = floats[i];
        }
        return reverse;
    }

    private static double[] reverseDoubles(double[] doubles) {
        double[] reverse = new double[doubles.length];
        for (int i = doubles.length - 1; i >= 0; i--) {
            reverse[doubles.length - 1 - i] = doubles[i];
        }
        return reverse;
    }

    /**
     * Adds as many bytes as are necessary to buffer to fill it.
     */
    private static AionBuffer fill(AionBuffer buffer) {
        int space = buffer.capacity() - buffer.size();
        for (int i = 0; i < space; i++) {
            buffer.putByte((byte) i);
        }
        return buffer;
    }

    /**
     * Returns a byte array such that every possible byte value is contained in the array.
     */
    private static byte[] getArrayOfAllBytes() {
        byte[] bytes = new byte[RANGE_1BYTE];
        for (int i = 0; i < RANGE_1BYTE; i++) {
            bytes[i] = (byte) i;
        }
        return bytes;
    }

    /**
     * Returns a char array such that every possible char value is contained in the array.
     */
    private static char[] getArrayOfAllChars() {
        char[] chars = new char[RANGE_2BYTES];
        for (int i = 0; i < RANGE_2BYTES; i++) {
            chars[i] = (char) i;
        }
        return chars;
    }

    /**
     * Returns a short array such that every possible short value is contained in the array.
     */
    private static short[] getArrayOfAllShorts() {
        short[] shorts = new short[RANGE_2BYTES];
        for (int i = 0; i < RANGE_2BYTES; i++) {
            shorts[i] = (short) i;
        }
        return shorts;
    }

    private static byte getRandomByte() {
        byte[] bytes = new byte[1];
        random.nextBytes(bytes);
        return bytes[0];
    }

    private static char getRandomChar() {
        return (char) random.nextInt();
    }

    private static short getRandomShort() {
        return (short) random.nextInt();
    }

    private static int getRandomInt() {
        return random.nextInt();
    }

    private static float getRandomFloat() {
        return random.nextFloat();
    }

    private static long getRandomLong() {
        return random.nextLong();
    }

    private static double getRandomDouble() {
        return random.nextDouble();
    }

    /**
     * Returns an array of num random bytes.
     */
    private static byte[] getRandomBytes(int num) {
        byte[] bytes = new byte[num];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Returns an array of num random ints, each of which is in the range [0, bound).
     */
    private static int[] getRandomInts(int num, int bound) {
        int[] ints = new int[num];
        for (int i = 0; i < num; i++) {
            ints[i] = random.nextInt(bound);
        }
        return ints;
    }

    /**
     * Returns an int array consisting of diverse quantities. Among these quantities will be the
     * minimum and maximum int values as well as other negative and positive values and zero.
     */
    private static int[] getArrayOfDiverseInts() {
        return appendLargestInts(
            appendPositiveInts(
                appendNearZeroInts(
                    appendNegativeInts(
                        getLowestInts()))));
    }

    /**
     * Returns a long array consisting of diverse quantities. Among these quantities will be the
     * minimum and maximum long values as well as other negative and positive values and zero.
     */
    private static long[] getArrayOfDiverseLongs() {
        return appendLargestLongs(
            appendPositiveLongs(
                appendNearZeroLongs(
                    appendNegativeLongs(
                        getLowestLongs()))));
    }

    /**
     * Returns a float array consisting of the special float values as well as random float values.
     *
     * The special values: NaN, positive/negative infinity, min/max value, min normal value.
     */
    private static float[] getArrayOfSpecialAndRandomFloats() {
       return appendRandomFloats(
           getSpecialFloatValues());
    }

    /**
     * Returns a double array consisting of the special double values as well as random double
     * values.
     *
     * The special values: NaN, positive/negative infinity, min/max value, min normal value.
     */
    private static double[] getArrayOfSpecialAndRandomDoubles() {
        return appendRandomDoubles(
            getSpecialDoubleValues());
    }

    /**
     * Returns a random PrimitiveType.
     */
    private static PrimitiveType getNextPrimitiveType() {
        return PrimitiveType.values()[random.nextInt(7)];
    }

    /**
     * Returns the size of quantity number of primitives whose type is primitiveType.
     */
    private static int getPrimitiveSize(PrimitiveType primitiveType, int quantity) {
        switch (primitiveType) {
            case BYTE: return quantity;
            case CHAR:
            case SHORT: return quantity * 2;
            case INT:
            case FLOAT: return quantity * 4;
            case LONG:
            case DOUBLE: return quantity * 8;
            default: return 0;
        }
    }

    /**
     * Returns true if, and only if, there is adequate space in buffer to put num data, each of
     * whose primitive type is primitiveType.
     */
    private static boolean isSpaceToPut(AionBuffer buffer, PrimitiveType primitiveType, int num) {
        switch (primitiveType) {
            case BYTE: return buffer.capacity() - buffer.size() >= num;
            case CHAR:
            case SHORT: return buffer.capacity() - buffer.size() >= num * 2;
            case INT:
            case FLOAT: return buffer.capacity() - buffer.size() >= num * 4;
            case LONG:
            case DOUBLE: return buffer.capacity() - buffer.size() >= num * 8;
            default: return false;
        }
    }

    /**
     * Returns true if, and only if, there is an adequate amount of data in buffer to get num data,
     * each of whose primitive type is primitiveType.
     */
    private static boolean isDataToGet(AionBuffer buffer, PrimitiveType primitiveType, int num) {
        switch (primitiveType) {
            case BYTE: return buffer.size() >= num;
            case CHAR:
            case SHORT: return buffer.size() >= num * 2;
            case INT:
            case FLOAT: return buffer.size() >= num * 4;
            case LONG:
            case DOUBLE: return buffer.size() >= num * 8;
            default: return false;
        }
    }

    /**
     * Calls the putX() method num times on buffer, where X is the primitive given by primitiveType.
     */
    private static void putPrimitives(AionBuffer buffer, PrimitiveType primitiveType, int num) {
        switch (primitiveType) {
            case BYTE:
                for (int i = 0; i < num; i++)
                    buffer.putByte((byte) i);
                break;
            case CHAR:
                for (int i = 0; i < num; i++)
                    buffer.putChar((char) i);
                break;
            case SHORT:
                for (int i = 0; i < num; i++)
                    buffer.putShort((short) i);
                break;
            case INT:
                for (int i = 0; i < num; i++)
                    buffer.putInt(i);
                break;
            case FLOAT:
                for (int i = 0; i < num; i++)
                    buffer.putFloat(i);
                break;
            case LONG:
                for (int i = 0; i < num; i++)
                    buffer.putLong(i);
                break;
            case DOUBLE:
                for (int i = 0; i < num; i++)
                    buffer.putDouble(i);
                break;
        }
    }

    /**
     * Calls the getX() method num times on buffer, where X is the primitive given by primitiveType.
     */
    private static void getPrimitives(AionBuffer buffer, PrimitiveType primitiveType, int num) {
        switch (primitiveType) {
            case BYTE:
                for (int i = 0; i < num; i++)
                    buffer.getByte();
                break;
            case CHAR:
                for (int i = 0; i < num; i++)
                    buffer.getChar();
                break;
            case SHORT:
                for (int i = 0; i < num; i++)
                    buffer.getShort();
                break;
            case INT:
                for (int i = 0; i < num; i++)
                    buffer.getInt();
                break;
            case FLOAT:
                for (int i = 0; i < num; i++)
                    buffer.getFloat();
                break;
            case LONG:
                for (int i = 0; i < num; i++)
                    buffer.getLong();
                break;
            case DOUBLE:
                for (int i = 0; i < num; i++)
                    buffer.getDouble();
                break;
        }
    }

    /**
     * Puts a random primitive of type primitiveType into buffer and returns that primitive as its
     * wrapper class under Object.
     */
    private static Object putPrimitive(AionBuffer buffer, PrimitiveType primitiveType) {
        long l = random.nextLong();
        switch (primitiveType) {
            case BYTE:
                byte b = (byte) l;
                buffer.putByte(b);
                return b;
            case CHAR:
                char c = (char) l;
                buffer.putChar(c);
                return c;
            case SHORT:
                short s = (short) l;
                buffer.putShort(s);
                return s;
            case INT:
                int i = (int) l;
                buffer.putInt(i);
                return i;
            case FLOAT:
                float f = (float) l;
                buffer.putFloat(f);
                return f;
            case LONG:
                buffer.putLong(l);
                return l;
            case DOUBLE:
                double d = (double) l;
                buffer.putDouble(d);
                return d;
            default: return null;
        }
    }

    /**
     * Calls the getX() method once on buffer, where X is the primitive given by primitiveType, and
     * then asserts that the returned primitive is in fact equal to the primitive wrapped by the
     * primitive wrapper class given by expected.
     */
    private static void getAndCheckPrimitive(AionBuffer buffer, PrimitiveType primitiveType, Object expected) {
        switch (primitiveType) {
            case BYTE:
                Byte b = (Byte) expected;
                assertEquals(b.byteValue(), buffer.getByte());
                break;
            case CHAR:
                Character c = (Character) expected;
                assertEquals(c.charValue(), buffer.getChar());
                break;
            case SHORT:
                Short s = (Short) expected;
                assertEquals(s.shortValue(), buffer.getShort());
                break;
            case INT:
                Integer i = (Integer) expected;
                assertEquals(i.intValue(), buffer.getInt());
                break;
            case FLOAT:
                Float f = (Float) expected;
                assertEquals(f.floatValue(), buffer.getFloat(), 0);
                break;
            case LONG:
                Long l = (Long) expected;
                assertEquals(l.longValue(), buffer.getLong());
                break;
            case DOUBLE:
                Double d = (Double) expected;
                assertEquals(d.doubleValue(), buffer.getDouble(), 0);
                break;
        }
    }

    /**
     * Returns a list of primitive types that can be inserted into buffer without causing overflow.
     */
    private static List<PrimitiveType> getRandomPutCallsUntilFull(AionBuffer buffer) {
        int capacity = buffer.capacity();
        int initialSize = buffer.size();

        List<PrimitiveType> primitiveTypes = new ArrayList<>();
        while (true) {
            PrimitiveType primitiveType = getNextPrimitiveType();
            initialSize += getPrimitiveSize(primitiveType, 1);
            if (initialSize > capacity) {
                break;
            }
            primitiveTypes.add(primitiveType);
        }

        return primitiveTypes;
    }

    /**
     * Functionally equivalent to directly setting buffer.size() to equal size without changes to
     * the underlying buffer.
     *
     * Returns the same buffer as buffer.
     */
    private static AionBuffer setBufferSize(AionBuffer buffer, int size) {
        if (buffer.size() == size) {
            return buffer;
        }

        int initialSize = buffer.size();
        if (initialSize > size) {
            for (int i = 0; i < initialSize - size; i++) {
                buffer.getByte();
            }
        } else {
            for (int i = initialSize; i < size; i++) {
                buffer.putByte(buffer.array()[i]);
            }
        }
        assertEquals(size, buffer.size());
        return buffer;
    }

    // <--------------------------------helpers of helpers below----------------------------------->

    private static int[] getLowestInts() {
        int[] newArray = new int[INT_SLICE];
        for (int i = 0; i < INT_SLICE; i++) {
            newArray[i] = Integer.MIN_VALUE + i;
        }
        return newArray;
    }

    private static int[] appendNegativeInts(int[] array) {
        int size = array.length + INT_SLICE;
        int[] newArray = new int[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = array.length; i < size; i++) {
            newArray[i] = -3_000_000 - array.length + i;
        }
        return newArray;
    }

    private static int[] appendNearZeroInts(int[] array) {
        int size = array.length + INT_SLICE;
        int[] newArray = new int[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = array.length; i < size; i++) {
            newArray[i] = -(INT_SLICE / 2) - array.length + i;
        }
        return newArray;
    }

    private static int[] appendPositiveInts(int[] array) {
        int size = array.length + INT_SLICE;
        int[] newArray = new int[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = array.length; i < size; i++) {
            newArray[i] = 28_000_000 - array.length + i;
        }
        return newArray;
    }

    private static int[] appendLargestInts(int[] array) {
        int size = array.length + INT_SLICE;
        int[] newArray = new int[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = array.length; i < size; i++) {
            newArray[i] = (Integer.MAX_VALUE - i) + array.length;
        }
        return newArray;
    }

    private static long[] getLowestLongs() {
        long[] newArray = new long[LONG_SLICE];
        for (int i = 0; i < LONG_SLICE; i++) {
            newArray[i] = Long.MIN_VALUE + i;
        }
        return newArray;
    }

    private static long[] appendNegativeLongs(long[] array) {
        int size = array.length + LONG_SLICE;
        long[] newArray = new long[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = array.length; i < size; i++) {
            newArray[i] = -300_000_000_000L - array.length + i;
        }
        return newArray;
    }

    private static long[] appendNearZeroLongs(long[] array) {
        int size = array.length + LONG_SLICE;
        long[] newArray = new long[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = array.length; i < size; i++) {
            newArray[i] = -(LONG_SLICE / 2) - array.length + i;
        }
        return newArray;
    }

    private static long[] appendPositiveLongs(long[] array) {
        int size = array.length + LONG_SLICE;
        long[] newArray = new long[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = array.length; i < size; i++) {
            newArray[i] = 28_000_000_000L - array.length + i;
        }
        return newArray;
    }

    private static long[] appendLargestLongs(long[] array) {
        int size = array.length + LONG_SLICE;
        long[] newArray = new long[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = array.length; i < size; i++) {
            newArray[i] = (Long.MAX_VALUE - i) + array.length;
        }
        return newArray;
    }

    private static float[] appendRandomFloats(float[] array) {
        int size = array.length + FLOAT_SLICE;
        float[] newArray = new float[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = 0; i < size; i++) {
            newArray[i] = random.nextFloat();
        }
        return newArray;
    }

    private static double[] appendRandomDoubles(double[] array) {
        int size = array.length + DOUBLE_SLICE;
        double[] newArray = new double[size];
        System.arraycopy(array, 0, newArray, 0, array.length);
        for (int i = 0; i < size; i++) {
            newArray[i] = random.nextDouble();
        }
        return newArray;
    }

    /**
     * Returns an array of the following float values:
     *      NaN
     *      Negative and positive infinity
     *      Minimum and maximum values
     *      Minimum normal value
     */
    private static float[] getSpecialFloatValues() {
        return new float[] {
            Float.NaN, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY, Float.MIN_VALUE,
            Float.MAX_VALUE, Float.MIN_NORMAL
        };
    }

    /**
     * Returns an array of the following double values:
     *      NaN
     *      Negative and positive infinity
     *      Minimum and maximum values
     *      Minimum normal value
     */
    private static double[] getSpecialDoubleValues() {
        return new double[] {
            Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.MIN_VALUE,
            Double.MAX_VALUE, Double.MIN_NORMAL
        };
    }

}

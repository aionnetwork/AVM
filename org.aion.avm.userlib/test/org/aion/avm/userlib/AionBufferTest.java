package org.aion.avm.userlib;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.math.BigInteger;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AionBufferTest {
    private static final int RANGE_1BYTE = BigInteger.TWO.pow(8).intValue();
    private static final int RANGE_2BYTES = BigInteger.TWO.pow(16).intValue();
    private static final int INT_SLICE = BigInteger.TWO.pow(18).intValue();
    private static final int LONG_SLICE = BigInteger.TWO.pow(16).intValue();
    private static final int FLOAT_SLICE = BigInteger.TWO.pow(18).intValue();
    private static final int DOUBLE_SLICE = BigInteger.TWO.pow(16).intValue();
    private static Random random;

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
        assertEquals(capacity, buffer.getLimit());
        assertEquals(0, buffer.getPosition());

        capacity = 100_000;
        buffer = AionBuffer.allocate(capacity);
        assertEquals(capacity, buffer.getLimit());
        assertEquals(0, buffer.getPosition());
    }

    /**
     * Tests wrapping a minimum-sized array and then a larger array.
     */
    @Test
    public void testArrayWrap() {
        byte[] array = new byte[] { 0xf };
        AionBuffer buffer = AionBuffer.wrap(array);
        assertEquals(array.length, buffer.getLimit());
        assertEquals(0, buffer.getPosition());
        assertArrayEquals(array, buffer.getArray());

        array = new byte[100_000];
        random.nextBytes(array);
        buffer = AionBuffer.wrap(array);
        assertEquals(array.length, buffer.getLimit());
        assertEquals(0, buffer.getPosition());
        assertArrayEquals(array, buffer.getArray());
    }

    /**
     * Tests that a newly created buffer is empty and not full, for both allocating and wrapping.
     */
    @Test
    public void testNewBufferIsEmpty() {
        AionBuffer buffer = AionBuffer.allocate(25_000);
        assertEquals(0, buffer.getPosition());

        byte[] array = new byte[100_000];
        random.nextBytes(array);
        buffer = AionBuffer.wrap(array);
        assertEquals(0, buffer.getPosition());
    }

    /**
     * Tests that a buffer that has been filled to capacity are full and not empty.
     */
    @Test
    public void testFullBuffer() {
        AionBuffer buffer = fill(AionBuffer.allocate(25_000));
        assertEquals(buffer.getLimit(), buffer.getPosition());
        assertEquals(buffer.getCapacity(), buffer.getPosition());
    }

    /**
     * Tests that bytes can be put into a buffer and retrieved back from it.
     */
    @Test
    public void testPutAndGetBytes() {
        AionBuffer buffer = AionBuffer.allocate(RANGE_1BYTE);
        byte[] bytes = getArrayOfAllBytes();
        for (byte b : bytes) {
            buffer.putByte(b);
        }
        buffer.flip();
        for (byte b : bytes) {
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
        for (char c : chars) {
            buffer.putChar(c);
        }
        buffer.flip();
        for (char c : chars) {
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
        for (short s : shorts) {
            buffer.putShort(s);
        }
        buffer.flip();
        for (short s : shorts) {
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
        for (int i : ints) {
            buffer.putInt(i);
        }
        buffer.flip();
        for (int i : ints) {
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
        for (long l : longs) {
            buffer.putLong(l);
        }
        buffer.flip();
        for (long l : longs) {
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
        for (float f : floats) {
            buffer.putFloat(f);
        }
        buffer.flip();
        for (float f : floats) {
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
        for (double d : doubles) {
            buffer.putDouble(d);
        }
        buffer.flip();
        for (double d : doubles) {
            assertEquals(d, buffer.getDouble(), 0);
        }
    }

    /**
     * Tests that the bytes in a byte array can be transferred to the buffer via the put() method.
     */
    @Test
    public void testTransferBytesToBuffer() {
        byte[] bytes = getArrayOfAllBytes();
        assertArrayEquals(bytes, AionBuffer.allocate(RANGE_1BYTE).put(bytes).getArray());
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
        buffer.flip();
        buffer.get(destination);
        assertArrayEquals(bytes, buffer.getArray());
    }

    /**
     * Tests that putting an array of length 0 into the buffer does not cause the buffer's size to
     * be incremented and no exceptions are thrown.
     */
    @Test
    public void testPutEmptyArray() {
        AionBuffer buffer = AionBuffer.allocate(55).put(new byte[0]);
        assertEquals(0, buffer.getPosition());
    }

    /**
     * Tests that transferring data from the buffer into an array of length 0 does not cause the
     * buffer's size to be decremented and no exceptions are thrown.
     */
    @Test
    public void testGetEmptyArray() {
        AionBuffer buffer = AionBuffer.allocate(47).get(new byte[0]);
        assertEquals(0, buffer.getPosition());
    }

    /**
     * Tests that a call to clear() will make the buffer into an empty buffer.
     */
    @Test
    public void testClear() {
        byte[] bytes = getArrayOfAllBytes();
        AionBuffer buffer = AionBuffer.allocate(RANGE_1BYTE).put(bytes);
        assertEquals(RANGE_1BYTE, buffer.getPosition());
        assertEquals(buffer.getLimit(), buffer.getPosition());

        buffer.clear();
        assertEquals(0, buffer.getPosition());
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

        assertEquals(buffer1, buffer2);
        buffer1.getByte();
        assertNotEquals(buffer1, buffer2);
    }

    /**
     * Tests the equals method by creating two buffers whose capacities differ by one, both of which
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

        assertNotEquals(buffer1, buffer2);
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
        // Move the position to test something more interesting.
        for (int i = 0; i < 3_456; ++i) {
            buffer.getByte();
        }
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
        cueUpToPosition(AionBuffer.allocate(10), 10).get(new byte[1]);
    }

    @Test(expected = BufferUnderflowException.class)
    public void testTransferBytesFromTruncatedBuffer() {
        AionBuffer.allocate(10).flip().get(new byte[1]);
    }

    @Test(expected = BufferUnderflowException.class)
    public void testGetByteFromEmptyBuffer() {
        cueUpToPosition(AionBuffer.allocate(10), 10).getByte();
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
        cueUpToPosition(AionBuffer.allocate(capacity), capacity - bytes.length + 1).put(bytes);
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutByteIntoFullBuffer() {
        fill(AionBuffer.allocate(50)).putByte(getRandomByte());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutCharIntoBufferWithOneByteOfSpace() {
        int capacity = 72;
        cueUpToPosition(AionBuffer.allocate(capacity), capacity - 1).putChar(getRandomChar());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutShortIntoBufferWithOneByteOfSpace() {
        int capacity = 53;
        cueUpToPosition(AionBuffer.allocate(capacity), capacity - 1).putShort(getRandomShort());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutIntIntoBufferWithThreeBytesOfSpace() {
        int capacity = 103;
        cueUpToPosition(AionBuffer.allocate(capacity), capacity - 3).putInt(getRandomInt());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutFloatIntoBufferWithThreeBytesOfSpace() {
        int capacity = 88;
        cueUpToPosition(AionBuffer.allocate(capacity), capacity - 3).putFloat(getRandomFloat());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutLongIntoBufferWithSevenBytesOfSpace() {
        int capacity = 153;
        cueUpToPosition(AionBuffer.allocate(capacity), capacity - 7).putLong(getRandomLong());
    }

    @Test(expected = BufferOverflowException.class)
    public void testPutDoubleIntoBufferWithSevenBytesOfSpace() {
        int capacity = 131;
        cueUpToPosition(AionBuffer.allocate(capacity), capacity - 7).putDouble(getRandomDouble());
    }

    @Test
    public void testWrapDimensions() {
        byte[] array = new byte[100];
        AionBuffer buffer = AionBuffer.wrap(array);
        assertEquals(0, buffer.getPosition());
        assertEquals(array.length, buffer.getLimit());
        assertEquals(array.length, buffer.getCapacity());
    }

    @Test
    public void testAllocateDimensions() {
        AionBuffer buffer = AionBuffer.allocate(100);
        assertEquals(0, buffer.getPosition());
        assertEquals(100, buffer.getLimit());
        assertEquals(100, buffer.getCapacity());
    }

    @Test
    public void testClearDimensions() {
        AionBuffer buffer = AionBuffer.allocate(100);
        buffer.putInt(5);
        assertEquals(Integer.BYTES, buffer.getPosition());
        assertEquals(100, buffer.getLimit());
        assertEquals(100, buffer.getCapacity());
        
        buffer.clear();
        assertEquals(0, buffer.getPosition());
        assertEquals(100, buffer.getLimit());
        assertEquals(100, buffer.getCapacity());
    }

    @Test
    public void testFlipDimensions() {
        AionBuffer buffer = AionBuffer.allocate(100);
        buffer.putInt(5);
        assertEquals(Integer.BYTES, buffer.getPosition());
        assertEquals(100, buffer.getLimit());
        assertEquals(100, buffer.getCapacity());
        
        buffer.flip();
        assertEquals(0, buffer.getPosition());
        assertEquals(Integer.BYTES, buffer.getLimit());
        assertEquals(100, buffer.getCapacity());
        
        // (we will also verify that clear resets the limit, here)
        buffer.clear();
        assertEquals(0, buffer.getPosition());
        assertEquals(100, buffer.getLimit());
        assertEquals(100, buffer.getCapacity());
    }

    @Test
    public void testRewindDimensions() {
        AionBuffer buffer = AionBuffer.allocate(100);
        buffer.putInt(5);
        assertEquals(Integer.BYTES, buffer.getPosition());
        assertEquals(100, buffer.getLimit());
        assertEquals(100, buffer.getCapacity());
        
        buffer.flip();
        buffer.getInt();
        assertEquals(Integer.BYTES, buffer.getPosition());
        assertEquals(Integer.BYTES, buffer.getLimit());
        assertEquals(100, buffer.getCapacity());
        
        buffer.rewind();
        assertEquals(0, buffer.getPosition());
        assertEquals(Integer.BYTES, buffer.getLimit());
        assertEquals(100, buffer.getCapacity());
    }

    // <------------------------------------helpers below------------------------------------------>

    /**
     * Adds as many bytes as are necessary to buffer to fill it.
     */
    private static AionBuffer fill(AionBuffer buffer) {
        int space = buffer.getLimit() - buffer.getPosition();
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

    private AionBuffer cueUpToPosition(AionBuffer buffer, int position) {
        while (buffer.getPosition() < position) {
            buffer.getByte();
        }
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

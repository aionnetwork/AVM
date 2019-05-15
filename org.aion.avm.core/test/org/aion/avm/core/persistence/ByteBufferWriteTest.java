package org.aion.avm.core.persistence;

import i.IObjectSerializer;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class ByteBufferWriteTest {

    private static int samples = 1;

    @Test
    public void testIntArraysSizeLimit() {
        int intCount = 125000;

        int[] array = new int[intCount];
        for (int j = 0; j < intCount; ++j) {
            array[j] = j;
        }

        long start = System.nanoTime();
        ByteBuffer serializationBuffer = ByteBuffer.allocate(5_000_000);
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            for (int i = 0; i < intCount; i++) {
                serializationBuffer.putInt(array[i]);
            }
        }
        long end = System.nanoTime();

        long deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized IntArray (for-loop) in " + deltaNanosPer + " ns");
        start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            serializationBuffer.asIntBuffer().put(array);
        }

        end = System.nanoTime();
        deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized IntArray (intbuffer) in " + deltaNanosPer + " ns");

        //deserialize
        serializationBuffer.rewind();
        int[] result = new int[intCount];
        serializationBuffer.asIntBuffer().get(result);
        Assert.assertArrayEquals(result, array);
    }


    @Test
    public void testLongArraysSizeLimit() {
        int longCount = 62500;

        long[] array = new long[longCount];
        for (int j = 0; j < longCount; ++j) {
            array[j] = (long) j;
        }

        long start = System.nanoTime();
        ByteBuffer serializationBuffer = ByteBuffer.allocate(5_000_000);
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            for (int i = 0; i < longCount; i++) {
                serializationBuffer.putLong(array[i]);
            }
        }
        long end = System.nanoTime();

        long deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized LongArray (for-loop) in " + deltaNanosPer + " ns");

        start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            serializationBuffer.asLongBuffer().put(array);
        }

        end = System.nanoTime();
        deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized LongArray (longbuffer) in " + deltaNanosPer + " ns");

        serializationBuffer.rewind();
        long[] result = new long[longCount];
        serializationBuffer.asLongBuffer().get(result);
        Assert.assertTrue(Arrays.equals(result, array));
    }

    @Test
    public void testCharacterArraysSizeLimit() {
        int charCount = 250000;

        char[] array = new char[charCount];
        for (int j = 0; j < charCount; ++j) {
            array[j] = (char) j;
        }

        long start = System.nanoTime();
        ByteBuffer serializationBuffer = ByteBuffer.allocate(5_000_000);
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            for (int i = 0; i < charCount; i++) {
                serializationBuffer.putChar(array[i]);
            }
        }
        long end = System.nanoTime();

        long deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized CharArray (for-loop) in " + deltaNanosPer + " ns");

        start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            serializationBuffer.asCharBuffer().put(array);
        }

        end = System.nanoTime();
        deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized CharArray (charbuffer) in " + deltaNanosPer + " ns");

        serializationBuffer.rewind();
        char[] result = new char[charCount];
        serializationBuffer.asCharBuffer().get(result);
        Assert.assertTrue(Arrays.equals(result, array));
    }

    @Test
    public void testByteArraysSizeLimit() {
        int byteCount = 500000;

        byte[] array = new byte[byteCount];
        for (int j = 0; j < byteCount; ++j) {
            array[j] = (byte) j;
        }

        ByteBuffer serializationBuffer = ByteBuffer.allocate(5_000_000);
        long start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            for (int i = 0; i < byteCount; i++) {
                serializationBuffer.put(array[i]);
            }
        }
        long end = System.nanoTime();

        long deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized ByteArray (for-loop) in " + deltaNanosPer + " ns");

        start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            serializationBuffer.put(array);
        }

        end = System.nanoTime();
        deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized ByteArray (bytebuffer) in " + deltaNanosPer + " ns");

        serializationBuffer.rewind();

        //deserialize
        byte[] result = new byte[byteCount];
        serializationBuffer.get(result);
        Assert.assertTrue(Arrays.equals(result, array));
    }


    @Test
    public void testBooleanArraysSizeLimit() {
        int byteCount = 500000;

        boolean[] array = new boolean[byteCount];
        for (int j = 0; j < byteCount; ++j) {
            array[j] = true;
        }

        ByteBuffer serializationBuffer = ByteBuffer.allocate(5_000_000);
        long start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            for (int i = 0; i < byteCount; i++) {
                serializationBuffer.put((byte) (array[i] ? 0x1 : 0x0));
            }
        }
        long end = System.nanoTime();

        long deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized BooleanArray (for-loop) in " + deltaNanosPer + " ns");

        start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            byte[] ser = new byte[byteCount];
            for (int i = 0; i < byteCount; i++)
                ser[i] = (byte) (array[i] ? 0x1 : 0x0);
            serializationBuffer.put(ser);
        }

        end = System.nanoTime();
        deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized BooleanArray (bytebuffer) in " + deltaNanosPer + " ns");

    }

    @Test
    public void testShortArraysSizeLimit() {
        int shortCount = 250000;

        short[] array = new short[shortCount];
        for (int j = 0; j < shortCount; ++j) {
            array[j] = (short) j;
        }

        long start = System.nanoTime();
        ByteBuffer serializationBuffer = ByteBuffer.allocate(5_000_000);
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            for (int i = 0; i < shortCount; i++) {
                serializationBuffer.putShort(array[i]);
            }
        }
        long end = System.nanoTime();

        long deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized ShortArray (for-loop) in " + deltaNanosPer + " ns");

        start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            serializationBuffer.clear();
            serializationBuffer.asShortBuffer().put(array);
        }

        end = System.nanoTime();
        deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized ShortArray (shortbuffer) in " + deltaNanosPer + " ns");

        serializationBuffer.rewind();
        short[] result = new short[shortCount];
        serializationBuffer.asShortBuffer().get(result);
        Assert.assertTrue(Arrays.equals(result, array));
    }

    /**
     * Test the overhead of running the for loop for arrays inside IntArray or Serializer class
     */
    @Test
    public void forloopArrayLocationTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        int arrayCount = 100000;

        TargetIntArray intArray = new TargetIntArray(arrayCount);
        for (int j = 0; j < arrayCount; ++j) {
            intArray.array[j] = (byte) j;
        }
        Method serializeSelf = TargetIntArray.class.getDeclaredMethod("serializeSelf", Class.class, IObjectSerializer.class);
        Method serializeForTest = TargetIntArray.class.getDeclaredMethod("serializeForTest", Class.class, TestObjectSerializer.class);

        ByteBuffer buffer = ByteBuffer.allocate(500_000);
        ByteBufferObjectSerializer objectSerializer = new ByteBufferObjectSerializer(buffer, null, null, null, null);
        TestObjectSerializer testObjectSerializer = new TestObjectSerializer(buffer);
        long start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            buffer.clear();
            serializeSelf.invoke(intArray, null, objectSerializer);
        }
        long end = System.nanoTime();

        long deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized (for-loop ) in " + deltaNanosPer + " ns");

        start = System.nanoTime();
        for (int s = 0; s < samples; ++s) {
            buffer.clear();
            serializeForTest.invoke(intArray, null, testObjectSerializer);
        }
        end = System.nanoTime();

        deltaNanosPer = (end - start) / samples;
        System.out.println("Serialized (for-loop lower level) in " + deltaNanosPer + " ns");

    }
}

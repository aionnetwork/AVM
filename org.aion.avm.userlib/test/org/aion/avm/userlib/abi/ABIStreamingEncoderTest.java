package org.aion.avm.userlib.abi;

import org.junit.Assert;
import org.junit.Test;

public class ABIStreamingEncoderTest {

    @Test
    public void testPrimitiveEncode() {

        byte[] encoded = new byte[ABIStreamingEncoder.BYTE_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneByte((byte) -1);
        Assert.assertArrayEquals(new byte[] {ABIToken.BYTE, -1}, encoded);

        encoded = new byte[ABIStreamingEncoder.CHAR_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneCharacter('a');
        Assert.assertArrayEquals(new byte[] {ABIToken.CHAR, 0, 97}, encoded);

        encoded = new byte[ABIStreamingEncoder.BOOLEAN_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneBoolean(true);
        Assert.assertArrayEquals(new byte[] {ABIToken.BOOLEAN, 1}, encoded);

        encoded = new byte[ABIStreamingEncoder.SHORT_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneShort((short) 1000);
        Assert.assertArrayEquals(new byte[] {ABIToken.SHORT, 3, -24}, encoded);

        encoded = new byte[ABIStreamingEncoder.INT_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneInteger(1000);
        Assert.assertArrayEquals(new byte[] {ABIToken.INT, 0, 0, 3, -24}, encoded);

        encoded = new byte[ABIStreamingEncoder.FLOAT_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneFloat(1000);
        Assert.assertArrayEquals(new byte[] {ABIToken.FLOAT, 68, 122, 0, 0}, encoded);

        encoded = new byte[ABIStreamingEncoder.LONG_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneLong(1000);
        Assert.assertArrayEquals(new byte[] {ABIToken.LONG, 0, 0, 0, 0, 0, 0, 3, -24}, encoded);

        encoded = new byte[ABIStreamingEncoder.DOUBLE_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneDouble(1000);
        Assert.assertArrayEquals(new byte[] {ABIToken.DOUBLE, 64, -113, 64, 0, 0, 0, 0, 0}, encoded);
    }

    @Test
    public void testPrimitiveSymmetry() {

        ABIDecoder decoder;
        byte[] encoded = new byte[ABIStreamingEncoder.BYTE_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneByte((byte) -1);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals((byte)-1, decoder.decodeOneByte());

        encoded = new byte[ABIStreamingEncoder.CHAR_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneCharacter('a');
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals('a', decoder.decodeOneCharacter());

        encoded = new byte[ABIStreamingEncoder.BOOLEAN_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneBoolean(true);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(true, decoder.decodeOneBoolean());

        encoded = new byte[ABIStreamingEncoder.SHORT_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneShort((short) 1000);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals((short)1000, decoder.decodeOneShort());

        encoded = new byte[ABIStreamingEncoder.INT_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneInteger(1000);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000, decoder.decodeOneInteger());

        encoded = new byte[ABIStreamingEncoder.FLOAT_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneFloat(1000);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000.0F, decoder.decodeOneFloat(), 0.1);

        encoded = new byte[ABIStreamingEncoder.LONG_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneLong(1000);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000L, decoder.decodeOneLong());

        encoded = new byte[ABIStreamingEncoder.DOUBLE_ENCODING_LENGTH];
        new ABIStreamingEncoder(encoded).encodeOneDouble(1000);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000.0, decoder.decodeOneDouble(), 0.1);
    }

    @Test
    public void testPrimitiveArray1DEncode() {
        byte[] byteArray = new byte[] { (byte)-1};
        byte[] encoded = new byte[ABIStreamingEncoder.getLengthOfOneByteArray(byteArray)];
        new ABIStreamingEncoder(encoded).encodeOneByteArray(byteArray);
        Assert.assertArrayEquals(new byte[] {ABIToken.A_BYTE, 0, 1, -1}, encoded);

        char[] charArray = new char[] { 'a' };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOneCharacterArray(charArray)];
        new ABIStreamingEncoder(encoded).encodeOneCharacterArray(charArray);
        Assert.assertArrayEquals(new byte[] {ABIToken.A_CHAR, 0, 1, 0, 97}, encoded);

        boolean[] boolArray = new boolean[] { true };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOneBooleanArray(boolArray)];
        new ABIStreamingEncoder(encoded).encodeOneBooleanArray(boolArray);
        Assert.assertArrayEquals(new byte[] {ABIToken.A_BOOLEAN, 0, 1, 1}, encoded);

        short[] shortArray = new short[] { 1000 };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOneShortArray(shortArray)];
        new ABIStreamingEncoder(encoded).encodeOneShortArray(shortArray);
        Assert.assertArrayEquals(new byte[] {ABIToken.A_SHORT, 0, 1, 3, -24}, encoded);

        int[] intArray = new int[] { 1000 };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOneIntegerArray(intArray)];
        new ABIStreamingEncoder(encoded).encodeOneIntegerArray(intArray);
        Assert.assertArrayEquals(new byte[] {ABIToken.A_INT, 0, 1, 0, 0, 3, -24}, encoded);

        float[] floatArray = new float[] { 1000 };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOneFloatArray(floatArray)];
        new ABIStreamingEncoder(encoded).encodeOneFloatArray(floatArray);
        Assert.assertArrayEquals(new byte[] {ABIToken.A_FLOAT, 0, 1, 68, 122, 0, 0}, encoded);

        long[] longArray = new long[] { 1000 };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOneLongArray(longArray)];
        new ABIStreamingEncoder(encoded).encodeOneLongArray(longArray);
        Assert.assertArrayEquals(new byte[] {ABIToken.A_LONG, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24}, encoded);

        double[] doubleArray = new double[] { 1000 };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOneDoubleArray(doubleArray)];
        new ABIStreamingEncoder(encoded).encodeOneDoubleArray(doubleArray);
        Assert.assertArrayEquals(new byte[] {ABIToken.A_DOUBLE, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0}, encoded);
    }

    @Test
    public void testPrimitiveArray2DEncode() {
        byte[][] byteArray = new byte[][] { new byte[] { (byte)-1 }, new byte[] { (byte)-1 } };
        byte[] encoded = new byte[ABIStreamingEncoder.getLengthOfOne2DByteArray(byteArray)];
        new ABIStreamingEncoder(encoded).encodeOne2DByteArray(byteArray);
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_BYTE, 0, 2, ABIToken.A_BYTE, 0, 1, -1, ABIToken.A_BYTE, 0, 1, -1 }, encoded);

        char[][] charArray = new char[][] { new char[] { 'a' }, new char[] { 'a' } };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOne2DCharacterArray(charArray)];
        new ABIStreamingEncoder(encoded).encodeOne2DCharacterArray(charArray);
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_CHAR, 0, 2, ABIToken.A_CHAR, 0, 1, 0, 97, ABIToken.A_CHAR, 0, 1, 0, 97 }, encoded);

        boolean[][] booleanArray = new boolean[][] { new boolean[] { true }, new boolean[] { true } };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOne2DBooleanArray(booleanArray)];
        new ABIStreamingEncoder(encoded).encodeOne2DBooleanArray(booleanArray);
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_BOOLEAN, 0, 2, ABIToken.A_BOOLEAN, 0, 1, 1, ABIToken.A_BOOLEAN, 0, 1, 1 }, encoded);

        short[][] shortArray = new short[][] { new short[] { (short)1000 }, new short[] { (short)1000 } };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOne2DShortArray(shortArray)];
        new ABIStreamingEncoder(encoded).encodeOne2DShortArray(shortArray);
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_SHORT, 0, 2, ABIToken.A_SHORT, 0, 1, 3, -24, ABIToken.A_SHORT, 0, 1, 3, -24 }, encoded);

        int[][] intArray = new int[][] { new int[] { 1000 }, new int[] { 1000 } };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOne2DIntegerArray(intArray)];
        new ABIStreamingEncoder(encoded).encodeOne2DIntegerArray(intArray);
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_INT, 0, 2, ABIToken.A_INT, 0, 1, 0, 0, 3, -24, ABIToken.A_INT, 0, 1, 0, 0, 3, -24 }, encoded);

        float[][] floatArray = new float[][] { new float[] { 1000.0F }, new float[] { 1000.0F } };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOne2DFloatArray(floatArray)];
        new ABIStreamingEncoder(encoded).encodeOne2DFloatArray(floatArray);
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_FLOAT, 0, 2, ABIToken.A_FLOAT, 0, 1, 68, 122, 0, 0, ABIToken.A_FLOAT, 0, 1, 68, 122, 0, 0 }, encoded);

        long[][] longArray = new long[][] { new long[] { 1000L }, new long[] { 1000L } };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOne2DLongArray(longArray)];
        new ABIStreamingEncoder(encoded).encodeOne2DLongArray(longArray);
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_LONG, 0, 2, ABIToken.A_LONG, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24, ABIToken.A_LONG, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24 }, encoded);

        double[][] doubleArray = new double[][] { new double[] { 1000.0 }, new double[] { 1000.0 } };
        encoded = new byte[ABIStreamingEncoder.getLengthOfOne2DDoubleArray(doubleArray)];
        new ABIStreamingEncoder(encoded).encodeOne2DDoubleArray(doubleArray);
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_DOUBLE, 0, 2, ABIToken.A_DOUBLE, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0, ABIToken.A_DOUBLE, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0 }, encoded);
    }

    @Test
    public void testNullPrimitiveArray1DEncodings() {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] encoded = encoder.encodeOneByteArray(null).toBytes();
        byte[] expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_BYTE
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneByteArray(null));

        encoded = encoder.encodeOneBooleanArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_BOOLEAN
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneBooleanArray(null));

        encoded = encoder.encodeOneBooleanArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_BOOLEAN
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneBooleanArray(null));

        encoded = encoder.encodeOneCharacterArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_CHAR
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneByteArray(null));

        encoded = encoder.encodeOneShortArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_SHORT
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneShortArray(null));

        encoded = encoder.encodeOneIntegerArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_INT
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneIntegerArray(null));

        encoded = encoder.encodeOneLongArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_LONG
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneLongArray(null));

        encoded = encoder.encodeOneFloatArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_FLOAT
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneFloatArray(null));

        encoded = encoder.encodeOneDoubleArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_DOUBLE
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneDoubleArray(null));

    }

    @Test
    public void testNullStringAddressEncodings() {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] encoded = encoder.encodeOneString(null).toBytes();
        byte[] expected = new byte[] {
            ABIToken.NULL,
            ABIToken.STRING
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneString(null));

        encoded = encoder.encodeOneAddress(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ADDRESS
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOneAddress(null));
    }

    @Test
    public void testNullPrimitiveArray2DEncodings() {
        ABIStreamingEncoder encoder = new ABIStreamingEncoder();
        byte[] encoded = encoder.encodeOne2DByteArray(null).toBytes();
        byte[] expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_BYTE
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOne2DByteArray(null));

        encoded = encoder.encodeOne2DBooleanArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_BOOLEAN
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOne2DBooleanArray(null));

        encoded = encoder.encodeOne2DCharacterArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_CHAR
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOne2DCharacterArray(null));

        encoded = encoder.encodeOne2DShortArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_SHORT
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOne2DShortArray(null));

        encoded = encoder.encodeOne2DIntegerArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_INT
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOne2DIntegerArray(null));

        encoded = encoder.encodeOne2DFloatArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_FLOAT
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOne2DFloatArray(null));

        encoded = encoder.encodeOne2DLongArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_LONG
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOne2DLongArray(null));

        encoded = encoder.encodeOne2DDoubleArray(null).toBytes();
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_DOUBLE
        };
        Assert.assertArrayEquals(expected, encoded);
        Assert.assertEquals(expected.length, ABIStreamingEncoder.getLengthOfOne2DDoubleArray(null));

    }
}

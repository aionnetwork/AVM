package org.aion.avm.userlib.abi;

import org.junit.Assert;
import org.junit.Test;

public class ABIEncoderTest {

    @Test
    public void testPrimitiveEncode() {
        byte[] encoded = ABIEncoder.encodeOneByte((byte) -1);
        Assert.assertArrayEquals(new byte[] {ABIToken.BYTE, -1}, encoded);

        encoded = ABIEncoder.encodeOneCharacter('a');
        Assert.assertArrayEquals(new byte[] {ABIToken.CHAR, 0, 97}, encoded);

        encoded = ABIEncoder.encodeOneBoolean(true);
        Assert.assertArrayEquals(new byte[] {ABIToken.BOOLEAN, 1}, encoded);

        encoded = ABIEncoder.encodeOneShort((short) 1000);
        Assert.assertArrayEquals(new byte[] {ABIToken.SHORT, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneInteger(1000);
        Assert.assertArrayEquals(new byte[] {ABIToken.INT, 0, 0, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneFloat(1000.0F);
        Assert.assertArrayEquals(new byte[] {ABIToken.FLOAT, 68, 122, 0, 0}, encoded);

        encoded = ABIEncoder.encodeOneLong(1000L);
        Assert.assertArrayEquals(new byte[] {ABIToken.LONG, 0, 0, 0, 0, 0, 0, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneDouble(1000.0);
        Assert.assertArrayEquals(new byte[] {ABIToken.DOUBLE, 64, -113, 64, 0, 0, 0, 0, 0}, encoded);
    }

    @Test
    public void testPrimitiveSymmetry() {

        ABIDecoder decoder;
        byte[] encoded = ABIEncoder.encodeOneByte((byte) -1);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals((byte)-1, decoder.decodeOneByte());

        encoded = ABIEncoder.encodeOneCharacter('a');
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals('a', decoder.decodeOneCharacter());

        encoded = ABIEncoder.encodeOneBoolean(true);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(true, decoder.decodeOneBoolean());

        encoded = ABIEncoder.encodeOneShort((short) 1000);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals((short)1000, decoder.decodeOneShort());

        encoded = ABIEncoder.encodeOneInteger(1000);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000, decoder.decodeOneInteger());

        encoded = ABIEncoder.encodeOneFloat(1000.0F);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000.0F, decoder.decodeOneFloat(), 0.1);

        encoded = ABIEncoder.encodeOneLong(1000L);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000L, decoder.decodeOneLong());

        encoded = ABIEncoder.encodeOneDouble(1000.0);
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000.0, decoder.decodeOneDouble(), 0.1);
    }

    @Test
    public void testPrimitiveArray1DEncode() {
        byte[] encoded = ABIEncoder.encodeOneByteArray(new byte[] { (byte)-1} );
        Assert.assertArrayEquals(new byte[] {ABIToken.A_BYTE, 0, 1, -1}, encoded);

        encoded = ABIEncoder.encodeOneCharacterArray(new char[] { 'a' });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_CHAR, 0, 1, 0, 97}, encoded);

        encoded = ABIEncoder.encodeOneBooleanArray(new boolean[] { true });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_BOOLEAN, 0, 1, 1}, encoded);

        encoded = ABIEncoder.encodeOneShortArray(new short[] { (short)1000 });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_SHORT, 0, 1, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneIntegerArray(new int[] { 1000 });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_INT, 0, 1, 0, 0, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneFloatArray(new float[] { 1000.0F });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_FLOAT, 0, 1, 68, 122, 0, 0}, encoded);

        encoded = ABIEncoder.encodeOneLongArray(new long[] { 1000L });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_LONG, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneDoubleArray(new double[] { 1000.0 });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_DOUBLE, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0}, encoded);
    }

    @Test
    public void testPrimitiveArray2DEncode() {
        byte[] encoded = ABIEncoder.encodeOne2DByteArray(new byte[][] { new byte[] { (byte)-1 }, new byte[] { (byte)-1 } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_BYTE, 0, 2, ABIToken.A_BYTE, 0, 1, -1, ABIToken.A_BYTE, 0, 1, -1 }, encoded);

        encoded = ABIEncoder.encodeOne2DCharacterArray(new char[][] { new char[] { 'a' }, new char[] { 'a' } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_CHAR, 0, 2, ABIToken.A_CHAR, 0, 1, 0, 97, ABIToken.A_CHAR, 0, 1, 0, 97 }, encoded);

        encoded = ABIEncoder.encodeOne2DBooleanArray(new boolean[][] { new boolean[] { true }, new boolean[] { true } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_BOOLEAN, 0, 2, ABIToken.A_BOOLEAN, 0, 1, 1, ABIToken.A_BOOLEAN, 0, 1, 1 }, encoded);

        encoded = ABIEncoder.encodeOne2DShortArray(new short[][] { new short[] { (short)1000 }, new short[] { (short)1000 } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_SHORT, 0, 2, ABIToken.A_SHORT, 0, 1, 3, -24, ABIToken.A_SHORT, 0, 1, 3, -24 }, encoded);

        encoded = ABIEncoder.encodeOne2DIntegerArray(new int[][] { new int[] { 1000 }, new int[] { 1000 } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_INT, 0, 2, ABIToken.A_INT, 0, 1, 0, 0, 3, -24, ABIToken.A_INT, 0, 1, 0, 0, 3, -24 }, encoded);

        encoded = ABIEncoder.encodeOne2DFloatArray(new float[][] { new float[] { 1000.0F }, new float[] { 1000.0F } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_FLOAT, 0, 2, ABIToken.A_FLOAT, 0, 1, 68, 122, 0, 0, ABIToken.A_FLOAT, 0, 1, 68, 122, 0, 0 }, encoded);

        encoded = ABIEncoder.encodeOne2DLongArray(new long[][] { new long[] { 1000L }, new long[] { 1000L } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_LONG, 0, 2, ABIToken.A_LONG, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24, ABIToken.A_LONG, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24 }, encoded);

        encoded = ABIEncoder.encodeOne2DDoubleArray(new double[][] { new double[] { 1000.0 }, new double[] { 1000.0 } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY, ABIToken.A_DOUBLE, 0, 2, ABIToken.A_DOUBLE, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0, ABIToken.A_DOUBLE, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0 }, encoded);
    }

    @Test
    public void testNullPrimitiveArray1DEncodings() {
        byte[] encoded = ABIEncoder.encodeOneByteArray(null);
        byte[] expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_BYTE
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOneBooleanArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_BOOLEAN
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOneBooleanArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_BOOLEAN
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOneCharacterArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_CHAR
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOneShortArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_SHORT
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOneIntegerArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_INT
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOneLongArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_LONG
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOneFloatArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_FLOAT
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOneDoubleArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.A_DOUBLE
        };
        Assert.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testNullStringAddressEncodings() {
        byte[] encoded = ABIEncoder.encodeOneString(null);
        byte[] expected = new byte[] {
            ABIToken.NULL,
            ABIToken.STRING
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOneAddress(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ADDRESS
        };
        Assert.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testNullPrimitiveArray2DEncodings() {
        byte[] encoded = ABIEncoder.encodeOne2DByteArray(null);
        byte[] expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_BYTE
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOne2DBooleanArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_BOOLEAN
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOne2DCharacterArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_CHAR
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOne2DShortArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_SHORT
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOne2DIntegerArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_INT
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOne2DFloatArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_FLOAT
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOne2DLongArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_LONG
        };
        Assert.assertArrayEquals(expected, encoded);

        encoded = ABIEncoder.encodeOne2DDoubleArray(null);
        expected = new byte[] {
            ABIToken.NULL,
            ABIToken.ARRAY,
            ABIToken.A_DOUBLE
        };
        Assert.assertArrayEquals(expected, encoded);
    }
}

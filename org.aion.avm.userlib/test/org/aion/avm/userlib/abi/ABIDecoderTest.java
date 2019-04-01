package org.aion.avm.userlib.abi;

import org.junit.Assert;
import org.junit.Test;

public class ABIDecoderTest {

    @Test
    public void testPrimitiveDecode() {

        ABIDecoder decoder;

        byte[] encoded = new byte[] {ABIToken.BYTE.identifier, -1};
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals((byte)-1, decoder.decodeOneByte());
        
        encoded = new byte[] {ABIToken.CHAR.identifier, 0, 97};
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals('a', decoder.decodeOneCharacter());
        
        encoded = new byte[] {ABIToken.BOOLEAN.identifier, 1};
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(true, decoder.decodeOneBoolean());
        
        encoded = new byte[] {ABIToken.SHORT.identifier, 3, -24};
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals((short)1000, decoder.decodeOneShort());
        
        encoded = new byte[] {ABIToken.INT.identifier, 0, 0, 3, -24};
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000, decoder.decodeOneInteger());
        
        encoded = new byte[] {ABIToken.FLOAT.identifier, 68, 122, 0, 0};
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000.0F, decoder.decodeOneFloat(), 0.1);
        
        encoded = new byte[] {ABIToken.LONG.identifier, 0, 0, 0, 0, 0, 0, 3, -24};
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000L, decoder.decodeOneLong());
        
        encoded = new byte[] {ABIToken.DOUBLE.identifier, 64, -113, 64, 0, 0, 0, 0, 0};
        decoder = new ABIDecoder(encoded);
        Assert.assertEquals(1000.0, decoder.decodeOneDouble(), 0.1);
    }

    @Test
    public void testPrimitiveArray1DDecode() {

        ABIDecoder decoder;

        byte[] encoded = new byte[] {ABIToken.A_BYTE.identifier, 0, 1, -1};
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new byte[] { (byte)-1 }, decoder.decodeOneByteArray());
        
        encoded = new byte[] {ABIToken.A_CHAR.identifier, 0, 1, 0, 97};
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new char[] { 'a' }, decoder.decodeOneCharacterArray());
        
        encoded = new byte[] {ABIToken.A_BOOLEAN.identifier, 0, 1, 1};
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new boolean[] { true }, decoder.decodeOneBooleanArray());
        
        encoded = new byte[] {ABIToken.A_SHORT.identifier, 0, 1, 3, -24};
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new short[] { (short)1000 }, decoder.decodeOneShortArray());
        
        encoded = new byte[] {ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24};
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new int[] { 1000 }, decoder.decodeOneIntegerArray());
        
        encoded = new byte[] {ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0};
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new float[] { 1000.0F }, decoder.decodeOneFloatArray(), 0.0F);
        
        encoded = new byte[] {ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24};
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new long[] { 1000L }, decoder.decodeOneLongArray());

        encoded = new byte[] {ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0};
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new double[] { 1000.0 }, decoder.decodeOneDoubleArray(), 0.0);
    }

    @Test
    public void testPrimitiveArray2DDecode() {

        ABIDecoder decoder;

        byte[] encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BYTE.identifier, 0, 2, ABIToken.A_BYTE.identifier, 0, 1, -1, ABIToken.A_BYTE.identifier, 0, 1, -1 };
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new byte[][] { new byte[] { (byte)-1 }, new byte[] { (byte)-1 } } , decoder.decodeOne2DByteArray());
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_CHAR.identifier, 0, 2, ABIToken.A_CHAR.identifier, 0, 1, 0, 97, ABIToken.A_CHAR.identifier, 0, 1, 0, 97 };
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new char[][] { new char[] { 'a' }, new char[] { 'a' } } , decoder.decodeOne2DCharacterArray());
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BOOLEAN.identifier, 0, 2, ABIToken.A_BOOLEAN.identifier, 0, 1, 1, ABIToken.A_BOOLEAN.identifier, 0, 1, 1 };
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new boolean[][] { new boolean[] { true }, new boolean[] { true } }, decoder.decodeOne2DBooleanArray());
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_SHORT.identifier, 0, 2, ABIToken.A_SHORT.identifier, 0, 1, 3, -24, ABIToken.A_SHORT.identifier, 0, 1, 3, -24 };
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new short[][] { new short[] { (short)1000 }, new short[] { (short)1000 } } , decoder.decodeOne2DShortArray());
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_INT.identifier, 0, 2, ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24, ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24 };
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new int[][] { new int[] { 1000 }, new int[] { 1000 } } , decoder.decodeOne2DIntegerArray());
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_FLOAT.identifier, 0, 2, ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0, ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0 };
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new float[] { 1000.0F } , decoder.decodeOne2DFloatArray()[0], 0.0F);
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_LONG.identifier, 0, 2, ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24, ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24 };
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new long[][] { new long[] { 1000L }, new long[] { 1000L } } , decoder.decodeOne2DLongArray());
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_DOUBLE.identifier, 0, 2, ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0, ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0 };
        decoder = new ABIDecoder(encoded);
        Assert.assertArrayEquals(new double[] { 1000.0 }, decoder.decodeOne2DDoubleArray()[0], 0.0);
    }

    @Test
    public void testNullPrimitiveArray1DDecodings() {

        ABIDecoder decoder;
        ABIException caught = null;

        // Byte Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneByteArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        byte[] encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.A_BYTE.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneByteArray());

        // Boolean Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneBooleanArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.A_BOOLEAN.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneBooleanArray());

        // Character Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneCharacterArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.A_CHAR.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneCharacterArray());

        // Short Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneShortArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.A_SHORT.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneShortArray());

        // Integer Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneIntegerArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.A_INT.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneIntegerArray());

        // Long Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneLongArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.A_LONG.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneLongArray());

        // Float Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneFloatArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.A_FLOAT.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneFloatArray());

        // Double Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneDoubleArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.A_DOUBLE.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneDoubleArray());
    }

    @Test
    public void testNullStringAddressDecodings() {

        ABIDecoder decoder;
        ABIException caught = null;

        // String Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneString();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        byte[] encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.STRING.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneString());

        // Address Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOneAddress();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.ADDRESS.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOneAddress());
    }

    @Test
    public void testNullPrimitiveArray2DDecodings() {

        ABIDecoder decoder;
        ABIException caught = null;

        // 2D Byte Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOne2DByteArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        byte[] encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.A_BYTE.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOne2DByteArray());

        // 2D Boolean Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOne2DBooleanArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.A_BOOLEAN.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOne2DBooleanArray());

        // 2D Character Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOne2DCharacterArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.A_CHAR.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOne2DCharacterArray());

        // 2D Short Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOne2DShortArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.A_SHORT.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOne2DShortArray());

        // 2D Integer Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOne2DIntegerArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.A_INT.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOne2DIntegerArray());

        // 2D Long Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOne2DLongArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.A_LONG.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOne2DLongArray());

        // 2D Float Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOne2DFloatArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.A_FLOAT.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOne2DFloatArray());

        // 2D Double Array

        try {
            decoder = new ABIDecoder(null);
            decoder.decodeOne2DDoubleArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);

        encoded = new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.A_DOUBLE.identifier };
        decoder = new ABIDecoder(encoded);
        Assert.assertNull(decoder.decodeOne2DDoubleArray());
    }

    @Test
    public void testPrimitiveAsymmetry() {

        ABIDecoder decoder;
        ABIException caught = null;
        byte[] encoded = ABIEncoder.encodeOneByte((byte) -1);
        decoder = new ABIDecoder(encoded);
        try {
            decoder.decodeOneString();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = ABIEncoder.encodeOneCharacter('a');
        decoder = new ABIDecoder(encoded);
        try {
            decoder.decodeOneByteArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = ABIEncoder.encodeOneBoolean(true);
        decoder = new ABIDecoder(encoded);
        try {
            decoder.decodeOneAddress();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = ABIEncoder.encodeOneShort((short) 1000);
        decoder = new ABIDecoder(encoded);
        try {
            decoder.decodeOne2DShortArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = ABIEncoder.encodeOneInteger(1000);
        decoder = new ABIDecoder(encoded);
        try {
            decoder.decodeOneCharacter();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = ABIEncoder.encodeOneFloat(1000.0F);
        decoder = new ABIDecoder(encoded);
        try {
            decoder.decodeOneStringArray();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = ABIEncoder.encodeOneLong(1000L);
        decoder = new ABIDecoder(encoded);
        try {
            decoder.decodeOneInteger();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;

        encoded = ABIEncoder.encodeOneDouble(1000.0);
        decoder = new ABIDecoder(encoded);
        try {
            decoder.decodeOneString();
        } catch (ABIException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
    }
}

package org.aion.avm.userlib.abi;

import org.junit.Assert;
import org.junit.Test;

public class ABIEncoderTest {

    @Test
    public void testPrimitiveEncode() {
        byte[] encoded = ABIEncoder.encodeOneByte((byte) -1);
        Assert.assertArrayEquals(new byte[] {ABIToken.BYTE.identifier, -1}, encoded);

        encoded = ABIEncoder.encodeOneCharacter('a');
        Assert.assertArrayEquals(new byte[] {ABIToken.CHAR.identifier, 0, 97}, encoded);

        encoded = ABIEncoder.encodeOneBoolean(true);
        Assert.assertArrayEquals(new byte[] {ABIToken.BOOLEAN.identifier, 1}, encoded);

        encoded = ABIEncoder.encodeOneShort((short) 1000);
        Assert.assertArrayEquals(new byte[] {ABIToken.SHORT.identifier, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneInteger(1000);
        Assert.assertArrayEquals(new byte[] {ABIToken.INT.identifier, 0, 0, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneFloat(1000.0F);
        Assert.assertArrayEquals(new byte[] {ABIToken.FLOAT.identifier, 68, 122, 0, 0}, encoded);

        encoded = ABIEncoder.encodeOneLong(1000L);
        Assert.assertArrayEquals(new byte[] {ABIToken.LONG.identifier, 0, 0, 0, 0, 0, 0, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneDouble(1000.0);
        Assert.assertArrayEquals(new byte[] {ABIToken.DOUBLE.identifier, 64, -113, 64, 0, 0, 0, 0, 0}, encoded);
    }

    @Test
    public void testPrimitiveDecode() {
        byte[] encoded = new byte[] {ABIToken.BYTE.identifier, -1};
        Assert.assertEquals((byte)-1, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.CHAR.identifier, 0, 97};
        Assert.assertEquals('a', ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.BOOLEAN.identifier, 1};
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.SHORT.identifier, 3, -24};
        Assert.assertEquals((short)1000, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.INT.identifier, 0, 0, 3, -24};
        Assert.assertEquals(1000, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.FLOAT.identifier, 68, 122, 0, 0};
        Assert.assertEquals(1000.0F, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.LONG.identifier, 0, 0, 0, 0, 0, 0, 3, -24};
        Assert.assertEquals(1000L, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.DOUBLE.identifier, 64, -113, 64, 0, 0, 0, 0, 0};
        Assert.assertEquals(1000.0, ABIDecoder.decodeOneObject(encoded));
    }

    @Test
    public void testPrimitiveSymmetry() {
        byte[] encoded = ABIEncoder.encodeOneByte((byte) -1);
        Assert.assertEquals((byte)-1, ABIDecoder.decodeOneObject(encoded));

        encoded = ABIEncoder.encodeOneCharacter('a');
        Assert.assertEquals('a', ABIDecoder.decodeOneObject(encoded));

        encoded = ABIEncoder.encodeOneBoolean(true);
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(encoded));

        encoded = ABIEncoder.encodeOneShort((short) 1000);
        Assert.assertEquals((short)1000, ABIDecoder.decodeOneObject(encoded));

        encoded = ABIEncoder.encodeOneInteger(1000);
        Assert.assertEquals(1000, ABIDecoder.decodeOneObject(encoded));

        encoded = ABIEncoder.encodeOneFloat(1000.0F);
        Assert.assertEquals(1000.0F, ABIDecoder.decodeOneObject(encoded));

        encoded = ABIEncoder.encodeOneLong(1000L);
        Assert.assertEquals(1000L, ABIDecoder.decodeOneObject(encoded));

        encoded = ABIEncoder.encodeOneDouble(1000.0);
        Assert.assertEquals(1000.0, ABIDecoder.decodeOneObject(encoded));
    }

    @Test
    public void testPrimitiveArray1Encode() {
        byte[] encoded = ABIEncoder.encodeOneByteArray(new byte[] { (byte)-1} );
        Assert.assertArrayEquals(new byte[] {ABIToken.A_BYTE.identifier, 0, 1, -1}, encoded);

        encoded = ABIEncoder.encodeOneCharacterArray(new char[] { 'a' });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_CHAR.identifier, 0, 1, 0, 97}, encoded);

        encoded = ABIEncoder.encodeOneBooleanArray(new boolean[] { true });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_BOOLEAN.identifier, 0, 1, 1}, encoded);

        encoded = ABIEncoder.encodeOneShortArray(new short[] { (short)1000 });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_SHORT.identifier, 0, 1, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneIntegerArray(new int[] { 1000 });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneFloatArray(new float[] { 1000.0F });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0}, encoded);

        encoded = ABIEncoder.encodeOneLongArray(new long[] { 1000L });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24}, encoded);

        encoded = ABIEncoder.encodeOneDoubleArray(new double[] { 1000.0 });
        Assert.assertArrayEquals(new byte[] {ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0}, encoded);
    }

    @Test
    public void testPrimitiveArray1Decode() {
        byte[] encoded = new byte[] {ABIToken.A_BYTE.identifier, 0, 1, -1};
        Assert.assertArrayEquals(new byte[] { (byte)-1 }, (byte[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.A_CHAR.identifier, 0, 1, 0, 97};
        Assert.assertArrayEquals(new char[] { 'a' }, (char[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.A_BOOLEAN.identifier, 0, 1, 1};
        Assert.assertArrayEquals(new boolean[] { true }, (boolean[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.A_SHORT.identifier, 0, 1, 3, -24};
        Assert.assertArrayEquals(new short[] { (short)1000 }, (short[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24};
        Assert.assertArrayEquals(new int[] { 1000 }, (int[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0};
        Assert.assertArrayEquals(new float[] { 1000.0F }, (float[]) ABIDecoder.decodeOneObject(encoded), 0.0F);
        
        encoded = new byte[] {ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24};
        Assert.assertArrayEquals(new long[] { 1000L }, (long[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0};
        Assert.assertArrayEquals(new double[] { 1000.0 }, (double[]) ABIDecoder.decodeOneObject(encoded), 0.0);
    }

    @Test
    public void testPrimitiveArray2Encode() {
        byte[] encoded = ABIEncoder.encodeOne2DByteArray(new byte[][] { new byte[] { (byte)-1 }, new byte[] { (byte)-1 } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BYTE.identifier, 0, 2, ABIToken.A_BYTE.identifier, 0, 1, -1, ABIToken.A_BYTE.identifier, 0, 1, -1 }, encoded);

        encoded = ABIEncoder.encodeOne2DCharacterArray(new char[][] { new char[] { 'a' }, new char[] { 'a' } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_CHAR.identifier, 0, 2, ABIToken.A_CHAR.identifier, 0, 1, 0, 97, ABIToken.A_CHAR.identifier, 0, 1, 0, 97 }, encoded);

        encoded = ABIEncoder.encodeOne2DBooleanArray(new boolean[][] { new boolean[] { true }, new boolean[] { true } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BOOLEAN.identifier, 0, 2, ABIToken.A_BOOLEAN.identifier, 0, 1, 1, ABIToken.A_BOOLEAN.identifier, 0, 1, 1 }, encoded);

        encoded = ABIEncoder.encodeOne2DShortArray(new short[][] { new short[] { (short)1000 }, new short[] { (short)1000 } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_SHORT.identifier, 0, 2, ABIToken.A_SHORT.identifier, 0, 1, 3, -24, ABIToken.A_SHORT.identifier, 0, 1, 3, -24 }, encoded);

        encoded = ABIEncoder.encodeOne2DIntegerArray(new int[][] { new int[] { 1000 }, new int[] { 1000 } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_INT.identifier, 0, 2, ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24, ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24 }, encoded);

        encoded = ABIEncoder.encodeOne2DFloatArray(new float[][] { new float[] { 1000.0F }, new float[] { 1000.0F } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_FLOAT.identifier, 0, 2, ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0, ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0 }, encoded);

        encoded = ABIEncoder.encodeOne2DLongArray(new long[][] { new long[] { 1000L }, new long[] { 1000L } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_LONG.identifier, 0, 2, ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24, ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24 }, encoded);

        encoded = ABIEncoder.encodeOne2DDoubleArray(new double[][] { new double[] { 1000.0 }, new double[] { 1000.0 } } );
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_DOUBLE.identifier, 0, 2, ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0, ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0 }, encoded);
    }

    @Test
    public void testPrimitiveArray2Decode() {
        byte[] encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BYTE.identifier, 0, 2, ABIToken.A_BYTE.identifier, 0, 1, -1, ABIToken.A_BYTE.identifier, 0, 1, -1 };
        Assert.assertArrayEquals(new byte[][] { new byte[] { (byte)-1 }, new byte[] { (byte)-1 } } , (byte[][]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_CHAR.identifier, 0, 2, ABIToken.A_CHAR.identifier, 0, 1, 0, 97, ABIToken.A_CHAR.identifier, 0, 1, 0, 97 };
        Assert.assertArrayEquals(new char[][] { new char[] { 'a' }, new char[] { 'a' } } , (char[][]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BOOLEAN.identifier, 0, 2, ABIToken.A_BOOLEAN.identifier, 0, 1, 1, ABIToken.A_BOOLEAN.identifier, 0, 1, 1 };
        Assert.assertArrayEquals(new boolean[][] { new boolean[] { true }, new boolean[] { true } }, (boolean[][]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_SHORT.identifier, 0, 2, ABIToken.A_SHORT.identifier, 0, 1, 3, -24, ABIToken.A_SHORT.identifier, 0, 1, 3, -24 };
        Assert.assertArrayEquals(new short[][] { new short[] { (short)1000 }, new short[] { (short)1000 } } , (short[][]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_INT.identifier, 0, 2, ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24, ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24 };
        Assert.assertArrayEquals(new int[][] { new int[] { 1000 }, new int[] { 1000 } } , (int[][]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_FLOAT.identifier, 0, 2, ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0, ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0 };
        Assert.assertArrayEquals(new float[] { 1000.0F } , ((float[][]) ABIDecoder.decodeOneObject(encoded))[0], 0.0F);
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_LONG.identifier, 0, 2, ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24, ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24 };
        Assert.assertArrayEquals(new long[][] { new long[] { 1000L }, new long[] { 1000L } } , (long[][]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_DOUBLE.identifier, 0, 2, ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0, ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0 };
        Assert.assertArrayEquals(new double[] { 1000.0 }, ((double[][]) ABIDecoder.decodeOneObject(encoded))[0], 0.0);
    }

    @Test
    public void testMethodCallEncoding() {
        byte[] encoded = ABIEncoder.encodeMethodArguments("method", 123, (byte)-1, "hello");
        byte[] expected = new byte[] {
                ABIToken.STRING.identifier, 0, 6, 0x6d, 0x65, 0x74, 0x68, 0x6f, 0x64,
                ABIToken.INT.identifier, 0x00, 0x00, 0x00, 0x7b,
                ABIToken.BYTE.identifier, (byte)0xff,
                ABIToken.STRING.identifier, 0, 5, 0x68, 0x65, 0x6c, 0x6c, 0x6f,
        };
        Assert.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testDeploymentArgsEncoding() {
        byte[] encoded = ABIEncoder.encodeDeploymentArguments(123, (byte)-1, "hello");
        byte[] expected = new byte[] {
            ABIToken.INT.identifier, 0x00, 0x00, 0x00, 0x7b,
            ABIToken.BYTE.identifier, (byte)0xff,
            ABIToken.STRING.identifier, 0, 5, 0x68, 0x65, 0x6c, 0x6c, 0x6f,
        };
        Assert.assertArrayEquals(expected, encoded);
    }

    @Test
    public void testNullArguments() {
        NullPointerException caught = null;
        try {
            // Note that this is a warning since you can't pass null as varargs array (we are just testing that we fail in the expected way).
            ABIEncoder.encodeMethodArguments("", (Object[]) null);
        } catch (NullPointerException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;
        try {
            ABIEncoder.encodeMethodArguments(null);
        } catch (NullPointerException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);

        caught = null;
        try {
            ABIDecoder.decodeOneObject(null);
        } catch (NullPointerException e) {
            caught = e;
        }
        Assert.assertNotNull(caught);
        caught = null;
    }
}

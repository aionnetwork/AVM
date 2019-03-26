package org.aion.avm.userlib.abi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.aion.avm.api.Address;
import org.junit.Assert;
import org.junit.Test;


public class ABICodecTest {

    @Test
    public void testPrimitiveEncode() throws Exception {
        byte[] encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Byte.class, (byte)-1)));
        Assert.assertArrayEquals(new byte[] { ABIToken.BYTE.identifier, -1 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Character.class, 'a')));
        Assert.assertArrayEquals(new byte[] { ABIToken.CHAR.identifier, 0, 97 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Boolean.class, true)));
        Assert.assertArrayEquals(new byte[] { ABIToken.BOOLEAN.identifier, 1 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Short.class, (short)1000)));
        Assert.assertArrayEquals(new byte[] { ABIToken.SHORT.identifier, 3, -24 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Integer.class, 1000)));
        Assert.assertArrayEquals(new byte[] { ABIToken.INT.identifier, 0, 0, 3, -24 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Float.class, 1000.0F)));
        Assert.assertArrayEquals(new byte[] { ABIToken.FLOAT.identifier, 68, 122, 0, 0 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Long.class, 1000L)));
        Assert.assertArrayEquals(new byte[] { ABIToken.LONG.identifier, 0, 0, 0, 0, 0, 0, 3, -24 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Double.class, 1000.0)));
        Assert.assertArrayEquals(new byte[] { ABIToken.DOUBLE.identifier, 64, -113, 64, 0, 0, 0, 0, 0 }, encoded);
    }

    @Test
    public void testPrimitiveDecode() throws Exception {
        List<ABICodec.Tuple> elements = ABICodec.parseEverything(new byte[] { ABIToken.BYTE.identifier, -1 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals((byte)-1, elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.CHAR.identifier, 0, 97 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals('a', elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.BOOLEAN.identifier, 1 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals(true, elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.SHORT.identifier, 3, -24 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals((short)1000, elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.INT.identifier, 0, 0, 3, -24 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals(1000, elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.FLOAT.identifier, 68, 122, 0, 0 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals(1000.0F, elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.LONG.identifier, 0, 0, 0, 0, 0, 0, 3, -24 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals(1000L, elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.DOUBLE.identifier, 64, -113, 64, 0, 0, 0, 0, 0 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals(1000.0, elements.get(0).value);
    }

    @Test
    public void testPrimitiveArrayEncode() throws Exception {
        byte[] encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(byte[].class, new byte[] { (byte)-1 })));
        Assert.assertArrayEquals(new byte[] { ABIToken.A_BYTE.identifier, 0, 1, -1 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(char[].class, new char[] { 'a' })));
        Assert.assertArrayEquals(new byte[] { ABIToken.A_CHAR.identifier, 0, 1, 0, 97 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(boolean[].class, new boolean[] { true })));
        Assert.assertArrayEquals(new byte[] { ABIToken.A_BOOLEAN.identifier, 0, 1, 1 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(short[].class, new short[] { (short)1000 })));
        Assert.assertArrayEquals(new byte[] { ABIToken.A_SHORT.identifier, 0, 1, 3, -24 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(int[].class, new int[] { 1000 })));
        Assert.assertArrayEquals(new byte[] { ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(float[].class, new float[] { 1000.0F })));
        Assert.assertArrayEquals(new byte[] { ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(long[].class, new long[] { 1000L })));
        Assert.assertArrayEquals(new byte[] { ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(double[].class, new double[] { 1000.0 })));
        Assert.assertArrayEquals(new byte[] { ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0 }, encoded);
    }

    @Test
    public void testPrimitiveArrayDecode() throws Exception {
        List<ABICodec.Tuple> elements = ABICodec.parseEverything(new byte[] { ABIToken.A_BYTE.identifier, 0, 1, -1 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new byte[] { (byte)-1 }, (byte[])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.A_CHAR.identifier, 0, 1, 0, 97 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new char[] { 'a' }, (char[])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.A_BOOLEAN.identifier, 0, 1, 1 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new boolean[] { true }, (boolean[])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.A_SHORT.identifier, 0, 1, 3, -24 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new short[] { (short)1000 }, (short[])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new int[] { 1000 }, (int[])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new float[] { 1000.0F }, (float[])elements.get(0).value, 0.0F);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new long[] { 1000L }, (long[])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new double[] { 1000.0 }, (double[])elements.get(0).value, 0.0);
    }

    @Test
    public void testObjectEncode() throws Exception {
        byte[] encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(String.class, "Test")));
        Assert.assertArrayEquals(new byte[] { ABIToken.STRING.identifier, 0, 4, 84, 101, 115, 116 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Address.class, new Address(bytesOfLength(Address.LENGTH)))));
        Assert.assertArrayEquals(new byte[] { ABIToken.ADDRESS.identifier, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31 }, encoded);
        
        // We will also check the null encoding here, too.
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(String.class, null)));
        Assert.assertArrayEquals(new byte[] { ABIToken.NULL.identifier, ABIToken.STRING.identifier }, encoded);
    }

    @Test
    public void testObjectDecode() throws Exception {
        List<ABICodec.Tuple> elements = ABICodec.parseEverything(new byte[] { ABIToken.STRING.identifier, 0, 4, 84, 101, 115, 116 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals("Test", elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ADDRESS.identifier, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31 });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals(new Address(bytesOfLength(Address.LENGTH)), elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.NULL.identifier, ABIToken.STRING.identifier });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals(null, elements.get(0).value);
        Assert.assertEquals(String.class, elements.get(0).type);
    }

    @Test
    public void testObjectArrayEncode() throws Exception {
        byte[] encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(String[].class, new String[] { "Test" })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.STRING.identifier, 0, 1, ABIToken.STRING.identifier, 0, 4, 84, 101, 115, 116 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(Address[].class, new Address[] { new Address(bytesOfLength(Address.LENGTH)) })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.ADDRESS.identifier, 0, 1, ABIToken.ADDRESS.identifier, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31 }, encoded);
        
        // Array containing a null element.
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(String[].class, new String[] { null, "Test" })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.STRING.identifier, 0, 2, ABIToken.NULL.identifier, ABIToken.STRING.identifier, ABIToken.STRING.identifier, 0, 4, 84, 101, 115, 116 }, encoded);
        
        // Null array.
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(String[].class, null)));
        Assert.assertArrayEquals(new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.STRING.identifier }, encoded);
    }

    @Test
    public void testObjectArrayDecode() throws Exception {
        List<ABICodec.Tuple> elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.STRING.identifier, 0, 1, ABIToken.STRING.identifier, 0, 4, 84, 101, 115, 116 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new String[] { "Test" }, (String[])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.ADDRESS.identifier, 0, 1, ABIToken.ADDRESS.identifier, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new Address[] { new Address(bytesOfLength(Address.LENGTH)) }, (Address[])elements.get(0).value);
        
        // Array containing a null element.
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.STRING.identifier, 0, 2, ABIToken.NULL.identifier, ABIToken.STRING.identifier, ABIToken.STRING.identifier, 0, 4, 84, 101, 115, 116 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new String[] { null, "Test" }, (String[])elements.get(0).value);
        
        // Null array.
        elements = ABICodec.parseEverything(new byte[] { ABIToken.NULL.identifier, ABIToken.ARRAY.identifier, ABIToken.STRING.identifier });
        Assert.assertEquals(1, elements.size());
        Assert.assertEquals(null, elements.get(0).value);
        Assert.assertEquals(String[].class, elements.get(0).type);
    }

    @Test
    public void testPrimitive2DArrayEncode() throws Exception {
        byte[] encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(byte[][].class, new byte[][] { new byte[] { (byte)-1 } })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BYTE.identifier, 0, 1, ABIToken.A_BYTE.identifier, 0, 1, -1 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(char[][].class, new char[][] { new char[] { 'a' } })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_CHAR.identifier, 0, 1, ABIToken.A_CHAR.identifier, 0, 1, 0, 97 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(boolean[][].class, new boolean[][] { new boolean[] { true } })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BOOLEAN.identifier, 0, 1, ABIToken.A_BOOLEAN.identifier, 0, 1, 1 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(short[][].class, new short[][] { new short[] { (short)1000 } })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_SHORT.identifier, 0, 1, ABIToken.A_SHORT.identifier, 0, 1, 3, -24 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(int[][].class, new int[][] { new int[] { 1000 } })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_INT.identifier, 0, 1, ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(float[][].class, new float[][] { new float[] { 1000.0F } })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_FLOAT.identifier, 0, 1, ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(long[][].class, new long[][] { new long[] { 1000L } })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_LONG.identifier, 0, 1, ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24 }, encoded);
        
        encoded = ABICodec.serializeList(Collections.singletonList(new ABICodec.Tuple(double[][].class, new double[][] { new double[] { 1000.0 } })));
        Assert.assertArrayEquals(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_DOUBLE.identifier, 0, 1, ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0 }, encoded);
    }

    @Test
    public void testPrimitive2DArrayDecode() throws Exception {
        List<ABICodec.Tuple> elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BYTE.identifier, 0, 1, ABIToken.A_BYTE.identifier, 0, 1, -1 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new byte[][] { new byte[] { (byte)-1 } }, (byte[][])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_CHAR.identifier, 0, 1, ABIToken.A_CHAR.identifier, 0, 1, 0, 97 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new char[][] { new char[] { 'a' } }, (char[][])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_BOOLEAN.identifier, 0, 1, ABIToken.A_BOOLEAN.identifier, 0, 1, 1 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new boolean[][] { new boolean[] { true } }, (boolean[][])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_SHORT.identifier, 0, 1, ABIToken.A_SHORT.identifier, 0, 1, 3, -24 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new short[][] { new short[] { (short)1000 } }, (short[][])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_INT.identifier, 0, 1, ABIToken.A_INT.identifier, 0, 1, 0, 0, 3, -24 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new int[][] { new int[] { 1000 } }, (int[][])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_FLOAT.identifier, 0, 1, ABIToken.A_FLOAT.identifier, 0, 1, 68, 122, 0, 0 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new float[][] { new float[] { 1000.0F } }, (float[][])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_LONG.identifier, 0, 1, ABIToken.A_LONG.identifier, 0, 1, 0, 0, 0, 0, 0, 0, 3, -24 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new long[][] { new long[] { 1000L } }, (long[][])elements.get(0).value);
        
        elements = ABICodec.parseEverything(new byte[] { ABIToken.ARRAY.identifier, ABIToken.A_DOUBLE.identifier, 0, 1, ABIToken.A_DOUBLE.identifier, 0, 1, 64, -113, 64, 0, 0, 0, 0, 0 });
        Assert.assertEquals(1, elements.size());
        Assert.assertArrayEquals(new double[][] { new double[] { 1000.0 } }, (double[][])elements.get(0).value);
    }

    @Test
    public void testSymmetry() throws Exception {
        String testString = "Test";
        List<ABICodec.Tuple> input = new ArrayList<>();
        input.add(new ABICodec.Tuple(String.class, testString));
        input.add(new ABICodec.Tuple(Byte.class, Byte.valueOf((byte)-1)));
        input.add(new ABICodec.Tuple(Address.class, null));
        input.add(new ABICodec.Tuple(int[].class, new int[] {1,2,3}));
        
        byte[] result = ABICodec.serializeList(input);
        Assert.assertArrayEquals(new byte[] {
                ABIToken.STRING.identifier, 0, 4, 84, 101, 115, 116,
                ABIToken.BYTE.identifier, -1,
                ABIToken.NULL.identifier, ABIToken.ADDRESS.identifier,
                ABIToken.A_INT.identifier, 0, 3, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3
        }, result);
        
        List<ABICodec.Tuple> list = ABICodec.parseEverything(result);
        Assert.assertEquals(4, list.size());
        Assert.assertEquals(testString, list.get(0).value);
        Assert.assertEquals(Byte.class, list.get(1).type);
        Assert.assertEquals(null, list.get(2).value);
        Assert.assertEquals(int[].class, list.get(3).type);
    }

    @Test
    public void testParseEmpty() {
        byte[] emptyArray = new byte[0];
        List result = ABICodec.parseEverything(emptyArray);
        Assert.assertTrue(result.isEmpty());
    }


    private byte[] bytesOfLength(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < data.length; ++i) {
            data[i] = (byte)i;
        }
        return data;
    }
}

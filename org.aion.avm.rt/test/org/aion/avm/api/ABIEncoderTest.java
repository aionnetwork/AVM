package org.aion.avm.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aion.avm.arraywrapper.Array;
import org.aion.avm.arraywrapper.BooleanArray;
import org.aion.avm.arraywrapper.ByteArray;
import org.aion.avm.arraywrapper.CharArray;
import org.aion.avm.arraywrapper.DoubleArray;
import org.aion.avm.arraywrapper.FloatArray;
import org.aion.avm.arraywrapper.IntArray;
import org.aion.avm.arraywrapper.LongArray;
import org.aion.avm.arraywrapper.ObjectArray;
import org.aion.avm.arraywrapper.ShortArray;
import org.aion.avm.internal.IArrayWrapperFactory;
import org.aion.avm.internal.IInstrumentation;
import org.aion.avm.internal.InstrumentationHelpers;
import org.aion.avm.internal.OutOfEnergyException;
import org.aion.avm.shadow.java.lang.Class;
import org.aion.avm.shadow.java.lang.Object;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import testutils.TestArrayWrapperFactory;
import testutils.TestInstrumentation;


public class ABIEncoderTest {
    private static IInstrumentation instrumentation;

    @BeforeClass
    public static void classSetup() {
        ABIEncoder.initializeArrayFactory(new TestArrayWrapperFactory());
        instrumentation = new TestInstrumentation();
        InstrumentationHelpers.attachThread(instrumentation);
    }

    @AfterClass
    public static void classTearDown() {
        InstrumentationHelpers.detachThread(instrumentation);
    }

    @Test
    public void testPrimitiveEncode() {
        byte[] encoded = ABIEncoder.encodeOneObject(Byte.valueOf((byte)-1));
        Assert.assertArrayEquals(new byte[] {66, -1}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Character.valueOf('a'));
        Assert.assertArrayEquals(new byte[] {67, 0, 97}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Boolean.valueOf(true));
        Assert.assertArrayEquals(new byte[] {90, 1}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Short.valueOf((short)1000));
        Assert.assertArrayEquals(new byte[] {83, 3, -24}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Integer.valueOf(1000));
        Assert.assertArrayEquals(new byte[] {73, 0, 0, 3, -24}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Float.valueOf(1000.0F));
        Assert.assertArrayEquals(new byte[] {70, 68, 122, 0, 0}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Long.valueOf(1000L));
        Assert.assertArrayEquals(new byte[] {76, 0, 0, 0, 0, 0, 0, 3, -24}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(Double.valueOf(1000.0));
        Assert.assertArrayEquals(new byte[] {68, 64, -113, 64, 0, 0, 0, 0, 0}, encoded);
    }

    @Test
    public void testPrimitiveDecode() {
        byte[] encoded = new byte[] {66, -1};
        Assert.assertEquals((byte)-1, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {67, 0, 97};
        Assert.assertEquals('a', ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {90, 1};
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {83, 3, -24};
        Assert.assertEquals((short)1000, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {73, 0, 0, 3, -24};
        Assert.assertEquals(1000, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {70, 68, 122, 0, 0};
        Assert.assertEquals(1000.0F, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {76, 0, 0, 0, 0, 0, 0, 3, -24};
        Assert.assertEquals(1000L, ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {68, 64, -113, 64, 0, 0, 0, 0, 0};
        Assert.assertEquals(1000.0, ABIDecoder.decodeOneObject(encoded));
    }

    @Test
    public void testPrimitiveSymmetry() {
        byte[] encoded = ABIEncoder.encodeOneObject(Byte.valueOf((byte)-1));
        Assert.assertEquals((byte)-1, ABIDecoder.decodeOneObject(encoded));
        
        encoded = ABIEncoder.encodeOneObject(Character.valueOf('a'));
        Assert.assertEquals('a', ABIDecoder.decodeOneObject(encoded));
        
        encoded = ABIEncoder.encodeOneObject(Boolean.valueOf(true));
        Assert.assertEquals(true, ABIDecoder.decodeOneObject(encoded));
        
        encoded = ABIEncoder.encodeOneObject(Short.valueOf((short)1000));
        Assert.assertEquals((short)1000, ABIDecoder.decodeOneObject(encoded));
        
        encoded = ABIEncoder.encodeOneObject(Integer.valueOf(1000));
        Assert.assertEquals(1000, ABIDecoder.decodeOneObject(encoded));
        
        encoded = ABIEncoder.encodeOneObject(Float.valueOf(1000.0F));
        Assert.assertEquals(1000.0F, ABIDecoder.decodeOneObject(encoded));
        
        encoded = ABIEncoder.encodeOneObject(Long.valueOf(1000L));
        Assert.assertEquals(1000L, ABIDecoder.decodeOneObject(encoded));
        
        encoded = ABIEncoder.encodeOneObject(Double.valueOf(1000.0));
        Assert.assertEquals(1000.0, ABIDecoder.decodeOneObject(encoded));
    }

    @Test
    public void testPrimitiveArray1Encode() {
        byte[] encoded = ABIEncoder.encodeOneObject(new byte[] { (byte)-1} );
        Assert.assertArrayEquals(new byte[] {91, 66, 49, 93, -1}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new char[] { 'a' });
        Assert.assertArrayEquals(new byte[] {91, 67, 49, 93, 0, 97}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new boolean[] { true });
        Assert.assertArrayEquals(new byte[] {91, 90, 49, 93, 1}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new short[] { (short)1000 });
        Assert.assertArrayEquals(new byte[] {91, 83, 49, 93, 3, -24}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new int[] { 1000 });
        Assert.assertArrayEquals(new byte[] {91, 73, 49, 93, 0, 0, 3, -24}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new float[] { 1000.0F });
        Assert.assertArrayEquals(new byte[] {91, 70, 49, 93, 68, 122, 0, 0}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new long[] { 1000L });
        Assert.assertArrayEquals(new byte[] {91, 76, 49, 93, 0, 0, 0, 0, 0, 0, 3, -24}, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new double[] { 1000.0 });
        Assert.assertArrayEquals(new byte[] {91, 68, 49, 93, 64, -113, 64, 0, 0, 0, 0, 0}, encoded);
    }

    @Test
    public void testPrimitiveArray1Decode() {
        byte[] encoded = new byte[] {91, 66, 49, 93, -1};
        Assert.assertArrayEquals(new byte[] { (byte)-1 }, (byte[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 67, 49, 93, 0, 97};
        Assert.assertArrayEquals(new char[] { 'a' }, (char[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 90, 49, 93, 1};
        Assert.assertArrayEquals(new boolean[] { true }, (boolean[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 83, 49, 93, 3, -24};
        Assert.assertArrayEquals(new short[] { (short)1000 }, (short[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 73, 49, 93, 0, 0, 3, -24};
        Assert.assertArrayEquals(new int[] { 1000 }, (int[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 70, 49, 93, 68, 122, 0, 0};
        Assert.assertArrayEquals(new float[] { 1000.0F }, (float[]) ABIDecoder.decodeOneObject(encoded), 0.0F);
        
        encoded = new byte[] {91, 76, 49, 93, 0, 0, 0, 0, 0, 0, 3, -24};
        Assert.assertArrayEquals(new long[] { 1000L }, (long[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 68, 49, 93, 64, -113, 64, 0, 0, 0, 0, 0};
        Assert.assertArrayEquals(new double[] { 1000.0 }, (double[]) ABIDecoder.decodeOneObject(encoded), 0.0);
    }

    @Test
    public void testPrimitiveArray2Encode() {
        byte[] encoded = ABIEncoder.encodeOneObject(new byte[][] { new byte[] { (byte)-1 }, new byte[] { (byte)-1 } } );
        Assert.assertArrayEquals(new byte[] { 91, 91, 66, 93, 50, 93, 40, 49, 41, 40, 49, 41, -1, -1 }, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new char[][] { new char[] { 'a' }, new char[] { 'a' } } );
        Assert.assertArrayEquals(new byte[] { 91, 91, 67, 93, 50, 93, 40, 49, 41, 40, 49, 41, 0, 97, 0, 97 }, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new boolean[][] { new boolean[] { true }, new boolean[] { true } } );
        Assert.assertArrayEquals(new byte[] { 91, 91, 90, 93, 50, 93, 40, 49, 41, 40, 49, 41, 1, 1 }, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new short[][] { new short[] { (short)1000 }, new short[] { (short)1000 } } );
        Assert.assertArrayEquals(new byte[] { 91, 91, 83, 93, 50, 93, 40, 49, 41, 40, 49, 41, 3, -24, 3, -24 }, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new int[][] { new int[] { 1000 }, new int[] { 1000 } } );
        Assert.assertArrayEquals(new byte[] { 91, 91, 73, 93, 50, 93, 40, 49, 41, 40, 49, 41, 0, 0, 3, -24, 0, 0, 3, -24 }, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new float[][] { new float[] { 1000.0F }, new float[] { 1000.0F } } );
        Assert.assertArrayEquals(new byte[] { 91, 91, 70, 93, 50, 93, 40, 49, 41, 40, 49, 41, 68, 122, 0, 0, 68, 122, 0, 0 }, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new long[][] { new long[] { 1000L }, new long[] { 1000L } } );
        Assert.assertArrayEquals(new byte[] { 91, 91, 76, 93, 50, 93, 40, 49, 41, 40, 49, 41, 0, 0, 0, 0, 0, 0, 3, -24, 0, 0, 0, 0, 0, 0, 3, -24 }, encoded);
        
        encoded = ABIEncoder.encodeOneObject(new double[][] { new double[] { 1000.0 }, new double[] { 1000.0 } } );
        Assert.assertArrayEquals(new byte[] { 91, 91, 68, 93, 50, 93, 40, 49, 41, 40, 49, 41, 64, -113, 64, 0, 0, 0, 0, 0, 64, -113, 64, 0, 0, 0, 0, 0 }, encoded);
    }

    @Test
    public void testPrimitiveArray2Decode() {
        byte[] encoded = new byte[] {91, 66, 49, 93, -1};
        Assert.assertArrayEquals(new byte[] { (byte)-1 }, (byte[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 67, 49, 93, 0, 97};
        Assert.assertArrayEquals(new char[] { 'a' }, (char[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 90, 49, 93, 1};
        Assert.assertArrayEquals(new boolean[] { true }, (boolean[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 83, 49, 93, 3, -24};
        Assert.assertArrayEquals(new short[] { (short)1000 }, (short[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 73, 49, 93, 0, 0, 3, -24};
        Assert.assertArrayEquals(new int[] { 1000 }, (int[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 70, 49, 93, 68, 122, 0, 0};
        Assert.assertArrayEquals(new float[] { 1000.0F }, (float[]) ABIDecoder.decodeOneObject(encoded), 0.0F);
        
        encoded = new byte[] {91, 76, 49, 93, 0, 0, 0, 0, 0, 0, 3, -24};
        Assert.assertArrayEquals(new long[] { 1000L }, (long[]) ABIDecoder.decodeOneObject(encoded));
        
        encoded = new byte[] {91, 68, 49, 93, 64, -113, 64, 0, 0, 0, 0, 0};
        Assert.assertArrayEquals(new double[] { 1000.0 }, (double[]) ABIDecoder.decodeOneObject(encoded), 0.0);
    }

    @Test
    public void testMethodCallEncoding() {
        byte[] encoded = ABIEncoder.encodeMethodArguments("method", 123, (byte)-1, "hello");
        // We expect this to be 6d6574686f64 3c49425b54355d3e 0007b ff 68656c6c6f:
        byte[] expected = new byte[] {
                0x6d, 0x65, 0x74, 0x68, 0x6f, 0x64,
                0x3c, 0x49, 0x42, 0x5b, 0x54, 0x35, 0x5d, 0x3e,
                0x00, 0x00, 0x00, 0x7b,
                (byte)0xff,
                0x68, 0x65, 0x6c, 0x6c, 0x6f,
        };
        Assert.assertArrayEquals(expected, encoded);
    }
}

package org.aion.avm.core.util;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

public class TxDataDecoderTest {
    @Test
    public void testDecodeElementaryTypes() throws UnsupportedEncodingException, InvalidTxDataException {
        byte[] txData = new byte[]{0x6D, 0x65, 0x74, 0x68, 0x6F, 0x64, 0x41, // methodA
                0x3C, 0x49, 0x42, 0x4C, 0x46, 0x5A, 0x5A, 0x43, 0x53, 0x44, 0x3E, // <IBLFZZCSD>
                0x00, 0x00, 0x00, 0x7B, (byte) 0xFF, // 123, -1
                0x1, 0x5E, (byte) 0xE2, (byte) 0xA3, 0x21, (byte) 0xCE, 0x7D, 0x15, // 98765432123456789L
                64, 102, 102, 102, // 3.6f
                111, 0,  // true, false
                0x65,    // 'e'
                0x01, 0x41, // 321 short
                64, 12, -52, -52, -52, -52, -52, -51  // 3.6d double
        };
        //  {123, -1} of a Java method, public static void methodA(Integer i, byte b)

        TxDataDecoder decoder = new TxDataDecoder();
        TxDataDecoder.MethodCaller mc = decoder.decode(txData);
        assertEquals("methodA", mc.methodName);
        assertEquals(9, mc.arguments.size());
        assertEquals(123, mc.arguments.get(0));
        assertEquals((byte)-1, mc.arguments.get(1));
        assertEquals(98765432123456789L, mc.arguments.get(2));
        assertEquals(3.6f, mc.arguments.get(3));
        assertEquals(true, mc.arguments.get(4));
        assertEquals(false, mc.arguments.get(5));
        assertEquals('e', mc.arguments.get(6));
        assertEquals((short)321, mc.arguments.get(7));
        assertEquals(3.6d, mc.arguments.get(8));
    }

    @Test
    public void testDecode1DArray() throws UnsupportedEncodingException, InvalidTxDataException {
        byte[] txData = new byte[] {0x6D, 0x65, 0x74, 0x68, 0x6F, 0x64, 0x41, // methodA
                0x3C, 0x49, 0x42, 0x5B, 0x43, 0x35, 0x5D, 0x3E, // <IB[C5]>
                0x00, 0x00, 0x00, 0x7B, (byte)0xFF, // 123, -1
                0x68, 0x65, 0x6C, 0x6C, 0x6F}; // hello
        //  {123, -1, "hello"} of a Java method, public static void methodA(Integer i, byte b, String s)

        TxDataDecoder decoder = new TxDataDecoder();
        TxDataDecoder.MethodCaller mc = decoder.decode(txData);
        assertEquals("methodA", mc.methodName);
        assertEquals(3, mc.arguments.size());
        assertEquals(123, mc.arguments.get(0));
        assertEquals((byte)-1, mc.arguments.get(1));
        assertEquals("hello", mc.arguments.get(2));
    }

    @Test
    public void testDecode2DArray() throws UnsupportedEncodingException, InvalidTxDataException {
        byte[] txData = new byte[] {0x6D, 0x65, 0x74, 0x68, 0x6F, 0x64, 0x41, // methodA
                0x3C, 0x49, 0x42, 0x5B, 0x43, 0x35, 0x5D, 0x5B, 0x5B, 0x43, 0x33, 0x5D, 0x32, 0x5D, 0x3E, // <IB[C5][[C3]2]>
                0x00, 0x00, 0x00, 0x7B, (byte)0xFF, // 123, -1
                0x68, 0x65, 0x6C, 0x6C, 0x6F, // hello
                0x63, 0x61, 0x74, 0x64, 0x6F, 0x67}; // {cat, dog}
        //  {123, -1, "hello"} of a Java method, public static void methodA(Integer i, byte b, String s)

        TxDataDecoder decoder = new TxDataDecoder();
        TxDataDecoder.MethodCaller mc = decoder.decode(txData);
        assertEquals("methodA", mc.methodName);
        assertEquals(4, mc.arguments.size());
        assertEquals(123, mc.arguments.get(0));
        assertEquals((byte)-1, mc.arguments.get(1));
        assertEquals("hello", mc.arguments.get(2));
        assertEquals("cat", ((String[])(mc.arguments.get(3)))[0]);
        assertEquals("dog", ((String[])(mc.arguments.get(3)))[1]);
    }

    @Test
    public void testDecode2DJaggedArray() throws UnsupportedEncodingException, InvalidTxDataException {
        byte[] txData = new byte[]{0x6D, 0x65, 0x74, 0x68, 0x6F, 0x64, 0x41, // methodA
                0x3C, 0x49, 0x42, 0x5B, 0x5B, 0x43, 0x33, 0x5D, 0x32, 0x5D, 0x5B, 0x5B, 0x49, 0x5D, 0x35, 0x5D,
                0x28, 0x31, 0x29, 0x28, 0x32, 0x29, 0x28, 0x35, 0x29, 0x28, 0x31, 0x29, 0x28, 0x33, 0x29, 0x3E, // <IB[[C3]2][[I]5](1)(2)(5)(1)(3)>
                0x00, 0x00, 0x00, 0x7B, (byte) 0xFF, // 123, -1
                0x63, 0x61, 0x74, 0x64, 0x6F, 0x67, // {cat, dog}
                0x00, 0x01, 0x2A, (byte) 0xD2, // 76498
                0x00, 0x00, 0x02, 0x51, 0x00, 0x00, 0x01, (byte)0x85, // 593, 389
                0x00, 0x00, 0x00, (byte)0xEA, 0x00, 0x00, 0x01, 0x59, 0x00, 0x00, 0x01, (byte) 0xC8, 0x00, 0x00, 0x02, 0x37, 0x00, 0x00, 0x02, (byte)0xA6,
                // 234, 345, 456, 567, 678
                0x00, 0x00, 0x03, 0x15, // 789
                0x00, 0x00, 0x03, 0x6C, 0x00, 0x00, 0x02, (byte) 0xFD, 0x00, 0x00, 0x02, (byte) 0x8E,// 876, 765, 654
                };
        //  {123, -1, "hello"} of a Java method, public static void methodA(Integer i, byte b, String s)

        TxDataDecoder decoder = new TxDataDecoder();
        TxDataDecoder.MethodCaller mc = decoder.decode(txData);
        assertEquals("methodA", mc.methodName);
        assertEquals(4, mc.arguments.size());
        assertEquals(123, mc.arguments.get(0));
        assertEquals((byte)-1, mc.arguments.get(1));
        assertEquals("cat", ((String[])(mc.arguments.get(2)))[0]);
        assertEquals("dog", ((String[])(mc.arguments.get(2)))[1]);

        assertEquals(76498, ((int[][])(mc.arguments.get(3)))[0][0]);
        assertEquals(593, ((int[][])(mc.arguments.get(3)))[1][0]);
        assertEquals(389, ((int[][])(mc.arguments.get(3)))[1][1]);
        assertEquals(234, ((int[][])(mc.arguments.get(3)))[2][0]);
        assertEquals(345, ((int[][])(mc.arguments.get(3)))[2][1]);
        assertEquals(456, ((int[][])(mc.arguments.get(3)))[2][2]);
        assertEquals(567, ((int[][])(mc.arguments.get(3)))[2][3]);
        assertEquals(678, ((int[][])(mc.arguments.get(3)))[2][4]);
        assertEquals(789, ((int[][])(mc.arguments.get(3)))[3][0]);
        assertEquals(876, ((int[][])(mc.arguments.get(3)))[4][0]);
        assertEquals(765, ((int[][])(mc.arguments.get(3)))[4][1]);
        assertEquals(654, ((int[][])(mc.arguments.get(3)))[4][2]);
    }
}
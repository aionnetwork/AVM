package org.aion.avm.rt;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

public class TxDataDecoderTest {
    @Test
    public void testDecodeElementaryTypes() throws UnsupportedEncodingException, InvalidTxDataException {
        byte[] txData = new byte[] {0x6D, 0x65, 0x74, 0x68, 0x6F, 0x64, 0x41, // methodA
                0x3C, 0x49, 0x42, 0x4C, 0x3E, // <IBL>
                0x00, 0x00, 0x00, 0x7B, (byte)0xFF, // 123, -1
                0x1, 0x5E, (byte)0xE2, (byte)0xA3, 0x21, (byte)0xCE, 0x7D, 0x15}; // 98765432123456789L
        //  {123, -1} of a Java method, public static void methodA(Integer i, byte b)

        TxDataDecoder decoder = new TxDataDecoder();
        TxDataDecoder.MethodCaller mc = decoder.decode(txData);
        assertEquals("methodA", mc.methodName);
        assertEquals(3, mc.arguments.size());
        assertEquals(123, mc.arguments.get(0));
        assertEquals((byte)-1, mc.arguments.get(1));
        assertEquals(98765432123456789L, mc.arguments.get(2));
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
}
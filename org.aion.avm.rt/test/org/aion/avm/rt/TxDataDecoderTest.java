package org.aion.avm.rt;

import org.junit.Test;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

public class TxDataDecoderTest {
    @Test
    public void testDecodeOne() throws UnsupportedEncodingException, InvalidTxDataException {
        byte[] txData = new byte[] {0x6D, 0x65, 0x74, 0x68, 0x6F, 0x64, 0x41, // methodA
                0x3C, 0x49, 0x42, 0x3E, // <IB>
                0x00, 0x00, 0x00, 0x7B, (byte)0xFF}; // 123, -1
        //  {123, -1} of a Java method, public static void methodA(Integer i, byte b)

        TxDataDecoder decoder = new TxDataDecoder();
        TxDataDecoder.MethodCaller mc = decoder.decode(txData);
        assertEquals("methodA", mc.methodName);
        assertEquals(2, mc.arguments.size());
        assertEquals(123, mc.arguments.get(0));
        assertEquals((byte)-1, mc.arguments.get(1));
    }

    @Test
    public void testDecode() throws UnsupportedEncodingException, InvalidTxDataException {
        byte[] txData = new byte[] {0x6D, 0x65, 0x74, 0x68, 0x6F, 0x64, 0x41, // methodA
                0x3C, 0x49, 0x42, 0x5B, 0x43, 0x35, 0x5D, 0x3E, // <IB[C5]>
                0x00, 0x00, 0x00, 0x7B, (byte)0xFF, // 123, -1
                0x68, 0x65, 0x6C, 0x6C, 0x6F}; // hello
        //  {123, -1, "hello"} of a Java method, public static void methodA(Integer i, byte b, String s)

        TxDataDecoder decoder = new TxDataDecoder();
        //TxDataDecoder.MethodCaller mc = decoder.decode(txData);
        //assertEquals("methodA", mc.methodName);
        //assertEquals(3, mc.arguments.size());
        //assertEquals(123, mc.arguments.get(0));
        //assertEquals(-1, mc.arguments.get(1));
        //assertEquals("hello", mc.arguments.get(2));
    }
}
package com.example.helloworld;

public class HelloWorld {

    public int foo;

    public static int bar;

    public byte[]  add(int a, int b) {
        int c = a + b;
        byte[] res = new byte[4];
        res[0] = (byte) ((c & 0xFF000000) >> 24);
        res[1] = (byte) ((c & 0xFF0000) >> 16);
        res[2] = (byte) ((c & 0xFF00) >> 8);
        res[3] = (byte) (c & 0xFF);
        return res;

        //return ByteBuffer.allocate(4).putInt(a + b).array();
        // java.nio.ByteBuffer is not on the white list!
    }

    public byte[] run() {
        return "Hello, world!".getBytes();
    }
}

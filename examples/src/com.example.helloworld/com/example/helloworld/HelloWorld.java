package com.example.helloworld;

import org.aion.avm.rt.BlockchainRuntime;

public class HelloWorld {

    public int foo;

    public static int bar;

    public int add(int a, int b) {
        return a + b;
    }

    public byte[] run(byte[] input, BlockchainRuntime rt) {
        return "Hello, world!".getBytes();
    }
}

package com.example.helloworld;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.api.BlockchainRuntime;

public class HelloWorld {

    public int foo;

    public static int bar;

    public int add(int a, int b) {
        return a + b;
    }

    public byte[] run() {
        return "Hello, world!".getBytes();
    }

    public static void init() {
    }

    public static byte[] main() throws InvalidTxDataException {
        return ABIEncoder.encodeOneObject(ABIDecoder.decodeAndRun(new HelloWorld(), BlockchainRuntime.getData()));
    }
}

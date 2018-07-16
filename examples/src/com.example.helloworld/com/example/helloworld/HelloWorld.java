package com.example.helloworld;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.api.BlockchainRuntime;

public class HelloWorld {

    public int foo;

    public static int bar;

    public byte[] add(int a, int b) {
        return ABIEncoder.encodeInt(a + b);
    }

    public byte[] run() {
        return "Hello, world!".getBytes();
    }

    public static void init() {
    }

    public static byte[] main() throws InvalidTxDataException {
        return ABIDecoder.decodeAndRun(new HelloWorld(), BlockchainRuntime.getData());
    }
}

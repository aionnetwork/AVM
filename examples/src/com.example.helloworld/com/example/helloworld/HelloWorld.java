package com.example.helloworld;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.InvalidTxDataException;
import org.aion.avm.api.BlockchainRuntime;

public class HelloWorld {

    public int foo;

    public static int bar;

    public static byte[] add(int a, int b) {
        return ABIEncoder.encodeInt(a + b);
    }

    public static byte[] run() {
        return "Hello, world!".getBytes();
    }

    public byte[] main() throws InvalidTxDataException {
        return ABIDecoder.decodeAndRun(this.getClass(), BlockchainRuntime.getData());
    }
}

package com.example.helloworld;

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

    public Object main() throws InvalidTxDataException {
        // for testing purpose only
        //byte[] txData = new byte[]{0x72, 0x75, 0x6E}; // "run"
        byte[] txData = new byte[]{0x61, 0x64, 0x64, 0x3C, 0x49, 0x49, 0x3E, 0x00, 0x00, 0x00, 0x7B, 0x00, 0x00, 0x00, 0x01}; // "add<II>" + raw data 123, 1

        ABIDecoder decoder = new ABIDecoder();
        ABIDecoder.MethodCaller methodCaller = decoder.decode(txData);
        switch (methodCaller.methodName) {
            case "add":
                return add((int)methodCaller.arguments[0], (int)methodCaller.arguments[1]);
            case "run":
                return run();
            default:
                throw new InvalidTxDataException();
        }
    }
}

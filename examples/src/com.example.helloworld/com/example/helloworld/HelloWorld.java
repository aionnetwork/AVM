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
        ABIDecoder decoder = new ABIDecoder();
        ABIDecoder.MethodCaller methodCaller = decoder.decode(BlockchainRuntime.getData());
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

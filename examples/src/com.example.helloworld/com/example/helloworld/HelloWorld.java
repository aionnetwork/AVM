package com.example.helloworld;

import org.aion.avm.api.BlockchainRuntime;

public class HelloWorld {

    public int foo;

    public static int bar;

    public static int add(int a, int b) {
        return a + b;
    }

    public static byte[] run() {
        return "Hello, world!".getBytes();
    }

    public static byte[] main() throws InvalidTxDataException {
        ABIDecoder.MethodCaller methodCaller = ABIDecoder.decode(BlockchainRuntime.getData());
        switch (methodCaller.methodName) {
            case "add":
                return ABIEncoder.encodeInt(add((int)methodCaller.arguments[0], (int)methodCaller.arguments[1]));
            case "run":
                return run();
            default:
                throw new InvalidTxDataException();
        }
    }
}

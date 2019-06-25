package legacy_examples.helloworld;

import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.userlib.abi.ABIDecoder;


public class HelloWorld {

    private int foo;

    private static int bar;

    static {
        if (Blockchain.getData() != null) {
            ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
            bar = decoder.decodeOneInteger();
        }
    }

    @Callable
    public static int add(int a, int b) {
        return a + b;
    }

    @Callable
    public static byte[] run() {
        return "Hello, world!".getBytes();
    }
}

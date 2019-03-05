package legacy_examples.helloworld;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


public class HelloWorld {

    private int foo;

    private static int bar;

    static {
        if (BlockchainRuntime.getData() != null) {
            Object[] arguments = ABIDecoder.decodeArguments(BlockchainRuntime.getData());
            bar = (int)arguments[0];
        }
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static byte[] run() {
        return "Hello, world!".getBytes();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(HelloWorld.class, BlockchainRuntime.getData());
    }
}

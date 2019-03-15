package legacy_examples.helloworld;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.tooling.abi.Callable;


public class HelloWorld {

    private int foo;

    private static int bar;

    static {
        if (BlockchainRuntime.getData() != null) {
            Object[] arguments = ABIDecoder.decodeArguments(BlockchainRuntime.getData());
            bar = (int)arguments[0];
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

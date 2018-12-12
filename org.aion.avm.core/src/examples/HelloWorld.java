package examples;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * An example DApp discussed here:  https://blog.aion.network/hello-world-from-the-aion-virtual-machine-25038ac62f17
 */
public class HelloWorld {

    public static void sayHello() {
        BlockchainRuntime.println("Hello World!");
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(HelloWorld.class, BlockchainRuntime.getData());
    }

}

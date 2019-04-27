package org.aion.avm.core.shadowing.lambdas;

import java.util.function.Supplier;


/**
 * This should fail to deploy since it references Supplier but the only lambda types we support are Runnable and Function.
 */
public class FunctionShadowFailSupplierResource {
    public static byte[] main() {
        Supplier<byte[]> sup = () -> new byte[0];
        return sup.get();
    }
}


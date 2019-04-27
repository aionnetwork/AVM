package org.aion.avm.core.shadowing.lambdas;

import avm.Blockchain;


/**
 * This should fail to deploy since it tries to pass additional arguments to the Runnable, which we don't allow.
 * Further explanation of this is found in AKI-130 but the gist is that this would require us to dynamically generate lots of
 * small classes, to support the different parameterized implementations, which is a possible attack vector.
 */
public class FunctionShadowFailArgsResource {
    public static byte[] main() {
        int foo = 5;
        Runnable sup = () -> Blockchain.println("Got: " + foo);
        sup.run();
        return new byte[0];
    }
}


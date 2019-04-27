package org.aion.avm.core.shadowing.lambdas;

import avm.Blockchain;


/**
 * This is similar to the FunctionShadowFailArgsResource except it shows a safe way to accomplish the same thing, without relying on invokedynamic.
 */
public class FunctionShadowPassArgsResource {
    public static byte[] main() {
        int foo = 5;
        Runnable sup = new Runnable() {
            @Override
            public void run() {
                Blockchain.println("Got: " + foo);
            }
        };
        sup.run();
        return new byte[0];
    }
}


package org.aion.avm.core.shadowing.lambdas;


/**
 * This should fail to deploy since we only currently support Runnable and Function lambdas, yet this tries to use a Comparable.
 */
public class FunctionShadowFailComparableResource {
    public static byte[] main() {
        int foo = 5;
        Comparable<Integer> comp = (o) -> 0;
        comp.compareTo(foo);
        return new byte[0];
    }
}


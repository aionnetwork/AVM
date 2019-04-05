package org.aion.avm.tooling.bootstrapmethods;

import java.lang.invoke.StringConcatFactory;
import avm.Blockchain;

/**
 * A contract that attempts to call into {@link java.lang.invoke.StringConcatFactory#makeConcat}.
 * This should be illegal.
 */
public class MakeConcatTarget {

    public static void call() {
        try {
            StringConcatFactory.makeConcat(null, null, null);
        } catch (Exception e) {
            Blockchain.println(e.toString());
        }
    }

}

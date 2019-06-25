package org.aion.avm.embed.bootstrapmethods;

import java.lang.invoke.StringConcatFactory;
import avm.Blockchain;

/**
 * A contract that attempts to call into {@link java.lang.invoke.StringConcatFactory#makeConcatWithConstants}.
 * This should be illegal.
 */
public class MakeConcatWithConstantsTarget {

    public static void call() {
        try {
            StringConcatFactory.makeConcatWithConstants(null, null, null, null);
        } catch (Exception e) {
            Blockchain.println(e.toString());
        }
    }

}

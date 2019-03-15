package org.aion.avm.tooling.bootstrapmethods;

import java.lang.invoke.StringConcatFactory;
import org.aion.avm.api.BlockchainRuntime;

/**
 * A contract that attempts to call into {@link java.lang.invoke.StringConcatFactory#makeConcatWithConstants}.
 * This should be illegal.
 */
public class MakeConcatWithConstantsTarget {

    public static void call() {
        try {
            StringConcatFactory.makeConcatWithConstants(null, null, null, null);
        } catch (Exception e) {
            BlockchainRuntime.println(e.toString());
        }
    }

}

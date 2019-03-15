package org.aion.avm.tooling.bootstrapmethods;

import java.lang.invoke.StringConcatFactory;
import org.aion.avm.api.BlockchainRuntime;

/**
 * A contract that attempts to call into {@link java.lang.invoke.StringConcatFactory#makeConcat}.
 * This should be illegal.
 */
public class MakeConcatTarget {

    public static void call() {
        try {
            StringConcatFactory.makeConcat(null, null, null);
        } catch (Exception e) {
            BlockchainRuntime.println(e.toString());
        }
    }

}

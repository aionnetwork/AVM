package org.aion.avm.core.bootstrapmethods;

import java.lang.invoke.StringConcatFactory;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

/**
 * A contract that attempts to call into {@link java.lang.invoke.StringConcatFactory#makeConcat}.
 * This should be illegal.
 */
public class MakeConcatTarget {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(new MakeConcatTarget(), BlockchainRuntime.getData());
    }

    public static void call() {
        try {
            StringConcatFactory.makeConcat(null, null, null);
        } catch (Exception e) {
            BlockchainRuntime.println(e.toString());
        }
    }

}

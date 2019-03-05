package org.aion.avm.tooling.bootstrapmethods;

import java.lang.invoke.LambdaMetafactory;
import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

/**
 * A contract that attempts to call into {@link java.lang.invoke.LambdaMetafactory#metafactory}.
 * This should be illegal.
 */
public class MetaFactoryTarget {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(MetaFactoryTarget.class, BlockchainRuntime.getData());
    }

    public static void call() {
        try {
            LambdaMetafactory.metafactory(null, null, null, null, null, null);
        } catch (Exception e) {
            BlockchainRuntime.println(e.toString());
        }
    }

}

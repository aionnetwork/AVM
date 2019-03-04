package org.aion.avm.core.unification;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


public class CommonSuperClassTarget_combineOverlappingInterfacesA {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(CommonSuperClassTarget_combineOverlappingInterfacesA.class, BlockchainRuntime.getData());
    }

    // Fails (verify error) since we don't handle ambiguous coalescing types.
    public static String combineOverlappingInterfacesA(boolean flag, CommonSuperClassTypes.ChildA a, CommonSuperClassTypes.ChildB b) {
        return (flag ? a : b).getRootA();
    }
}

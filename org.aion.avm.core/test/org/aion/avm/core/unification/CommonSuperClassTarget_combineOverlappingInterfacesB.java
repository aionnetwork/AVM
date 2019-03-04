package org.aion.avm.core.unification;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


public class CommonSuperClassTarget_combineOverlappingInterfacesB {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(CommonSuperClassTarget_combineOverlappingInterfacesB.class, BlockchainRuntime.getData());
    }

    // Fails (verify error) since we don't handle ambiguous coalescing types.  This is the case where javac emits a checkcast.
    public static String combineOverlappingInterfacesB(boolean flag, CommonSuperClassTypes.ChildA a, CommonSuperClassTypes.ChildB b) {
        return (flag ? a : b).getRootB();
    }
}

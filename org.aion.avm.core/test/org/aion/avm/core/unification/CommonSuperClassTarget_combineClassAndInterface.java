package org.aion.avm.core.unification;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


public class CommonSuperClassTarget_combineClassAndInterface {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(CommonSuperClassTarget_combineClassAndInterface.class, BlockchainRuntime.getData());
    }

    // Fails (verify error) since we don't currently handle coalescing paths between classes and non-IObject interfaces.
    public static String combineClassAndInterface(boolean flag, CommonSuperClassTarget_combineClassAndInterface a, CommonSuperClassTypes.RootA b) {
        return (flag ? a : b).toString();
    }
}

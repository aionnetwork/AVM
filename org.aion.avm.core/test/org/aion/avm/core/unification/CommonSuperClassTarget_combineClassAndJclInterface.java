package org.aion.avm.core.unification;

import java.util.Iterator;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


public class CommonSuperClassTarget_combineClassAndJclInterface {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(CommonSuperClassTarget_combineClassAndJclInterface.class, BlockchainRuntime.getData());
    }

    // Fails (NPE) since we don't properly describe JCL interfaces.
    public static String combineClassAndJclInterface(boolean flag, CommonSuperClassTarget_combineClassAndJclInterface a, Iterator<?> b) {
        return (flag ? a : b).toString();
    }
}

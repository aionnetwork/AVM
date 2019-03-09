package org.aion.avm.tooling.shadowing.testEnum;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class TestResourceForValues {
    static TestEnumForValues test = TestEnumForValues.TEST;

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(TestResourceForValues.class, BlockchainRuntime.getData());
    }

    public static boolean testEnumAccess(){
        boolean ret = true;
        ret = ret && (test == TestEnumForValues.TEST);
        ret = ret && (test == TestEnumForValues.valueOf("TEST"));

        return ret;
    }
}

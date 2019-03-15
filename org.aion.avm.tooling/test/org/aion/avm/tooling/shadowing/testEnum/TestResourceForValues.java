package org.aion.avm.tooling.shadowing.testEnum;

import org.aion.avm.tooling.abi.Callable;

public class TestResourceForValues {
    static TestEnumForValues test = TestEnumForValues.TEST;

    @Callable
    public static boolean testEnumAccess(){
        boolean ret = true;
        ret = ret && (test == TestEnumForValues.TEST);
        ret = ret && (test == TestEnumForValues.valueOf("TEST"));

        return ret;
    }
}

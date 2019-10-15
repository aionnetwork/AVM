package org.aion.avm.embed.bootstrapmethods;

import org.aion.avm.tooling.abi.Callable;
import org.aion.avm.tooling.abi.Initializable;

public class InvokeDynamicBlockTarget {
    @Initializable
    private static String str1;

    @Initializable
    private static String str2;

    @Callable
    public static String concat() {
        return (str1 == null ? "" : str1) +
                (str2 == null ? "" : str2);
    }
}

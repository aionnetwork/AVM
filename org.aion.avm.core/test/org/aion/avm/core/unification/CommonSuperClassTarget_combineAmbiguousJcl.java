package org.aion.avm.core.unification;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

public class CommonSuperClassTarget_combineAmbiguousJcl {
    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    public static Serializable combineAmbiguous1(boolean flag, BigInteger a, BigDecimal b) {
        return (flag ? a : b);
    }

    public static int combineAmbiguous2(boolean flag, BigInteger a, BigDecimal b) {
        return (flag ? a : b).compareTo(null);
    }

}

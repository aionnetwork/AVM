package org.aion.avm.core.unification;

import org.aion.avm.core.unification.CommonSuperClassTypes.EnumA1;
import org.aion.avm.core.unification.CommonSuperClassTypes.EnumB;

public class CommonSuperClassTarget_combineAmbiguousEnums {
    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    public static String combineAmbiguous1(boolean flag, EnumA1 a, EnumB b) {
        return (flag ? a : b).getRootA();
    }

    public static String combineAmbiguous2(boolean flag, EnumA1 a, EnumB b) {
        return (flag ? a : b).getRootB();
    }
}

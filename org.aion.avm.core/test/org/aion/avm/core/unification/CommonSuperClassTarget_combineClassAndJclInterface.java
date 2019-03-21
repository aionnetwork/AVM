package org.aion.avm.core.unification;

import java.util.Iterator;

public class CommonSuperClassTarget_combineClassAndJclInterface {
    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    // Fails (NPE) since we don't properly describe JCL interfaces.
    public static String combineClassAndJclInterface(boolean flag, CommonSuperClassTarget_combineClassAndJclInterface a, Iterator<?> b) {
        return (flag ? a : b).toString();
    }
}

package org.aion.avm.core.unification;

public class CommonSuperClassTarget_combineClassAndInterface {
    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    // Fails (verify error) since we don't currently handle coalescing paths between classes and non-IObject interfaces.
    public static String combineClassAndInterface(boolean flag, CommonSuperClassTarget_combineClassAndInterface a, CommonSuperClassTypes.RootA b) {
        return (flag ? a : b).toString();
    }
}

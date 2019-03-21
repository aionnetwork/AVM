package org.aion.avm.core.unification;

public class CommonSuperClassTarget_combineOverlappingInterfacesA {
    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    // Fails (verify error) since we don't handle ambiguous coalescing types.
    public static String combineOverlappingInterfacesA(boolean flag, CommonSuperClassTypes.ChildA a, CommonSuperClassTypes.ChildB b) {
        return (flag ? a : b).getRootA();
    }
}

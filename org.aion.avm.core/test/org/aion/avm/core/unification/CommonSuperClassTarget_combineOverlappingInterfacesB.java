package org.aion.avm.core.unification;

public class CommonSuperClassTarget_combineOverlappingInterfacesB {

    // The associated test only checks that deployment fails, so main() can return null
    public static byte[] main() {
        return null;
    }

    // Fails (verify error) since we don't handle ambiguous coalescing types.  This is the case where javac emits a checkcast.
    public static String combineOverlappingInterfacesB(boolean flag, CommonSuperClassTypes.ChildA a, CommonSuperClassTypes.ChildB b) {
        return (flag ? a : b).getRootB();
    }
}

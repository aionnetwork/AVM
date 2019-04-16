package org.aion.avm.core.unification;

public class CommonSuperClassTarget_combineAmbiguousArrays {
    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    public static String combineAmbiguous1(boolean flag, CommonSuperClassTypes.ChildA[] a, CommonSuperClassTypes.ChildB[][] b) {
        return (flag ? a[0] : b[0][0]).getRootA();
    }

    public static String combineAmbiguous2(boolean flag, CommonSuperClassTypes.ChildA[] a, CommonSuperClassTypes.ChildB[][] b) {
        return (flag ? a[0] : b[0][0]).getRootB();
    }
}

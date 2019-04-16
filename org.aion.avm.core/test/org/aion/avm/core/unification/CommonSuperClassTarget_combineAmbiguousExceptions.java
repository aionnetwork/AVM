package org.aion.avm.core.unification;

public class CommonSuperClassTarget_combineAmbiguousExceptions {

    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    public static int combineAmbiguous1(boolean flag, Exception1 a, Exception2 b) {
        return (flag ? a : b).numberA();
    }

    public static int combineAmbiguous2(boolean flag, Exception1 a, Exception2 b) {
        return (flag ? a : b).numberB();
    }

    private static interface Interface1 {
        public int numberA();
    }

    private static interface Interface2 {
        public int numberB();
    }

    private static abstract class Exception1 extends RuntimeException implements Interface1, Interface2 {
        private static final long serialVersionUID = 1L;
    }

    private static abstract class Exception2 extends RuntimeException implements Interface1, Interface2 {
        private static final long serialVersionUID = 1L;
    }

}

package org.aion.avm.core.unification;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;


/**
 * This test verifies that there are no unification problems around exceptions, since they represent another case of unification.
 * Note that the main distinction of exceptions is in the exception wrappers, which are not visible to the user code, but only
 * appear as a result of instrumented exception handlers.
 */
public class CommonSuperClassTarget_combineWithExceptions {
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(CommonSuperClassTarget_combineWithExceptions.class, BlockchainRuntime.getData());
    }

    public static ParentException combineRelatedUserExceptions(boolean flag, Child1Exception a, Child2Exception b) {
        return flag ? a : b;
    }

    public static RuntimeException combineRelatedUserAndJclExceptions(boolean flag, Child1Exception a, NullPointerException b) {
        return flag ? a : b;
    }

    public static Throwable combineUnrelatedUserAndJclExceptions(boolean flag, Child1Exception a, AssertionError b) {
        return flag ? a : b;
    }

    public static Object combineUnrelatedUserClassAndJclException(boolean flag, CommonSuperClassTarget_combineWithExceptions a, AssertionError b) {
        return flag ? a : b;
    }

    public static Object combineUnrelatedUserClassAndUserException(boolean flag, CommonSuperClassTarget_combineWithExceptions a, Child1Exception b) {
        return flag ? a : b;
    }


    private static abstract class ParentException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    private static class Child1Exception extends ParentException {
        private static final long serialVersionUID = 1L;
    }

    private static class Child2Exception extends ParentException {
        private static final long serialVersionUID = 1L;
    }
}

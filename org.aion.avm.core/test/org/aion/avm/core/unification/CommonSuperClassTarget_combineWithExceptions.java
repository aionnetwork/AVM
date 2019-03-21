package org.aion.avm.core.unification;

/**
 * This test verifies that there are no unification problems around exceptions, since they represent another case of unification.
 * Note that the main distinction of exceptions is in the exception wrappers, which are not visible to the user code, but only
 * appear as a result of instrumented exception handlers.
 */
public class CommonSuperClassTarget_combineWithExceptions {

    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
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

    public static ParentException catch_combineRelatedUserExceptions() {
        try {
            throw_combineRelatedUserExceptions();
        } catch (Child1Exception e) {
            return e;
        } catch (Child2Exception e) {
            return e;
        }
        return null;
    }
    public static ParentException catchCombined_combineRelatedUserExceptions() {
        try {
            throw_combineRelatedUserExceptions();
        } catch (Child1Exception | Child2Exception e) {
            return e;
        }
        return null;
    }
    private static void throw_combineRelatedUserExceptions() throws Child1Exception, Child2Exception {
    }

    public static RuntimeException catch_combineRelatedUserAndJclExceptions() {
        try {
            throw_combineRelatedUserAndJclExceptions();
        } catch (Child1Exception e) {
            return e;
        } catch (NullPointerException e) {
            return e;
        }
        return null;
    }
    public static RuntimeException catchCombined_combineRelatedUserAndJclExceptions() {
        try {
            throw_combineRelatedUserAndJclExceptions();
        } catch (Child1Exception | NullPointerException e) {
            return e;
        }
        return null;
    }
    private static void throw_combineRelatedUserAndJclExceptions() throws Child1Exception, NullPointerException {
    }

    public static Throwable catch_combineUnrelatedUserAndJclExceptions() {
        try {
            throw_combineUnrelatedUserAndJclExceptions();
        } catch (Child1Exception e) {
            return e;
        } catch (AssertionError e) {
            return e;
        }
        return null;
    }
    public static Throwable catchCombined_combineUnrelatedUserAndJclExceptions() {
        try {
            throw_combineUnrelatedUserAndJclExceptions();
        } catch (Child1Exception | AssertionError e) {
            return e;
        }
        return null;
    }
    private static void throw_combineUnrelatedUserAndJclExceptions() throws Child1Exception, AssertionError {
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

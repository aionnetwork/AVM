package org.aion.avm.core.unification;

import avm.Blockchain;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.abi.ABIException;

/**
 * This test verifies that there are no unification problems around exceptions, since they represent another case of unification.
 * Note that the main distinction of exceptions is in the exception wrappers, which are not visible to the user code, but only
 * appear as a result of instrumented exception handlers.
 */
public class CommonSuperClassTarget_combineWithExceptions {

    private enum EmptyEnum {}

    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        NullPointerException n = new NullPointerException();
        IllegalArgumentException i = new IllegalArgumentException();

        // Just to verify that our solution to re-wrap the exception as Throwable is okay.
        try {
            throw combineExceptions(true, n, i);
        } catch (NullPointerException e) {
            e.toString();
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException();
        } catch (Exception e) {
            throw new IllegalStateException();
        }

        try {
            throw combineExceptions(false, n, i);
        } catch (NullPointerException e) {
            throw new IllegalStateException();
        } catch (IllegalArgumentException e) {
            e.toString();
        } catch (Exception e) {
            throw new IllegalStateException();
        }

        return null;
    }

    public static Exception combineExceptions(boolean flag, NullPointerException n, IllegalArgumentException i) {
        return flag ? n : i;
    }

    public static ParentException combineRelatedUserExceptions(boolean flag, Child1Exception a, Child2Exception b) {
        return flag ? a : b;
    }

    public static ParentException combineRelatedUserExceptionArrays(boolean flag, Child1Exception[] a, Child2Exception[][] b) {
        return flag ? a[0] : b[0][0];
    }

    public static RuntimeException combineRelatedUserAndJclExceptions(boolean flag, Child1Exception a, NullPointerException b) {
        return flag ? a : b;
    }

    public static RuntimeException combineRelatedUserAndJclExceptionArrays(boolean flag, Child1Exception[] a, NullPointerException[] b) {
        return flag ? a[0] : b[0];
    }

    public static Throwable combineUnrelatedUserAndJclExceptions(boolean flag, Child1Exception a, AssertionError b) {
        return flag ? a : b;
    }

    public static Throwable combineUnrelatedUserAndJclExceptionArrays(boolean flag, Child1Exception[][][] a, AssertionError[][] b) {
        return flag ? a[0][0][0] : b[0][0];
    }

    public static Object combineUnrelatedUserClassAndJclException(boolean flag, CommonSuperClassTarget_combineWithExceptions a, AssertionError b) {
        return flag ? a : b;
    }

    public static Object combineUnrelatedUserClassAndJclException(boolean flag, CommonSuperClassTarget_combineWithExceptions[] a, AssertionError[] b) {
        return flag ? a[0] : b[0];
    }

    public static Object combineUnrelatedUserClassAndUserException(boolean flag, CommonSuperClassTarget_combineWithExceptions a, Child1Exception b) {
        return flag ? a : b;
    }

    public static Object combineUnrelatedUserClassAndUserExcpetion(boolean flag, CommonSuperClassTarget_combineWithExceptions[][] a, Child1Exception[] b) {
        return flag ? a[0][0] : b[0];
    }

    public static Object combineExceptions(boolean flag, CommonSuperClassTarget_combineWithExceptions a, Blockchain runtime) {
        return flag ? a : runtime;
    }

    public static Object combineUserlib(boolean flag, CommonSuperClassTarget_combineWithExceptions a, AionMap map) {
        return flag ? a : map;
    }

    public static Object combineEnum(boolean flag, CommonSuperClassTarget_combineWithExceptions a, EmptyEnum emptyEnum) {
        return flag ? a : emptyEnum;
    }

    public static Object combineArray(boolean flag, CommonSuperClassTarget_combineWithExceptions a, CommonSuperClassTypes.RootA root) {
        return flag ? a : root;
    }

    public static Throwable combineApiException(boolean flag, Child1Exception a, ABIException b) {
        return flag ? a : b;
    }

    public static Object combineNull(boolean flag, CommonSuperClassTarget_combineWithExceptions a) {
        return flag ? a : null;
    }

    public static Exception catch_combineApiAndJclExceptions() {
        try {
            throw_combineApiAndJclExceptions();
        } catch (ABIException e) {
            return e;
        } catch (NullPointerException e) {
            return e;
        }
        return null;
    }

    public static Exception catchCombined_combineApiAndJclExceptions() {
        try {
            throw_combineApiAndJclExceptions();
        } catch (ABIException | NullPointerException e) {
            return e;
        }
        return null;
    }

    public static Exception catch_combineApiAndUserExceptions() {
        try {
            throw_combineApiAndUserExceptions();
        } catch (ABIException e) {
            return e;
        } catch (Child1Exception e) {
            return e;
        }
        return null;
    }

    public static Exception catchCombined_combineApiAndUserExceptions() {
        try {
            throw_combineApiAndUserExceptions();
        } catch (ABIException | Child1Exception e) {
            return e;
        }
        return null;
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

    private static void throw_combineApiAndJclExceptions() throws ABIException, NullPointerException {}

    private static void throw_combineApiAndUserExceptions() throws Child1Exception, ABIException {}

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

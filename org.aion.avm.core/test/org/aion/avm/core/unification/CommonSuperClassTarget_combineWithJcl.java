package org.aion.avm.core.unification;

import java.math.BigInteger;
import java.util.Iterator;
import org.aion.avm.core.unification.CommonSuperClassTypes.RootA;
import org.aion.avm.userlib.abi.ABIException;

public class CommonSuperClassTarget_combineWithJcl {

    private enum EmptyEnum {}

    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    public static String combineJclWithUserInterface(boolean flag, RootA a, BigInteger b) {
        return (flag ? a : b).toString();
    }

    public static Object combineJclWithUserInterface(boolean flag, RootA a, Number b) {
        return flag ? a : b;
    }

    public Object combineJclWithUserClass(boolean flag, CommonSuperClassTarget_combineWithJcl a, Iterator<?> b) {
        return flag ? a : b;
    }

    public String combineJclWithUserClass(boolean flag, EmptyIterator a, Iterator b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithArray(boolean flag, CommonSuperClassTarget_combineWithJcl[] a, BigInteger b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithArray(boolean flag, CommonSuperClassTarget_combineWithJcl[] a, Number b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithInterfaceArray(boolean flag, RootA[] a, BigInteger b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithInterfaceArray(boolean flag, RootA[] a, Number[] b) {
        return (flag ? a : b[0]).toString();
    }

    public String combineJclWithEnum(boolean flag, EmptyEnum a, BigInteger b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithEnum(boolean flag, EmptyEnum a, Number b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithException(boolean flag, NullPointerException a, BigInteger b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithException(boolean flag, NullPointerException a, Number b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithUserException(boolean flag, EmptyException a, BigInteger b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithUserException(boolean flag, EmptyException a, Number b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithUserlibException(boolean flag, ABIException a, BigInteger b) {
        return (flag ? a : b).toString();
    }

    public String combineJclWithUserlibException(boolean flag, ABIException a, Number b) {
        return (flag ? a : b).toString();
    }

    public Object combineJclWithNull(boolean flag, BigInteger a) {
        return flag ? a : null;
    }

    public Object combineJclWithNull(boolean flag, Number a) {
        return flag ? a : null;
    }

    private static class EmptyIterator implements Iterator {
        @Override
        public boolean hasNext() {
            return false;
        }
        @Override
        public Object next() {
            return null;
        }
    }

    private static abstract class EmptyException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

}

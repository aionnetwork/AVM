package org.aion.avm.core.unification;

import avm.Result;
import java.math.BigInteger;
import org.aion.avm.core.unification.CommonSuperClassTypes.RootA;
import org.aion.avm.userlib.AionBuffer;
import org.aion.avm.userlib.AionMap;

public class CommonSuperClassTarget_combineWithUserlib {

    private enum EmptyEnum {}

    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    public static Object combineUserlibWithUserClass(boolean flag, AionMap a, CommonSuperClassTarget_combineWithUserlib b) {
        return flag ? a : b;
    }

    public static Object combineUserlibWithUserInterface(boolean flag, AionMap a, RootA b) {
        return flag ? a : b;
    }

    public static Object combineUserlibWithArrayOfClass(boolean flag, CommonSuperClassTarget_combineWithApi[] a, AionMap b) {
        return flag ? a : b;
    }

    public static Object combineUserlibWithArrayOfInterface(boolean flag, RootA[][][] a, AionMap b) {
        return flag ? a : b;
    }

    public static Object combineUserlibWithJclException(boolean flag, NullPointerException a, AionMap b) {
        return flag ? a : b;
    }

    public static Object combineUserlibWithUserException(boolean flag, EmptyException a, AionMap b) {
        return flag ? a : b;
    }

    public static Object combineUserlibWithEnum(boolean flag, EmptyEnum a, AionMap b) {
        return flag ? a : b;
    }

    public static String combineUserlibWithUserlib(boolean flag, AionBuffer a, AionMap b) {
        return (flag ? a : b).toString();
    }

    public static Object combineUserlibWithJclClass(boolean flag, BigInteger a, AionMap b) {
        return (flag ? a : b).toString();
    }

    public static Object combineUserlibWithJclInterface(boolean flag, Number a, AionMap b) {
        return flag ? a : b;
    }

    public static Object combineUserlibWithApi(boolean flag, Result a, AionMap b) {
        return flag ? a : b;
    }

    public static Object combineUserlibWithNull(boolean flag, AionMap a) {
        return flag ? a : null;
    }

    private static abstract class EmptyException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

}

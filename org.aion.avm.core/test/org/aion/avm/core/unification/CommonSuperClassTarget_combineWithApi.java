package org.aion.avm.core.unification;

import avm.Blockchain;
import avm.Result;
import java.math.BigInteger;
import org.aion.avm.core.unification.CommonSuperClassTypes.RootA;
import org.aion.avm.userlib.AionBuffer;

public class CommonSuperClassTarget_combineWithApi {

    private enum EmptyEnum {}

    // The associated test only checks that deployment succeeds, so main() can return null
    public static byte[] main() {
        return null;
    }

    public static Object combineApiWithUserClass(boolean flag, CommonSuperClassTarget_combineWithApi a, Result b) {
        return flag ? a : b;
    }

    public static Object combineApiWithUserInterface(boolean flag, RootA a, Result b) {
        return flag ? a : b;
    }

    public static Object combineApiWithArrayOfClass(boolean flag, CommonSuperClassTarget_combineWithApi[] a, Result b) {
        return flag ? a : b;
    }

    public static Object combineApiWithArrayOfInterface(boolean flag, RootA[][][] a, Result b) {
        return flag ? a : b;
    }

    public static Object combineApiWithJclException(boolean flag, NullPointerException a, Result b) {
        return flag ? a : b;
    }

    public static Object combineApiWithUserException(boolean flag, EmptyException a, Result b) {
        return flag ? a : b;
    }

    public static Object combineApiWithEnum(boolean flag, EmptyEnum a, Result b) {
        return flag ? a : b;
    }

    public static Object combineApiWithUserlib(boolean flag, AionBuffer a, Result b) {
        return flag ? a : b;
    }

    public static Object combineApiWithJclClass(boolean flag, BigInteger a, Result b) {
        return (flag ? a : b).toString();
    }

    public static Object combineApiWithJclInterface(boolean flag, Number a, Result b) {
        return flag ? a : b;
    }

    public static String combineApiWithApi(boolean flag, Result a, Blockchain b) {
        return (flag ? a : b).toString();
    }

    public static Object combineApiWithNull(boolean flag, Blockchain runtime) {
        return flag ? runtime : null;
    }

    private static abstract class EmptyException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

}

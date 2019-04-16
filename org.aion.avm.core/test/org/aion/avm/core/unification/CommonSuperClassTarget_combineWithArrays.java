package org.aion.avm.core.unification;

import avm.Blockchain;
import java.math.BigInteger;
import org.aion.avm.userlib.AionBuffer;

public class CommonSuperClassTarget_combineWithArrays {

    private enum emptyEnum {}

    public static byte[] main() {
        return null;
    }

    public static String combineOverlappingArrays(boolean flag, CommonSuperClassTypes.ClassRoot[] root, CommonSuperClassTypes.ClassChild[] child) {
        return (flag ? root : child)[0].getClassRoot();
    }

    public static String combineOverlappingArrays(boolean flag, CommonSuperClassTypes.RootA[] root, CommonSuperClassTypes.ChildA[] child) {
        return (flag ? root : child)[0].getRootA();
    }

    public static String combineOverlappingArrays(boolean flag, CommonSuperClassTypes.RootA[][][] root, CommonSuperClassTypes.ChildA[][][][] child) {
        return (flag ? root[0][0][0] : child[0]).toString();
    }

    public static String combineArrayAndException(boolean flag, CommonSuperClassTypes.RootA[] root, RuntimeException exception) {
        return (flag ? root[0] : exception).toString();
    }

    public static String combineArrayAndUserCode(boolean flag, CommonSuperClassTypes.RootA[] root, CommonSuperClassTarget_combineWithArrays object) {
        return (flag ? root[0] : object).toString();
    }

    public static String combineArrayAndJclCode(boolean flag, CommonSuperClassTypes.RootA[] root, BigInteger object) {
        return (flag ? root[0] : object).toString();
    }

    public static String combineArrayAndEnum(boolean flag, CommonSuperClassTypes.RootA[] root, emptyEnum emptyEnum) {
        return (flag ? root[0] : emptyEnum).toString();
    }

    public static String combineArrayAndApi(boolean flag, CommonSuperClassTypes.RootA[] root, Blockchain runtime) {
        return (flag ? root[0] : runtime).toString();
    }

    public static String combineArrayAndUserlib(boolean flag, CommonSuperClassTypes.RootA[] root, AionBuffer buffer) {
        return (flag ? root[0] : buffer).toString();
    }

    public static Object combineNull(boolean flag, CommonSuperClassTypes.RootA[] root) {
        return flag ? root : null;
    }
}

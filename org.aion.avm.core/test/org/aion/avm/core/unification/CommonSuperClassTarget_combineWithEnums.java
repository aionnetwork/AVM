package org.aion.avm.core.unification;

import avm.Blockchain;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.aion.avm.core.unification.CommonSuperClassTypes.ClassChild;
import org.aion.avm.core.unification.CommonSuperClassTypes.EnumA1;
import org.aion.avm.core.unification.CommonSuperClassTypes.EnumA2;
import org.aion.avm.core.unification.CommonSuperClassTypes.RootA;
import org.aion.avm.userlib.AionList;

public class CommonSuperClassTarget_combineWithEnums {

    private enum EmptyEnum1 { ME1 }

    private enum EmptyEnum2 { ME2 }

    public static byte[] main() {
        String string = combineDifferentEnums(true, EmptyEnum1.ME1, EmptyEnum2.ME2);
        Blockchain.println(string + " -> " + string.getClass().getName());

        Enum e = combineShadowEnums(true, TimeUnit.MINUTES, RoundingMode.HALF_UP);
        TimeUnit unit = (TimeUnit) e;
        unit.toSeconds(50);

        return null;
    }

    public static String combineDifferentEnums(boolean flag, EmptyEnum1 emptyEnum1, EmptyEnum2 emptyEnum2) {
        return (flag ? emptyEnum1 : emptyEnum2).name();
    }

    public static String combineDifferentEnums2(boolean flag, EmptyEnum1 emptyEnum1, EmptyEnum2 emptyEnum2) {
        return (flag ? emptyEnum1 : emptyEnum2).toString();
    }

    public static RootA combineCommonEnums(boolean flag, EnumA1 a1, EnumA2 a2) {
        return flag ? a1 : a2;
    }

    public static Object combineJcl(boolean flag, EmptyEnum1 emptyEnum1, List list) {
        return flag ? emptyEnum1 : list;
    }

    public static Object combineApi(boolean flag, EmptyEnum1 emptyEnum1, Blockchain runtime) {
        return flag ? emptyEnum1 : runtime;
    }

    public static Object combineUserlib(boolean flag, EmptyEnum1 emptyEnum1, AionList list) {
        return flag ? emptyEnum1 : list;
    }

    public static Object combineException(boolean flag, EmptyEnum1 emptyEnum1, NullPointerException e) {
        return flag ? emptyEnum1 : e;
    }

    public static Object combineArray(boolean flag, EmptyEnum1 emptyEnum1, NullPointerException[] e) {
        return flag ? emptyEnum1 : e;
    }

    public static String combineInterface(boolean flag, EmptyEnum1 emptyEnum1, RootA a) {
        return (flag ? emptyEnum1 : a).toString();
    }

    public static String combineClass(boolean flag, EmptyEnum1 emptyEnum1, ClassChild klazz) {
        return (flag ? emptyEnum1 : klazz).toString();
    }

    public static Enum combineShadowEnums(boolean flag, TimeUnit unit, RoundingMode mode) {
        return flag ? unit : mode;
    }

    public static Object combineNull(boolean flag, EmptyEnum1 emptyEnum1) {
        return flag ? emptyEnum1 : null;
    }
}

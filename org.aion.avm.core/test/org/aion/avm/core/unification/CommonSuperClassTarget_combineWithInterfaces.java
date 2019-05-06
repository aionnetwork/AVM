package org.aion.avm.core.unification;

import avm.Address;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import org.aion.avm.core.unification.CommonSuperClassTypes.EnumA1;
import org.aion.avm.core.unification.CommonSuperClassTypes.EnumB;
import org.aion.avm.core.unification.CommonSuperClassTypes.RootA;
import org.aion.avm.core.unification.CommonSuperClassTypes.RootB;
import org.aion.avm.core.unification.CommonSuperClassTypes.SubRootA1;
import org.aion.avm.core.unification.CommonSuperClassTypes.SubRootA1Child;
import org.aion.avm.core.unification.CommonSuperClassTypes.SubRootA2;
import org.aion.avm.core.unification.CommonSuperClassTypes.SubRootA2Child;
import org.aion.avm.core.unification.CommonSuperClassTypes.SubSubRootA1;
import org.aion.avm.core.unification.CommonSuperClassTypes.SubSubRootA1Child;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.abi.ABIEncoder;

public class CommonSuperClassTarget_combineWithInterfaces {

    private enum EmptyEnum { ME }

    public static byte[] main() {
        touchEachMethod();
        return null;
    }

    /**
     * We just touch each of the methods below by calling them and doing something with their returned value
     * to ensure no weirdness has come about from the tightest super class algorithm.
     */
    public static void touchEachMethod() {
        AionList list = new AionList();

        combineClassAndInterface(false, new CommonSuperClassTarget_combineWithInterfaces(), EnumA1.ME).length();
        combineClassAndJclInterface(true, new CommonSuperClassTarget_combineWithInterfaces(), list.iterator()).length();
        ((Iterator<?>) combineJclInterfaces(true, list.iterator(), list)).hasNext();
        combineRelatedInterfaces(true, new SubSubRootA1Child(), new SubRootA2Child()).getRootA();
        ((SubRootA1) combineUnrelatedInterfaces(true, new SubRootA1Child(), EnumA1.ME)).getRootA();
        combineA(false, new int[]{ 1 }, new byte[]{ 0x1 }).length();
        combineB(true, new int[]{ 2 } , new int[]{ 3 }).clone();
        combineA(false, new int[3][], new byte[7][]).length();
        combineB(false, new int[3][][], new int[5][][]).clone();
        ((int[]) combineC(true, new int[3], new int[6][][])).clone();
        ((int[]) combineD(true, new int[4], new byte[7][][])).clone();
        combineE(false, new Integer[3], new Byte[2]).length();
        ((Comparable[]) combineF(true, new Comparable[3], new Serializable[2])).clone();
        ((Comparable[]) combineG(true, new Comparable[3], new Serializable[2][])).clone();
        ((SubRootA1[]) combineH(true, new SubRootA1Child[2], new SubRootA2Child[5])).clone();
        combineI(true, new int[2][], new Serializable[3][][]).length();
        ((SubRootA2[]) combineInterfaceWithArrays1(false, new SubSubRootA1[3], new SubRootA2Child[5])).clone();
        combineInterfaceWithArrays2(true, new SubRootA1Child(), new Integer[3][]).length();

        // Returns EmptyException[0], which is null.
        try {
            ((EmptyException[]) combineInterfaceWithException1(false, new SubRootA1Child(), new EmptyException[3])).clone();
        } catch (NullPointerException e) {}

        combineInterfaceWithException2(true, new SubRootA2Child(), new EmptyException()).toString();
        combineInterfaceWithEnum(false, EnumB.ME, EmptyEnum.ME).length();
        ((Address) combineInterfaceWithApi(false, EnumB.ME, new Address(new byte[32]))).toByteArray();
        combineInterfaceWithUserlib(true, EnumA1.ME, null).toString();
    }

    public static String combineClassAndInterface(boolean flag, CommonSuperClassTarget_combineWithInterfaces a, RootA b) {
        return (flag ? a : b).toString();
    }

    public static String combineClassAndJclInterface(boolean flag, CommonSuperClassTarget_combineWithInterfaces a, Iterator<?> b) {
        return (flag ? a : b).toString();
    }

    public static Object combineJclInterfaces(boolean flag, Iterator<?> a, List<?> b) {
        return flag ? a : b;
    }

    public static RootA combineRelatedInterfaces(boolean flag, SubSubRootA1 a, SubRootA2 b) {
        return flag ? a : b;
    }

    public static Object combineUnrelatedInterfaces(boolean flag, SubRootA1 a, RootB b) {
        return flag ? a : b;
    }

    public static String combineA(boolean flag, int[] a, byte[] b) {
        return (flag ? a : b).toString();
    }

    public static int[] combineB(boolean flag, int[] a, int[] b) {
        return flag ? a : b;
    }

    public static String combineA(boolean flag, int[][] a, byte[][] b) {
        return (flag ? a : b).toString();
    }

    public static int[][][] combineB(boolean flag, int[][][] a, int[][][] b) {
        return flag ? a : b;
    }

    public static Object combineC(boolean flag, int[] a, int[][][] b) {
        return flag ? a : b;
    }

    public static Object combineD(boolean flag, int[] a, byte[][][] b) {
        return flag ? a : b;
    }

    public static String combineE(boolean flag, Integer[] a, Byte[] b) {
        return (flag ? a : b).toString();
    }

    public static Object combineF(boolean flag, Comparable[] a, Serializable[] b) {
        return flag ? a : b;
    }

    public static Object combineG(boolean flag, Comparable[] a, Serializable[][] b) {
        return flag ? a : b;
    }

    public static Object combineH(boolean flag, SubRootA1[] a, SubRootA2[] b) {
        return flag ? a : b;
    }

    public static String combineI(boolean flag, int[][] a, Serializable[][][] b) {
        return (flag ? a : b).toString();
    }

    public static RootA[] combineInterfaceWithArrays1(boolean flag, SubSubRootA1[] a, SubRootA2[] b) {
        return flag ? a : b;
    }

    public static String combineInterfaceWithArrays2(boolean flag, SubRootA1 a, Integer[][] b) {
        return (flag ? a : b).toString();
    }

    public static Object combineInterfaceWithException1(boolean flag, RootA a, EmptyException[] b) {
        return flag ? a : b[0];
    }

    public static Object combineInterfaceWithException2(boolean flag, RootA a, EmptyException b) {
        return flag ? a : b;
    }

    public static String combineInterfaceWithEnum(boolean flag, RootB a, EmptyEnum b) {
        return (flag ? a : b).toString();
    }

    public static Object combineInterfaceWithApi(boolean flag, RootB a, Address b) {
        return flag ? a : b;
    }

    public static Object combineInterfaceWithUserlib(boolean flag, RootA a, ABIEncoder b) {
        return (flag ? a : b).toString();
    }

    private static class EmptyException extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}

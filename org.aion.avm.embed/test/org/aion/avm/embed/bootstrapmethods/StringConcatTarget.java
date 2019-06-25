package org.aion.avm.embed.bootstrapmethods;

import avm.Address;
import org.aion.avm.tooling.abi.Callable;

public class StringConcatTarget {

    @Callable
    public static boolean concat(){
        String s = "";
        for(int i = 0; i< 400; i++){
            s += "a";
        }
        return true;
    }

    @Callable
    public static boolean concatWithPrimitiveArray() {
        int[] arr = new int[10];
        String s = "" + arr;
        return true;
    }

    @Callable
    public static boolean concatWithArrays() {
        int[] arr = new int[10];
        boolean[] arr2 = new boolean[10];
        String[] arr3 = new String[10];
        byte[] arr4 = new byte[10];

        String s = "" + arr + arr2 + arr3 + arr4;
        return true;
    }

    @Callable
    public static boolean concatWithObjectArray() {
        Object[] arr = new Object[10];
        String s = "" + arr;
        return true;
    }

    @Callable
    public static boolean concatWithInterfaceArray() {
        MyInterface[] arr = new MyInterface[10];
        String s = "" + arr;
        return true;
    }

    @Callable
    public static boolean concatWithUserDefinedArray() {
        MyClass[] arr = new MyClass[10];
        String s = "" + arr;
        return true;
    }

    @Callable
    public static boolean concatWithMultiDimArray() {
        boolean[][] arr = new boolean[10][];
        String s = "" + arr;
        return true;
    }

    @Callable
    public static boolean concatWithDynamicBoolean(){
        String s = " " + concatWithMultiDimArray();
        return true;
    }

    @Callable
    public static boolean concatWithAddress(Address addr){
        String s = " " + addr;
        return true;
    }

    public interface MyInterface { }
    public class MyClass { }

}

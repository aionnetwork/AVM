package org.aion.avm.tooling.shadowing.testClass;

import org.aion.avm.api.Address;
import org.aion.avm.tooling.abi.Callable;

public class TestResource {

    @Callable
    public static String getClassName1DIntArray(){
        int[] a = new int[10];
        return a.getClass().getName();
    }

    @Callable
    public static String getClassName2DBooleanArray(){
        boolean [][] b = new boolean[10][];
        return b.getClass().getName();
    }

    @Callable
    public static String getClassName1DObjectArray(){
        Object[] o = new Object[10];
        return o.getClass().getName();
    }

    @Callable
    public static String getClassName3DObjectArray(){
        Object[][][] o = new Object[10][][];
        return o.getClass().getName();
    }

    @Callable
    public static String getClassName1DUserDefinedArray(){
        Child[] o = new Child[10];
        return o.getClass().getName();
    }

    @Callable
    public static String getClassNameUserDefined(){
        Child o = new Child();
        return o.getClass().getName();
    }

    @Callable
    public static String getClassNameUserDefined2(int[][] i){
        return i.getClass().getName();
    }

    @Callable
    public static String getClassNameThrowableArray(){
        Throwable[] t = new Throwable[10];
        return t.getClass().getName();
    }

    @Callable
    public static String getClassNameAssertionErrorArray(){
        AssertionError[] t = new AssertionError[10];
        return t.getClass().getName();
    }

    @Callable
    public static String getClassName1DStringArray(){
        String[] t = new String[10];
        return t.getClass().getName();
    }

    @Callable
    public static String getClassName2DStringArray(){
        String[][] t = new String[10][];
        return t.getClass().getName();
    }

    @Callable
    public static String getClassNameAddressArray(){
        Address[] a = new Address[10];
        return a.getClass().getName();
    }

    @Callable
    public static String getClassNameThrowable(){
        Throwable t = new Throwable();
        return t.getClass().getName();
    }

    @Callable
    public static String getClassNameBoolean(){
        Boolean b = true;
        return b.getClass().getName();
    }

    @Callable
    public static String getObjectToString(){
        Child o = new Child();
        return o.toString();
    }

    @Callable
    public static String getInterfaceName(){
        return MyInterface.class.getName();
    }

    @Callable
    public static String getInterfaceArrayName(){
        MyInterface[] o = new MyInterface[10];
        return o.getClass().getName();
    }

    @Callable
    public static String getObjectImplementInterfaceName(){
        MyChild[] o = new MyChild[10];
        return o.getClass().getName();
    }

    @Callable
    public static String getClassName1DArrayParam(int[] a){
        return a.getClass().getName();
    }

    @Callable
    public static String getClassName2DArrayParam(int[][] a){
        return a.getClass().getName();
    }

    public static class Child {
        @Override
        public int hashCode() {
            return 20;
        }
    }

    public interface MyInterface{}
    public static class MyChild implements MyInterface{}


}

package org.aion.avm.embed.arraywrapping;

import org.aion.avm.tooling.abi.Callable;

public class ReferenceArrayTarget {

    @Callable
    public static void twoDimArraySize() {
        int[][] a = new int[10][];
        a[0] = new int[3];
        a[1] = new int[6];
        a[2] = new int[100];

        validate(a.length, 10);
        validate(a[0].length, 3);
        validate(a[1].length, 6);
        validate(a[2].length, 100);

        // to create a new code block
        validate(1,1);
    }

    @Callable
    public static void twoDimArraySize2() {
        int[][] a = new int[10][5];

        validate(a.length, 10);
        validate(a[0].length, 5);

        a[1] = new int[100];
        validate(a[1].length, 100);

        // to create a new code block
        validate(1,1);
    }

    @Callable
    public static void multiDimArraySize() {
        int[][][] a = new int[10][][];
        a[0] = new int[3][];
        a[1] = new int[6][];
        a[2] = new int[100][1];

        a[0][0] = new int[5];

        validate(a.length, 10);
        validate(a[0].length, 3);
        validate(a[0][0].length, 5);

        validate(a[1].length, 6);

        validate(a[2].length, 100);
        validate(a[2][0].length, 1);

        int[][][] b = new int[10][20][];
        b[0][0] = new int [30];

        validate(b.length, 10);
        validate(b[0].length, 20);
        validate(b[0][0].length, 30);

        // to create a new code block
        validate(1,1);
    }

    @Callable
    public static void twoDimArrayAccess() {
        int[][] a = new int[10][];
        int[][] b = new int[10][10];

        if (a[0] != null) {
            throw new RuntimeException("int[10][] array initialization values are not null.");
        }

        if (b[0][0] != 0) {
            throw new RuntimeException("int[10][10] array initialization values are not 0.");
        }

        // to create a new code block
        validate(1,1);
    }

    @Callable
    public static void multiDimArrayAccess() {
        int[][][] a = new int[10][][];
        int[][][] b = new int[10][20][];
        int[][][] c = new int[10][20][30];

        if (a[0] != null) {
            throw new RuntimeException("int[10][][] array initialization values are not null.");
        }

        if (b[0][0] != null) {
            throw new RuntimeException("int[10][20][] array initialization values are not null.");
        }

        if (c[0][0][0] != 0) {
            throw new RuntimeException("int[10][20][30] array initialization values are not 0.");
        }

        // to create a new code block
        validate(1,1);
    }

    @Callable
    public static void InterfaceArraySize(){
        ChildInterfaceOne[][] childInterfacesOne = new ChildInterfaceOne[10][];
        CommonInterface[][] childInterfacesTwo = new ChildInterfaceTwo[10][];

        validate(childInterfacesOne.length, 10);
        validate(childInterfacesTwo.length, 10);

        childInterfacesTwo[0] = new ChildInterfaceTwo[20];
        validate(childInterfacesTwo[0].length, 20);
    }

    @Callable
    public static void InterfaceArrayAccess(){
        ChildInterfaceOne[][] childInterfacesOne = new ChildInterfaceOne[10][];
        CommonInterface[][] childInterfacesTwo = new ChildInterfaceTwo[10][20];

        if (childInterfacesOne[0] != null) {
            throw new RuntimeException("interface[10][] array initialization values are not null.");
        }

        if (childInterfacesTwo[0][0] != null) {
            throw new RuntimeException("interface[10][20] array initialization values are not 0.");
        }
    }

    @Callable
    public static void ObjectArraySize(){
        ConcreteChildOne[][] childOne = new ConcreteChildOne[10][];
        ChildInterfaceTwo[][] childTwo = new ConcreteChildTwo[10][];

        validate(childOne.length, 10);
        validate(childTwo.length, 10);

        childTwo[0] = new ConcreteChildTwo[20];
        validate(childTwo[0].length, 20);
    }

    @Callable
    public static void ObjectArrayAccess(){
        ConcreteChildOne[][] childOne = new ConcreteChildOne[10][];
        ChildInterfaceTwo[][] childTwo = new ConcreteChildTwo[10][10];

        if (childOne[0] != null) {
            throw new RuntimeException("object[10][] array initialization values are not null.");
        }

        if (childTwo[0][0] != null) {
            throw new RuntimeException("object[10][20] array initialization values are not 0.");
        }
    }

    private static void validate(int expected, int actual) {
        if (expected != actual)
            throw new RuntimeException(expected + " was not equal to " + actual);
    }

    public interface CommonInterface{}
    public interface ChildInterfaceOne extends CommonInterface{}
    public interface ChildInterfaceTwo extends CommonInterface{}
    public static class ConcreteChildOne implements ChildInterfaceOne {}
    public static class ConcreteChildTwo implements ChildInterfaceTwo {}

}

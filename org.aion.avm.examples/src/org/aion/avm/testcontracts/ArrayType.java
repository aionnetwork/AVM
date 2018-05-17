package org.aion.avm.testcontracts;

import java.lang.reflect.Array;

public class ArrayType {
    public static void main(String[] args) {
        byte[] x = new byte[0];
        System.out.println((Object) x instanceof Array);
    }
}

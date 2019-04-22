package org.aion.avm.tooling.arraywrapping;

import org.aion.avm.tooling.abi.Callable;

public class InvalidDimensionPrimitiveTarget {

    @Callable
    public static void initArray(){
        int [][][][] a = new int [10][][][];
    }
}

package org.aion.avm.embed.arraywrapping;

import org.aion.avm.tooling.abi.Callable;

public class InvalidDimensionPrimitiveTarget {

    @Callable
    public static void initArray(){
        int [][][][] a = new int [10][][][];
    }
}

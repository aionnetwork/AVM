package org.aion.avm.tooling.arraywrapping;

import org.aion.avm.tooling.abi.Callable;

public class InvalidDimensionObjectTarget {

    @Callable
    public static void initArray(){
        String [][][][] a = new String[10][][][];
    }
}

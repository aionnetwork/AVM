package org.aion.avm.embed.arraywrapping;

import org.aion.avm.tooling.abi.Callable;

import avm.Blockchain;

public class InvalidDimensionObjectTarget {

    @Callable
    public static void initArray(){
        String [][][][] a = new String[10][][][];
        Blockchain.require(null != a);
    }
}

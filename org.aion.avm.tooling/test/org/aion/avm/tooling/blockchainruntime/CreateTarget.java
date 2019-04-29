package org.aion.avm.tooling.blockchainruntime;

import avm.Blockchain;
import avm.Result;
import org.aion.avm.tooling.abi.Callable;

import java.math.BigInteger;

public class CreateTarget {

    @Callable
    public static void createContracts(byte[] data){
        for(int i =0 ; i< 20; i++) {
            Result createResult = Blockchain.create(BigInteger.ZERO, data, 600000);
            Blockchain.require(createResult.isSuccess());
        }
    }
}

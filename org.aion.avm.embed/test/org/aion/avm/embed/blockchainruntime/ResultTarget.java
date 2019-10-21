package org.aion.avm.embed.blockchainruntime;

import avm.Blockchain;
import avm.Result;
import org.aion.avm.tooling.abi.Callable;

import java.math.BigInteger;

public class ResultTarget {

    @Callable
    public static void testHashCode(){
        Result result = Blockchain.call(Blockchain.getCaller(), BigInteger.ZERO, new byte[0], Blockchain.getRemainingEnergy());
        result.hashCode();
    }

    @Callable
    public static void testEquality(byte[] data){
        Result result1 = Blockchain.call(Blockchain.getCaller(), BigInteger.ZERO, new byte[0], Blockchain.getRemainingEnergy());
        Result result2 = Blockchain.call(Blockchain.getCaller(), BigInteger.ZERO, new byte[0], Blockchain.getRemainingEnergy());

        Blockchain.require(result1.equals(result2));

        result2 = Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data,  Blockchain.getRemainingEnergy());
        Blockchain.require(!result1.equals(result2));

        result1 = Blockchain.call(Blockchain.getAddress(), BigInteger.ZERO, data,  Blockchain.getRemainingEnergy());
        Blockchain.require(result1.equals(result2));
    }

    @Callable
    public static int returnInt(){
        return 10;
    }
}

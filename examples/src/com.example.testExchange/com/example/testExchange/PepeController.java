package com.example.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class PepeController {

    private static ERC20 coinbase;

    public static void init(){
        coinbase = new ERC20Token("Pepe", "PEPE", 8, BlockchainRuntime.getSender());
    }

    public static byte[] main(){
        return ABIDecoder.decodeAndRun(coinbase, BlockchainRuntime.getData());
    }
}

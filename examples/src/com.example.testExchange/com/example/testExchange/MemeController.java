package com.example.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class MemeController {

    private static IAionToken coinbase;

    public static void init(){
        coinbase = new MemeCoin(BlockchainRuntime.getSender());
    }

    public static byte[] main(){
        return ABIDecoder.decodeAndRun(coinbase, BlockchainRuntime.getData());
    }
}


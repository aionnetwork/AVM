package com.example.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class CoinController {

    private static ERC20 coinbase;

    public static void init(char[] name, char[] symbol, int decimals){
        coinbase = new ERC20Token(new String(name), new String(symbol), decimals, BlockchainRuntime.getSender());
    }

    public static byte[] main(){
        return ABIDecoder.decodeAndRun(coinbase, BlockchainRuntime.getData());
    }
}

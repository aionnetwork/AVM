package com.example.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class CoinController {

    private static ERC20 coinbase;

    static {
        Object[] arguments = ABIDecoder.decodeArguments(BlockchainRuntime.getData());
        coinbase = new ERC20Token(new String((char[]) arguments[0]), new String((char[]) arguments[1]), (int) arguments[2], BlockchainRuntime.getSender());
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRun(coinbase, BlockchainRuntime.getData());
    }
}

package com.example.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class ExchangeController {

    private static Exchange base;

    public static void init() {
        base = new Exchange();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRun(base, BlockchainRuntime.getData());
    }
}

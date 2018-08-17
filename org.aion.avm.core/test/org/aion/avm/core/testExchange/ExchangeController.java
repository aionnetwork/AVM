package org.aion.avm.core.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class ExchangeController {

    private static Exchange base = new Exchange();

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(base, BlockchainRuntime.getData());
    }
}
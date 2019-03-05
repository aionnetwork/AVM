package org.aion.avm.tooling.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class ExchangeController {

    static {
        Exchange.init();
    }

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(Exchange.class, BlockchainRuntime.getData());
    }
}
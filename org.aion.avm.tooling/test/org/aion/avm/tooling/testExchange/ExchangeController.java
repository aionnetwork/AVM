package org.aion.avm.tooling.testExchange;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class ExchangeController {

    static {
        Exchange.init();
    }

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("processExchangeTransaction")) {
                return ABIEncoder.encodeOneObject(Exchange.processExchangeTransaction());
            } else if (methodName.equals("requestTransfer")) {
                return ABIEncoder.encodeOneObject(Exchange.requestTransfer(decoder.decodeOneCharacterArray(), decoder.decodeOneAddress(), decoder.decodeOneLong()));
            } else if (methodName.equals("listCoin")) {
                return ABIEncoder.encodeOneObject(Exchange.listCoin(decoder.decodeOneCharacterArray(), decoder.decodeOneAddress()));
            } else {
                return new byte[0];
            }
        }
    }
}
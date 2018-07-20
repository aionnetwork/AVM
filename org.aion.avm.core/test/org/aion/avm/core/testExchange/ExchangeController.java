package org.aion.avm.core.testExchange;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.core.testWallet.ByteArrayHelpers;

public class ExchangeController {

    private static Exchange base;

    public static void init(){
        base = new Exchange();
    }

    public static byte[] main() {
        byte[] result = new byte[0];
        byte[] input = BlockchainRuntime.getData();
        ExchangeABI.Decoder decoder = ExchangeABI.buildDecoder(input);
        byte methodByte = decoder.decodeByte();

        switch (methodByte) {
            case ExchangeABI.kExchange_listCoin:
                // We know that this is int (length), Address(*length), int, long.;
                String name = decoder.decodeString(4);
                Address contractAddr = decoder.decodeAddress();
                result = ByteArrayHelpers.encodeBoolean(base.listCoin(name, contractAddr));
                break;
            case ExchangeABI.kExchange_requestTransfer:
                String coin = decoder.decodeString(4);
                Address to = decoder.decodeAddress();
                long amount = decoder.decodeLong();
                result = ByteArrayHelpers.encodeBoolean(base.requestTransfer(coin, to, amount));
                break;
            case ExchangeABI.kExchange_processExchangeTransaction:
                result = ByteArrayHelpers.encodeBoolean(base.processExchangeTransaction());
                break;
        }

        return result;
    }

}

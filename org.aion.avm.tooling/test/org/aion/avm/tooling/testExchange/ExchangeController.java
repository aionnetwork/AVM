package org.aion.avm.tooling.testExchange;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class ExchangeController {

    static {
        Exchange.init();
    }

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            Object[] argValues = ABIDecoder.decodeArguments(inputBytes);
            if (methodName.equals("processExchangeTransaction")) {
                return ABIEncoder.encodeOneObject(Exchange.processExchangeTransaction());
            } else if (methodName.equals("requestTransfer")) {
                return ABIEncoder.encodeOneObject(Exchange.requestTransfer((char[]) argValues[0], (Address) argValues[1], (Long) argValues[2]));
            } else if (methodName.equals("listCoin")) {
                return ABIEncoder.encodeOneObject(Exchange.listCoin((char[]) argValues[0], (Address) argValues[1]));
            } else {
                return new byte[0];
            }
        }
    }
}
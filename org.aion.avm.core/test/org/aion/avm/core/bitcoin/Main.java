package org.aion.avm.core.bitcoin;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class Main {

    public static byte[] main() {
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            Object[] argValues = ABIDecoder.decodeArguments(inputBytes);
            if (methodName.equals("addBlock")) {
                return ABIEncoder.encodeOneObject(Blockchain.addBlock((byte[])argValues[0]));
            } else {
                return new byte[0];
            }
        }
    }
}

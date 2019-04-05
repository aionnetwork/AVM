package org.aion.avm.core.bitcoin;

import avm.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class Main {

    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("addBlock")) {
                return ABIEncoder.encodeOneBoolean(Blockchain.addBlock(decoder.decodeOneByteArray()));
            } else {
                return new byte[0];
            }
        }
    }
}

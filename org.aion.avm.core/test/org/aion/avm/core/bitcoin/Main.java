package org.aion.avm.core.bitcoin;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class Main {

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithClass(Blockchain.class, BlockchainRuntime.getData());
    }
}

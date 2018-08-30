package org.aion.avm.core.bitcoin;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.BlockchainRuntime;

public class Main {

    private static Blockchain chain = new Blockchain(new Genesis(0, new byte[32], new byte[32], 0, 0, 0));

    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(chain, BlockchainRuntime.getData());
    }
}

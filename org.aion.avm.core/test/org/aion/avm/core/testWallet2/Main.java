package org.aion.avm.core.testWallet2;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.InvalidTxDataException;

public class Main {

    private static Wallet wallet;

    static {
        // TODO: parse arguments from transaction data
        Address[] owners = {
                BlockchainRuntime.getSender(),
                new Address(new byte[32]),
                new Address(new byte[32])
        };
        int confirmationsRequired = 4;

        wallet = new Wallet(owners, confirmationsRequired);
    }

    public static byte[] main() throws InvalidTxDataException {
        return ABIDecoder.decodeAndRun(wallet, BlockchainRuntime.getData());
    }
}

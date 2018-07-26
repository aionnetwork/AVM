package org.aion.avm.core.testWallet2;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.api.InvalidTxDataException;

public class Main {

    private static Wallet wallet;

    static {
        Object[] arguments = ABIDecoder.decodeArguments(BlockchainRuntime.getData());
        Address owner1 = (Address) arguments[0];
        Address owner2 = (Address) arguments[1];
        int confirmationsRequired = (int) arguments[2];
        Address[] owners = {
                BlockchainRuntime.getSender(),
                owner1,
                owner2
        };

        wallet = new Wallet(owners, confirmationsRequired);
    }

    public static byte[] main() throws InvalidTxDataException {
        return ABIDecoder.decodeAndRun(wallet, BlockchainRuntime.getData());
    }
}

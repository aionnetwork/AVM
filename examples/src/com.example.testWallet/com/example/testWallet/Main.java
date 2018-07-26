package com.example.testWallet;

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
                new Address(new byte[32]),
                new Address(new byte[32])
        };

        wallet = new Wallet(owners, confirmationsRequired);
       // TODO - here the code refers to runtime space ABIDecoder so this exception is thrown. However this exception needs to be unchecked at least
    }

    public static byte[] main() throws InvalidTxDataException {
        return ABIDecoder.decodeAndRun(wallet, BlockchainRuntime.getData());
    }
}

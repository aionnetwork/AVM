package org.aion.avm.core.poc;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class Main {

    private static Wallet wallet;

    /**
     * Initialization code executed once at the Dapp deployment.
     * Read the transaction data, decode it and construct the wallet instance with the decoded arguments.
     * This wallet instance is transparently put into storage.
     */
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

    /**
     * Entry point at a transaction call.
     * Read the transaction data, decode it and run the specified method of the token class with the decoded arguments.
     * The token instance is loaded transparently from the storage in prior.
     * @return the encoded return data of the method being called.
     */
    public static byte[] main() {
        return ABIDecoder.decodeAndRun(wallet, BlockchainRuntime.getData());
    }
}

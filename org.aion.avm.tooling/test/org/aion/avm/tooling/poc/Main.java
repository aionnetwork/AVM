package org.aion.avm.tooling.poc;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class Main {

    /**
     * Initialization code executed once at the Dapp deployment.
     * Read the transaction data, decode it and construct the wallet instance with the decoded arguments.
     * This wallet instance is transparently put into storage.
     */
    static {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        Address owner1 = decoder.decodeOneAddress();
        Address owner2 = decoder.decodeOneAddress();
        int confirmationsRequired = decoder.decodeOneInteger();
        Address[] owners = {
                BlockchainRuntime.getCaller(),
                owner1,
                owner2
        };

        Wallet.init(owners, confirmationsRequired);
    }

    /**
     * Entry point at a transaction call.
     * Read the transaction data, decode it and run the specified method of the token class with the decoded arguments.
     * The token instance is loaded transparently from the storage in prior.
     * @return the encoded return data of the method being called.
     */
    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(BlockchainRuntime.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("propose")) {
                return ABIEncoder.encodeOneObject(Wallet.propose(decoder.decodeOneAddress(), decoder.decodeOneLong(), decoder.decodeOneByteArray(), decoder.decodeOneLong()));
            } else if (methodName.equals("confirm")) {
                return ABIEncoder.encodeOneObject(Wallet.confirm(decoder.decodeOneByteArray()));
            } else {
                return new byte[0];
            }
        }
    }
}

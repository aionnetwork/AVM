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
        Object[] arguments = ABIDecoder.decodeArguments(BlockchainRuntime.getData());
        Address owner1 = (Address) arguments[0];
        Address owner2 = (Address) arguments[1];
        int confirmationsRequired = (int) arguments[2];
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
        byte[] inputBytes = BlockchainRuntime.getData();
        String methodName = ABIDecoder.decodeMethodName(inputBytes);
        if (methodName == null) {
            return new byte[0];
        } else {
            Object[] argValues = ABIDecoder.decodeArguments(inputBytes);
            if (methodName.equals("propose")) {
                return ABIEncoder.encodeOneObject(Wallet.propose((Address) argValues[0], (Long) argValues[1], (byte[]) argValues[2], (Long) argValues[3]));
            } else if (methodName.equals("confirm")) {
                return ABIEncoder.encodeOneObject(Wallet.confirm((byte[]) argValues[0]));
            } else {
                return new byte[0];
            }
        }
    }
}

package org.aion.avm.core.testExchange;

import org.aion.avm.api.ABIDecoder;
import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class CoinController {

    private static ERC20 token;

    /**
     * Initialization code executed once at the Dapp deployment.
     * Read the transaction data, decode it and construct the token instance with the decoded arguments.
     * This token instance is transparently put into storage.
     */
    static {
        Object[] arguments = ABIDecoder.decodeArguments(BlockchainRuntime.getData());
        String name = new String((char[]) arguments[0]);
        String symbol = new String((char[]) arguments[1]);
        int decimals = (int) arguments[2];
        Address minter = BlockchainRuntime.getCaller();

        token = new ERC20Token(name, symbol, decimals, minter);
    }

    /**
     * Entry point at a transaction call.
     * Read the transaction data, decode it and run the specified method of the token class with the decoded arguments.
     * The token instance is loaded transparently from the storage in prior.
     * @return the encoded return data of the method being called.
     */
    public static byte[] main() {
        return ABIDecoder.decodeAndRunWithObject(token, BlockchainRuntime.getData());
    }
}

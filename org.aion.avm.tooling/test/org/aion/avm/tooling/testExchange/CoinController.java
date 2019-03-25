package org.aion.avm.tooling.testExchange;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class CoinController {

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

        ERC20Token.init(name, symbol, decimals, minter);
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
            if (methodName.equals("balanceOf")) {
                return ABIEncoder.encodeOneObject(ERC20Token.balanceOf((Address) argValues[0]));
            } else if (methodName.equals("transfer")) {
                return ABIEncoder.encodeOneObject(ERC20Token.transfer((Address) argValues[0], (Long) argValues[1]));
            } else if (methodName.equals("approve")) {
                return ABIEncoder.encodeOneObject(ERC20Token.approve((Address) argValues[0], (Long) argValues[1]));
            } else if (methodName.equals("allowance")) {
                return ABIEncoder.encodeOneObject(ERC20Token.allowance((Address) argValues[0], (Address) argValues[1]));
            } else if (methodName.equals("transferFrom")) {
                return ABIEncoder.encodeOneObject(ERC20Token.transferFrom((Address) argValues[0], (Address) argValues[1], (Long) argValues[2]));
            } else if (methodName.equals("totalSupply")) {
                return ABIEncoder.encodeOneObject(ERC20Token.totalSupply());
            } else if (methodName.equals("mint")) {
                return ABIEncoder.encodeOneObject(ERC20Token.mint((Address) argValues[0], (Long) argValues[1]));
            } else {
                return new byte[0];
            }
        }
    }
}

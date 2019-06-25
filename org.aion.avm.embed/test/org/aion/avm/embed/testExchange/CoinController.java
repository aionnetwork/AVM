package org.aion.avm.embed.testExchange;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;
import org.aion.avm.userlib.abi.ABIEncoder;

public class CoinController {

    /**
     * Initialization code executed once at the Dapp deployment.
     * Read the transaction data, decode it and construct the token instance with the decoded arguments.
     * This token instance is transparently put into storage.
     */
    static {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String name = decoder.decodeOneString();
        String symbol = decoder.decodeOneString();
        int decimals = decoder.decodeOneInteger();
        Address minter = Blockchain.getCaller();

        ERC20Token.init(name, symbol, decimals, minter);
    }

    /**
     * Entry point at a transaction call.
     * Read the transaction data, decode it and run the specified method of the token class with the decoded arguments.
     * The token instance is loaded transparently from the storage in prior.
     * @return the encoded return data of the method being called.
     */
    public static byte[] main() {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        String methodName = decoder.decodeMethodName();
        if (methodName == null) {
            return new byte[0];
        } else {
            if (methodName.equals("balanceOf")) {
                return ABIEncoder.encodeOneLong(ERC20Token.balanceOf(decoder.decodeOneAddress()));
            } else if (methodName.equals("transfer")) {
                return ABIEncoder.encodeOneBoolean(ERC20Token.transfer(decoder.decodeOneAddress(), decoder.decodeOneLong()));
            } else if (methodName.equals("approve")) {
                return ABIEncoder.encodeOneBoolean(ERC20Token.approve(decoder.decodeOneAddress(), decoder.decodeOneLong()));
            } else if (methodName.equals("allowance")) {
                return ABIEncoder.encodeOneLong(ERC20Token.allowance(decoder.decodeOneAddress(), decoder.decodeOneAddress()));
            } else if (methodName.equals("transferFrom")) {
                return ABIEncoder.encodeOneBoolean(ERC20Token.transferFrom(decoder.decodeOneAddress(), decoder.decodeOneAddress(), decoder.decodeOneLong()));
            } else if (methodName.equals("totalSupply")) {
                return ABIEncoder.encodeOneLong(ERC20Token.totalSupply());
            } else if (methodName.equals("mint")) {
                return ABIEncoder.encodeOneBoolean(ERC20Token.mint(decoder.decodeOneAddress(), decoder.decodeOneLong()));
            } else {
                return new byte[0];
            }
        }
    }
}

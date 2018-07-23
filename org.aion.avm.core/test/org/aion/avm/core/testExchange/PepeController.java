package org.aion.avm.core.testExchange;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.core.testWallet.ByteArrayHelpers;

public class PepeController {

    private static ERC20 coinbase;

    public static void init(){
        coinbase = new ERC20Token("Pepe", "PEPE", 8, BlockchainRuntime.getSender());
    }

    public static byte[] main(){
        byte[] result = new byte[0];
        byte[] input = BlockchainRuntime.getData();
        ExchangeABI.Decoder decoder = ExchangeABI.buildDecoder(input);
        byte methodByte = decoder.decodeByte();

        switch (methodByte) {
            case ExchangeABI.kToken_totalSupply:
                // We know that this is int (length), Address(*length), int, long.;
                result = ByteArrayHelpers.encodeLong(coinbase.totalSupply());
                break;
            case ExchangeABI.kToken_balanceOf:
                result = ByteArrayHelpers.encodeLong(coinbase.balanceOf(decoder.decodeAddress()));
                break;
            case ExchangeABI.kToken_allowance:
                result = ByteArrayHelpers.encodeLong(coinbase.allowance(decoder.decodeAddress(), decoder.decodeAddress()));
                break;
            case ExchangeABI.kToken_transfer:
                result = ByteArrayHelpers.encodeBoolean(coinbase.transfer(decoder.decodeAddress(), decoder.decodeLong()));
                break;
            case ExchangeABI.kToken_approve:
                result = ByteArrayHelpers.encodeBoolean(coinbase.approve(decoder.decodeAddress(), decoder.decodeLong()));
                break;
            case ExchangeABI.kToken_transferFrom:
                result = ByteArrayHelpers.encodeBoolean(coinbase.transferFrom(decoder.decodeAddress(), decoder.decodeAddress(), decoder.decodeLong()));
                break;
            case ExchangeABI.kToken_mint:
                result = ByteArrayHelpers.encodeBoolean(coinbase.mint(decoder.decodeAddress(), decoder.decodeLong()));
                break;
            default:
                break;
        }
        return result;
    }
}

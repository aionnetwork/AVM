package org.aion.avm.core.testExchange;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.core.testWallet.ByteArrayHelpers;

public class MemeController {

    private static IAionToken coinbase;

    public static void init(){
        coinbase = new MemeCoin(BlockchainRuntime.getSender());
    }

    public static byte[] main(){
        byte[] result = new byte[0];
        byte[] input = BlockchainRuntime.getData();
        AionTokenAbi.Decoder decoder = AionTokenAbi.buildDecoder(input);
        byte methodByte = decoder.decodeByte();

        switch (methodByte) {
            case AionTokenAbi.kICO_totalSupply:
                // We know that this is int (length), Address(*length), int, long.;
                result = ByteArrayHelpers.encodeLong(coinbase.totalSupply());
                break;
            case AionTokenAbi.kICO_balanceOf:
                result = ByteArrayHelpers.encodeLong(coinbase.balanceOf(decoder.decodeAddress()));
                break;
            case AionTokenAbi.kICO_allowance:
                result = ByteArrayHelpers.encodeLong(coinbase.allowance(decoder.decodeAddress(), decoder.decodeAddress()));
                break;
            case AionTokenAbi.kICO_transfer:
                result = ByteArrayHelpers.encodeBoolean(coinbase.transfer(decoder.decodeAddress(), decoder.decodeLong()));
                break;
            case AionTokenAbi.kICO_approve:
                result = ByteArrayHelpers.encodeBoolean(coinbase.approve(decoder.decodeAddress(), decoder.decodeLong()));
                break;
            case AionTokenAbi.kICO_transferFrom:
                result = ByteArrayHelpers.encodeBoolean(coinbase.transferFrom(decoder.decodeAddress(), decoder.decodeAddress(), decoder.decodeLong()));
                break;
            case AionTokenAbi.kICO_openAccount:
                result = ByteArrayHelpers.encodeBoolean(coinbase.openAccount(decoder.decodeAddress()));
                break;
            case AionTokenAbi.kICO_mint:
                result = ByteArrayHelpers.encodeBoolean(coinbase.mint(decoder.decodeAddress(), decoder.decodeLong()));
                break;
            default:
                break;
        }
        return result;
    }
}

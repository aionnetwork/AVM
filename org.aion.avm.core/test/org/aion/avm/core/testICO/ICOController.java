package org.aion.avm.core.testICO;

import org.aion.avm.api.BlockchainRuntime;
import org.aion.avm.core.testWallet.ByteArrayHelpers;

public class ICOController {

    private static IAionToken coinbase;

    public static void init(){
        coinbase = new PepeCoin(BlockchainRuntime.getSender());
    }

    public static byte[] main(){
        byte[] result = new byte[0];
        byte[] input = BlockchainRuntime.getData();
        ICOAbi.Decoder decoder = ICOAbi.buildDecoder(input);
        byte methodByte = decoder.decodeByte();
        boolean res = true;

        switch (methodByte) {
            case ICOAbi.kICO_totalSupply:
                // We know that this is int (length), Address(*length), int, long.;
                result = ByteArrayHelpers.encodeLong(coinbase.totalSupply());
                break;
            case ICOAbi.kICO_balanceOf:
                result = ByteArrayHelpers.encodeLong(coinbase.balanceOf(decoder.decodeAddress()));
                break;
            case ICOAbi.kICO_allowance:
                coinbase.allowance(decoder.decodeAddress(), decoder.decodeAddress());
                break;
            case ICOAbi.kICO_transfer:
                result = ByteArrayHelpers.encodeBoolean(coinbase.transfer(decoder.decodeAddress(), decoder.decodeLong()));
                break;
            case ICOAbi.kICO_approve:
                res = coinbase.approve(decoder.decodeAddress(), decoder.decodeLong());
                break;
            case ICOAbi.kICO_transferFrom:
                res = coinbase.transferFrom(decoder.decodeAddress(), decoder.decodeAddress(), decoder.decodeLong());
                break;
            case ICOAbi.kICO_openAccount:
                result = ByteArrayHelpers.encodeBoolean(coinbase.openAccount(decoder.decodeAddress()));
                break;
            case ICOAbi.kICO_mint:
                result = ByteArrayHelpers.encodeBoolean(coinbase.mint(decoder.decodeAddress(), decoder.decodeLong()));
                break;
            default:
                break;
        }
        return result;
    }
}

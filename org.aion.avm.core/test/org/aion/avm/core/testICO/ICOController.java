package org.aion.avm.core.testICO;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class ICOController {

    private static MemeCoin coinbase;

    public static void init(){
        coinbase = new MemeCoin(BlockchainRuntime.getSender());
    }

    public static byte[] main(){
        byte[] result = new byte[1];
        byte[] input = BlockchainRuntime.getData();
        ICOAbi.Decoder decoder = ICOAbi.buildDecoder(input);
        byte methodByte = decoder.decodeByte();
        boolean res = true;

        switch (methodByte) {
            case ICOAbi.kICO_totalSupply:
                // We know that this is int (length), Address(*length), int, long.
                coinbase.totalSupply();
                result[0] = 1;
                break;
            case ICOAbi.kICO_balanceOf:
                coinbase.balanceOf(decoder.decodeAddress());
                break;
            case ICOAbi.kICO_allowance:
                coinbase.allowance(decoder.decodeAddress(), decoder.decodeAddress());
                break;
            case ICOAbi.kICO_transfer:
                res = coinbase.transfer(decoder.decodeAddress(), decoder.decodeLong());
                break;
            case ICOAbi.kICO_approve:
                res = coinbase.approve(decoder.decodeAddress(), decoder.decodeLong());
                break;
            case ICOAbi.kICO_transferFrom:
                res = coinbase.transferFrom(decoder.decodeAddress(), decoder.decodeAddress(), decoder.decodeLong());
                break;
            default:
                break;
        }
        result[0] = res ? (byte)1 : (byte)0;

        return result;
    }
}

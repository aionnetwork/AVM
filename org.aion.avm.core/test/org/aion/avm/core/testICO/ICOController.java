package org.aion.avm.core.testICO;

import org.aion.avm.api.Address;
import org.aion.avm.api.BlockchainRuntime;

public class ICOController {

    private static PepeCoin coinbase;

    public static void init(Address minter){
        coinbase = new PepeCoin(minter);
    }

    public static byte[] main(){
        byte[] result = new byte[0];
        byte[] input = BlockchainRuntime.getData();
        ICOAbi.Decoder decoder = ICOAbi.buildDecoder(input);
        byte methodByte = decoder.decodeByte();

        switch (methodByte) {
            case ICOAbi.kICO_totalSupply: {
                // We know that this is int (length), Address(*length), int, long.
                //coinbase.totalSupply();
                break;
            }
        }

        return result;
    }
}

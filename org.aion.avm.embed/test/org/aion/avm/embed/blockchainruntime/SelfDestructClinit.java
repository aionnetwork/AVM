package org.aion.avm.embed.blockchainruntime;

import avm.Blockchain;
import org.aion.avm.userlib.abi.ABIDecoder;

import java.math.BigInteger;

public class SelfDestructClinit {

    static {
        ABIDecoder decoder = new ABIDecoder(Blockchain.getData());
        int type = decoder.decodeOneInteger();
        if (type == 0) {
            Blockchain.selfDestruct(Blockchain.getCaller());
         } else {
            Blockchain.call(decoder.decodeOneAddress(), BigInteger.ZERO, decoder.decodeOneByteArray(), Blockchain.getRemainingEnergy());
        }
    }

    public static byte[] main() {
        return new byte[0];
    }

}

package org.aion.avm.tooling.blockchainruntime;

import avm.Blockchain;

public class StorageEnergyClinitTarget {

    static {
        byte[] key = new byte[32];
        key[0] = 0x1;
        for(int i =0; i< 3; i++) {
            Blockchain.putStorage(key, new byte[0]);
            Blockchain.putStorage(key, null);
        }
    }

    public static byte[] main(){
        return new byte[0];
    }
}

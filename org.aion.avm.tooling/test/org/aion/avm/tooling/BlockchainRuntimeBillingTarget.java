package org.aion.avm.tooling;

import avm.Address;
import avm.Blockchain;
import org.aion.avm.tooling.abi.Callable;

public class BlockchainRuntimeBillingTarget {

    @Callable
    public static void fillArray(){
        int i = 0;
        Address[] addresses = new Address[9000];
        while(i<9000) {
            addresses[i] = Blockchain.getCaller();
            i++;
        }
    }
}

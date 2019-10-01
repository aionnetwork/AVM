package org.aion.avm.core;

import avm.Blockchain;

public class RevertInClinitTarget {

    static {
        Blockchain.revert();
    }

    public static byte[] main() {
        return null;
    }
}

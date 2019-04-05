package org.aion.avm.tooling.blockchainruntime;

import avm.Blockchain;

public class RevertAndInvalidTestResource {

    private static int state = 0;

    private static void testRevert() {
        state = 1;
        Blockchain.revert();
    }

    private static void testInvalid() {
        state = 2;
        Blockchain.invalid();
    }

    public static byte[] main() {
        if (Blockchain.getData()[0] == 1) {
            testRevert();
        } else if (Blockchain.getData()[0] == 2) {
            testInvalid();
        }

        return new byte[]{(byte) state};
    }
}

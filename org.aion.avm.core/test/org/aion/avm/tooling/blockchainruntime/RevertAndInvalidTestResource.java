package org.aion.avm.tooling.blockchainruntime;

import org.aion.avm.api.BlockchainRuntime;

public class RevertAndInvalidTestResource {

    private static int state = 0;

    private static void testRevert() {
        state = 1;
        BlockchainRuntime.revert();
    }

    private static void testInvalid() {
        state = 2;
        BlockchainRuntime.invalid();
    }

    public static byte[] main() {
        if (BlockchainRuntime.getData()[0] == 1) {
            testRevert();
        } else if (BlockchainRuntime.getData()[0] == 2) {
            testInvalid();
        }

        return new byte[]{(byte) state};
    }
}

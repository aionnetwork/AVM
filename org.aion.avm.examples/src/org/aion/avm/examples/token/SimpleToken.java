package org.aion.avm.examples.token;

import org.aion.avm.base.Contract;
import org.aion.avm.rt.BlockchainRuntime;

public class SimpleToken extends Contract {

    public void transfer(BlockchainRuntime rt, byte[] from, byte[] to) {
        // dummy balance check
        rt.getStorage().get(from);

        // dummy balance update
        rt.getStorage().put(from, new byte[]{4});
        rt.getStorage().put(to, new byte[]{2});
    }

    private byte[] copyOf(byte[] src, int from, int to) {
        byte[] dst = new byte[to - from];
        for (int i = from; i < to; i++) {
            dst[i] = src[i - from];
        }
        return dst;
    }

    @Override
    public byte[] run(byte[] input, BlockchainRuntime rt) {
        // dummy encoding: method id + abi(input parameters)
        if (input.length == 33 && input[0] == 1) {
            transfer(rt, rt.getSender(), copyOf(input, 1, 33));
        }

        return null;
    }
}

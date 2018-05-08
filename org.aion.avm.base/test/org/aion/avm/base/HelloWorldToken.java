package org.aion.avm.base;

import org.aion.avm.rt.BlockchainRuntime;

public class HelloWorldToken extends Contract {

    public void transfer(BlockchainRuntime context, byte[] from, byte[] to) {
        // dummy balance check
        context.getStorage().get(from);

        // dummy balance update
        context.getStorage().put(from, new byte[]{4});
        context.getStorage().put(to, new byte[]{2});
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

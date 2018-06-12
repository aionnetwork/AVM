package com.example.simpletoken;

import org.aion.avm.rt.BlockchainRuntime;

public class SimpleToken {

    public void transfer(BlockchainRuntime rt, byte[] from, byte[] to) {
        // dummy balance check
        rt.avm_getStorage(from);

        // dummy balance update
        rt.avm_putStorage(from, new byte[]{4});
        rt.avm_putStorage(to, new byte[]{2});
    }

    private byte[] copyOf(byte[] src, int from, int to) {
        byte[] dst = new byte[to - from];
        for (int i = from; i < to; i++) {
            dst[i] = src[i - from];
        }
        return dst;
    }

    public byte[] run(byte[] input, BlockchainRuntime rt) {
        // dummy encoding: method id + abi(input parameters)
        if (input.length == 33 && input[0] == 1) {
            transfer(rt, rt.avm_getSender().unwrap(), copyOf(input, 1, 33));
        }

        return null;
    }
}

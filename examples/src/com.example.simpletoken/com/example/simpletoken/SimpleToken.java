package com.example.simpletoken;

import org.aion.avm.api.BlockchainRuntime;

public class SimpleToken {

    public void transfer(byte[] from, byte[] to) {
        // dummy balance check
        BlockchainRuntime.getStorage(from);

        // dummy balance update
        BlockchainRuntime.putStorage(from, new byte[]{4});
        BlockchainRuntime.putStorage(to, new byte[]{2});
    }

    private byte[] copyOf(byte[] src, int from, int to) {
        byte[] dst = new byte[to - from];
        for (int i = from; i < to; i++) {
            dst[i] = src[i - from];
        }
        return dst;
    }

    public byte[] run(byte[] input) {
        // dummy encoding: method id + abi(input parameters)
        if (input.length == 33 && input[0] == 1) {
            transfer(BlockchainRuntime.getCaller().unwrap(), copyOf(input, 1, 33));
        }

        return null;
    }
}

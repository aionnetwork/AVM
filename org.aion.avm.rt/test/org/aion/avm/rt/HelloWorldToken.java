package org.aion.avm.rt;

import org.aion.avm.rt.Context;
import org.aion.avm.rt.Contract;

public class HelloWorldToken extends Contract {

    public void transfer(Context context, byte[] from, byte[] to) {
        // dummy balance check
        context.getStorage(from);

        // dummy balance update
        context.putStorage(from, new byte[]{4});
        context.putStorage(to, new byte[]{2});
    }

    private byte[] copyOf(byte[] src, int from, int to) {
        byte[] dst = new byte[to - from];
        for (int i = from; i < to; i++) {
            dst[i] = src[i - from];
        }
        return dst;
    }

    @Override
    public byte[] run(byte[] input, Context context) {
        // dummy encoding: method id + abi(input parameters)
        if (input.length == 33 && input[0] == 1) {
            transfer(context, context.getSender(), copyOf(input, 1, 33));
        }

        return null;
    }
}

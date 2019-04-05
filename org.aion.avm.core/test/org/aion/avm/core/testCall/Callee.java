package org.aion.avm.core.testCall;

import avm.BlockchainRuntime;

public class Callee {

    private static byte[] merge(byte[] a, byte[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("The byte array to merge can't be null!");
        }

        byte[] ret = new byte[a.length + b.length];
        System.arraycopy(a, 0, ret, 0, a.length);
        System.arraycopy(b, 0, ret, a.length, b.length);

        return ret;
    }

    public static byte[] main() {
        byte[] data = BlockchainRuntime.getData();
        return merge(data, "world".getBytes());
    }
}

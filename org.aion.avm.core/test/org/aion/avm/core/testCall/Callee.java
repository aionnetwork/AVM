package org.aion.avm.core.testCall;

import org.aion.avm.api.BlockchainRuntime;

public class Callee {

    private static byte[] merge(byte[] a, byte[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("The byte array to merge can't be null!");
        }

        byte[] ret = new byte[a.length + b.length];
        for (int i = 0; i < a.length; i++) {
            ret[i] = a[i];
        }
        for (int i = 0; i < b.length; i++) {
            ret[i + a.length] = b[i];
        }

        return ret;
    }

    public static byte[] main() {
        byte[] data = BlockchainRuntime.getData();
        return merge(data, "world".getBytes());
    }
}

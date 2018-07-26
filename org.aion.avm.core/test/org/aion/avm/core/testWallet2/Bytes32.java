package org.aion.avm.core.testWallet2;

import java.util.Arrays;

public class Bytes32 {
    private final byte[] data;

    public Bytes32(byte[] data) {
        if (data == null || data.length != 32) {
            throw new IllegalArgumentException("The length of data does not equal to 32");
        }
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Bytes32 && Arrays.equals(data, ((Bytes32) obj).getData());
    }
}

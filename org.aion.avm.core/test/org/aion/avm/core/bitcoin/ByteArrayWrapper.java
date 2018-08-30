package org.aion.avm.core.bitcoin;

import java.util.Arrays;

public class ByteArrayWrapper {
    private byte[] bytes;

    public ByteArrayWrapper(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] unwrap() {
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ByteArrayWrapper that = (ByteArrayWrapper) o;
        return Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}

package org.aion.avm.core.testWallet2;


// TODO: use Arrays utility class

public class ByteArrayWrapper {
    private final byte[] data;
    private final int hash;

    public ByteArrayWrapper(byte[] data) {
        // We just build a basic hash by summing the bytes, for now.
        int hash = data.length;
        for (int i = 0; i < data.length; ++i) {
            hash += data[i];
        }
        // We also want to create a copy of the array to provide immutable semantics.
        byte[] copy = data.clone();

        this.data = copy;
        this.hash = hash;
    }

    public byte[] getData() {
        return this.data;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = (this == obj);
        // Note that we want to make sure we are both the same sub-class, since equality is defined per-class.
        if (!isEqual
                && (null != obj)
                && (this.getClass() == obj.getClass())
                ) {
            ByteArrayWrapper other = (ByteArrayWrapper) obj;
            isEqual = (this.hash == other.hash)
                    && (this.data.length == other.data.length);
            if (isEqual) {
                for (int i = 0; i < this.data.length; ++i) {
                    if (this.data[i] != other.data[i]) {
                        isEqual = false;
                        break;
                    }
                }
            }
        }
        return isEqual;
    }
}

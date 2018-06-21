package org.aion.avm.api;

/**
 * Represents an address of account in Aion Network.
 */
public class Address {

    public static final int LENGTH = 32;

    private final byte[] raw;

    public Address(byte[] raw) {
        if (raw == null || raw.length != LENGTH) {
            throw new IllegalArgumentException();
        }
        this.raw = raw;
    }

    public byte[] unwrap() {
        return this.raw;
    }

    @Override
    public int hashCode() {
        // Just a really basic implementation.
        int code = 0;
        for (byte elt : this.raw) {
            code += (int) elt;
        }
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = this == obj;
        if (!isEqual && (obj instanceof Address)) {
            Address other = (Address) obj;
            if (this.raw.length == other.raw.length) {
                isEqual = true;
                for (int i = 0; isEqual && (i < other.raw.length); ++i) {
                    isEqual = (this.raw[i] == other.raw[i]);
                }
            }
        }
        return isEqual;
    }
}

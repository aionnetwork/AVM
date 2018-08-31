package org.aion.avm.api;

/**
 * Represents an address of account in the Aion Network.
 */
public class Address {

    /**
     * The length of an address.
     */
    public static final int LENGTH = 32;

    private final byte[] raw;

    /**
     * Create an Address instance from byte array.
     *
     * @param raw a byte array
     * @throws IllegalArgumentException when the input byte array is null or the length is invalid.
     */
    public Address(byte[] raw) throws IllegalArgumentException {
        if (raw == null || raw.length != LENGTH) {
            throw new IllegalArgumentException();
        }
        this.raw = raw;
    }

    /**
     * Returns the underlying byte array.
     *
     * @return the wrapped byte array.
     */
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

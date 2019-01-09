package org.aion.kernel;

import java.util.Arrays;
import org.aion.avm.core.util.Helpers;
import org.aion.vm.api.interfaces.IBloomFilter;

public class BloomFilter implements IBloomFilter {
    private byte[] filter = new byte[SIZE];

    /**
     * Constructs a new empty {@code BloomFilter}. That is, a filter whose bytes are all zeroes.
     */
    public BloomFilter() {}

    /**
     * Constructs a {@code BloomFilter} whose filter is backed by the specified byte array.
     *
     * @param bytes The bytes backing the filter.
     */
    public BloomFilter(byte[] bytes) {
        if (bytes == null || bytes.length != SIZE) {
            throw new IllegalArgumentException("BloomFilter bytes must be exactly length " + SIZE);
        }
        this.filter = bytes;
    }

    public static BloomFilter create(byte[] toBloom) {
        // value range: [0, 2^12-1=4096]
        int position1 = (((toBloom[0] & 0xff) & 7) << 8) + ((toBloom[1]) & 0xff);
        int position2 = (((toBloom[2] & 0xff) & 7) << 8) + ((toBloom[3]) & 0xff);
        int position3 = (((toBloom[4] & 0xff) & 7) << 8) + ((toBloom[5]) & 0xff);

        // # bits: 8 * 256 = 2048
        byte[] data = new byte[256];
        BloomFilter bloom = new BloomFilter(data);

        setBit(data, position1);
        setBit(data, position2);
        setBit(data, position3);

        return bloom;
    }

    /**
     * Sets the bit at position pos in data.
     */
    private static void setBit(byte[] data, int pos) {
        if ((data.length * 8) - 1 < pos) {
            throw new Error("outside byte array limit, pos: " + pos);
        }

        int posByte = data.length - 1 - (pos) / 8;
        int posBit = (pos) % 8;
        byte setter = (byte) (1 << (posBit));
        byte toBeSet = data[posByte];
        byte result = (byte) (toBeSet | setter);

        data[posByte] = result;
    }

    /**
     * @return The underlying bytes of the filter.
     */
    @Override
    public byte[] getBloomFilterBytes() {
        return this.filter;
    }

    /**
     * Performs a logical or of each bit in otherFilter and this filter.
     *
     * @param otherFilter The other filter to or with.
     */
    @Override
    public void or(IBloomFilter otherFilter) {
        byte[] otherBytes = otherFilter.getBloomFilterBytes();
        for (int i = 0; i < this.filter.length; i++) {
            this.filter[i] |= otherBytes[i];
        }
    }

    /**
     * Performs a logical and of each bit in otherFilter and this filter.
     *
     * @param otherFilter The other filter to and with.
     */
    @Override
    public void and(IBloomFilter otherFilter) {
        byte[] otherBytes = otherFilter.getBloomFilterBytes();
        for (int i = 0; i < this.filter.length; i++) {
            this.filter[i] &= otherBytes[i];
        }
    }

    @Override
    public boolean matches(IBloomFilter otherFilter) {
        BloomFilter copyOfThis = copy();
        copyOfThis.or(otherFilter);
        return this.equals(copyOfThis);
    }

    @Override
    public boolean contains(IBloomFilter otherFilter) {
        BloomFilter copyOfThis = copy();
        copyOfThis.and(otherFilter);
        return otherFilter.equals(copyOfThis);
    }

    private BloomFilter copy() {
        return new BloomFilter(Arrays.copyOf(this.filter, SIZE));
    }

    @Override
    public String toString() {
        return Helpers.bytesToHexString(this.filter);
    }

    /**
     * Returns {@code true} if, and only if, the underlying bytes of this filter are equal to the
     * underlying files of other, where other must be of type {@link IBloomFilter}.
     *
     * @param other The other object whose equality is to be tested.
     * @return True if the two filters are equal.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IBloomFilter)) {
            return false;
        }
        IBloomFilter otherAsFilter = (IBloomFilter) other;
        return Arrays.equals(this.filter, otherAsFilter.getBloomFilterBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.filter);
    }

}

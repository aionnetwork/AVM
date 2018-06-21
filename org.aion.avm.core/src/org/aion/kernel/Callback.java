package org.aion.kernel;

/**
 * Interface for accessing kernel features.
 */
public interface Callback {
    /**
     * Writes the auxiliary database from AVM, maintained by kernel.
     *
     * @param key
     * @param value
     */
    void putTransformedCode(byte[] key, byte[] value);

    /**
     * Reads the auxiliary database from AVM, maintained by kernel.
     *
     * @param key
     */
    void getTransformedCode(byte[] key);

    /**
     *
     */
    void call(byte[] address, byte[] value, byte[] data, long energyLimit);
}

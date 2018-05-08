package org.aion.avm.rt;

public interface Storage {

    /**
     * Returns the corresponding value in the storage.
     *
     * @param key
     * @return
     */
    byte[] get(byte[] key);

    /**
     * Inserts/updates a key-value pair.
     *
     * @param key
     * @param value
     */
    void put(byte[] key, byte[] value);
}

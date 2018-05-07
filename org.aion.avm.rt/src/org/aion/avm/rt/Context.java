package org.aion.avm.rt;

public interface Context {

    /**
     * Returns the sender address.
     *
     * @return
     */
    byte[] getSender();

    /**
     * Returns the corresponding value in the storage.
     *
     * @param key
     * @return
     */
    byte[] getStorage(byte[] key);

    /**
     * Inserts/updates a key-value pair.
     *
     * @param key
     * @param value
     */
    void putStorage(byte[] key, byte[] value);
}

package org.aion.avm.core.persistence;


/**
 * An abstract interface over the storage back-end (since we want to allow that to be changed, later).
 * Note that a single instance of this interface is meant to represent the graph for a single DApp so there is no need for "address" arguments.
 */
public interface IObjectGraphStore {
    /**
     * @return The code for this DApp.
     */
    public byte[] getCode();

    /**
     * Loads the data for the given key from this DApp's storage.
     * 
     * @param key The key to load.
     * @return The value for the key, null if it isn't found.
     */
    public byte[] getStorage(byte[] key);

    /**
     * Stores the given value for the given key in this DApp's storage.
     * 
     * @param key The key to store.
     * @param value The value to store for this key.
     */
    public void putStorage(byte[] key, byte[] value);

    /**
     * Builds a node referencing a constant.
     * 
     * @param constantId The ID of the constant (must be negative).
     * @return The INode instance representing this constant reference in storage.
     */
    public INode buildConstantNode(long constantId);

    /**
     * Builds a node referencing a class.
     * 
     * @param className The name of the class.
     * @return The INode instance representing this class reference in storage.
     */
    public INode buildClassNode(String className);

    /**
     * Tells the implementation to write-back any internally-managed state to its persistent layer.
     */
    public void flushWrites();
}

package org.aion.avm.core.persistence;

import java.util.function.Function;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IPersistenceToken;


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
     * Used for late initialization of the implementation in cases where not all requirements can be built before it is loaded.
     * 
     * @param classLoader The loader to use for looking up classes when initializing objects.
     * @param logicalDeserializer The deserializer to give to newly-instantiated objects, for when they want to load.
     * @param tokenBuilder Used to create the deserialization token for newly-instantiated objects, for when they want to load.
     */
    public void setLateComponents(ClassLoader classLoader, IDeserializer logicalDeserializer, Function<IRegularNode, IPersistenceToken> tokenBuilder);

    /**
     * Builds a node referencing a regular object.
     * 
     * @param typeName The name of the object's type (class name).
     * @return The IRegularNode instance representing the instance in storage.
     */
    public IRegularNode buildNewRegularNode(String typeName);

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

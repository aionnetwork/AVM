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
     * @param identityHashCode The identity hash code of the target object.
     * @param typeName The name of the object's type (class name).
     * @return The IRegularNode instance representing the instance in storage.
     */
    public IRegularNode buildNewRegularNode(int identityHashCode, String typeName);

    /**
     * Builds a node referencing a constant.
     * 
     * @param constantHashCode The special identity hash code assigned to the constant (see NodeEnvironment).
     * @return The INode instance representing this constant reference in storage.
     */
    public INode buildConstantNode(int constantHashCode);

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

    /**
     * Reads the high-level meta-data from storage.
     * 
     * @return The high-level meta-data (null if there isn't any).
     */
    public byte[] getMetaData();

    /**
     * Updates the on-disk representation of high-level meta-data.
     * 
     * @param data The meta-data to store.
     */
    public void setNewMetaData(byte[] data);

    /**
     * Reads the data extent representing the graph root (class statics) from storage.
     * 
     * @return The graph root data (null if there isn't any).
     */
    public SerializedRepresentation getRoot();

    /**
     * Updates the on-disk representation of graph root (class statics) data.
     * 
     * @param root The graph root data to store.
     */
    public void setRoot(SerializedRepresentation root);

    /**
     * A proof-of-concept done under issue-246.
     * This either needs to be changed to the correct size/algorithm (currently just the Arrays hash for byte[]) and/or changed
     * into a different approach, entirely.  This is currently using a Merkle tree where each instance is a leaf but we may
     * want a more compact implementation (n instances are a leaf, etc) or a different approach (streaming delta hash, for
     * example).
     * 
     * @return The hash representing the state of the storage graph.
     */
    public byte[] simpleHashCode();

    /**
     * Requests that the data store perform a deterministic garbage collection of reachable storage.
     * This involves walking all reachable object, from the class statics root, renumbering (compacting) the objects as it progresses.
     * A simplified description of the required GC algorithm:
     * 1)  Set next available slot to 1, set next scan slot to 1, load root SerializedRepresentation
     * 2)  Walk extent references, in-order
     * 3)  If reference has not been copied?
     *    T)  Copy object to next available slot, increment pointer, add mapping to old->new slot map, update reference
     *    F)  Read old->new slot mapping, update reference
     * 4)  When done loop (2), save back updated extent
     * 5)  If next scan slot less than next available slot?
     *    T)  Read extent at next scan slot, increment pointer, GOTO (2)
     *    F)  DONE - GC has completed
     * 
     * TODO:  In order to support multi-version data stores, this will actually have to be called on something lower-level.
     * 
     * @return The number of instances freed by the GC.
     */
    public long gc();
}

package org.aion.avm.core.persistence;


/**
 * The representation of a normal/regular object reference, in the storage system.
 * This is as opposed to constants or classes, which are handled specially.
 * The implementations of this of this will be, by their nature, highly implementation-dependent, so this interface
 * exists to provide a portable type check to the higher-level components.
 */
public interface IRegularNode extends INode {
    /**
     * Loads the actual data describing the instance pointed to by this INode or null if it wasn't loaded from storage.
     * Calling saveRegularData() does NOT change the result of this method.
     * Note that this method may be called, multiple times, over the course of the receiver's life so an implementation
     * is expected to cache this if it suspects that reloading it has a non-trivial cost.
     * 
     * @return The original storage representation of the serialized target instance, null if it is a new instance.
     */
    SerializedRepresentation loadOriginalData();

    /**
     * Saves the actual data describing the instance pointed to by this INode.
     * Note that this should only be called once on a given instance.  Subsequent calls will assert fail.
     * 
     * @param extent The data containing the serialized target instance.
     */
    void saveRegularData(SerializedRepresentation extent);
}

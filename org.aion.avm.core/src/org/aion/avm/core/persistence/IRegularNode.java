package org.aion.avm.core.persistence;


/**
 * The representation of a normal/regular object reference, in the storage system.
 * This is as opposed to constants or classes, which are handled specially.
 * The implementations of this of this will be, by their nature, highly implementation-dependent, so this interface
 * exists to provide a portable type check to the higher-level components.
 */
public interface IRegularNode extends INode {
    /**
     * Loads the actual data describing the instance pointed to by this INode.
     * 
     * @return The extent containing the serialized target instance.
     */
    SerializedRepresentation loadRegularData();

    /**
     * Saves the actual data describing the instance pointed to by this INode.
     * 
     * @param extent The data containing the serialized target instance.
     */
    void saveRegularData(SerializedRepresentation extent);
}

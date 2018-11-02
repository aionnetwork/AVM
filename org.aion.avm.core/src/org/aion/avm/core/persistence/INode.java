package org.aion.avm.core.persistence;


/**
 * The INode instances MUST be uniqued by the underlying data store which created them.
 */
public interface INode {
    /**
     * @return The same object instance on each call.
     */
    public org.aion.avm.shadow.java.lang.Object getObjectInstance();

    /**
     * Required to build the high-level serialized form of an object (references are encoded as identity hash code).
     * 
     * @return The identity hash code of the target object.
     */
    public int getIdentityHashCode();
}

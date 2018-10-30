package org.aion.avm.core.persistence;


/**
 * The INode instances MUST be uniqued by the underlying data store which created them.
 */
public interface INode {
    /**
     * @return The same object instance on each call.
     */
    public org.aion.avm.shadow.java.lang.Object getObjectInstance();
}

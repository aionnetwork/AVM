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
     * TODO:  Make sure that the billable reference size is converted into a logic value, no longer directly dependent upon our implementation
     * details.  We currently allow that as a stop-gap to moving into the physical/logic storage split.
     */
    public int getBillableReferenceSize();
}

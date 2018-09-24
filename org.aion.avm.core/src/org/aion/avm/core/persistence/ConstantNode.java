package org.aion.avm.core.persistence;


/**
 * The data required to describe a constant reference, in the serialization layer.
 */
public class ConstantNode implements INode {
    public final long constantId;

    public ConstantNode(long constantId) {
        this.constantId = constantId;
    }
}

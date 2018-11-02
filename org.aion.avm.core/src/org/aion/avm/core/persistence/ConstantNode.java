package org.aion.avm.core.persistence;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.shadow.java.lang.Object;


/**
 * The data required to describe a constant reference, in the serialization layer.
 * Constants are identified by their specified identity hash codes.
 */
public class ConstantNode implements INode {
    public final int constantHashCode;

    public ConstantNode(int constantHashCode) {
        this.constantHashCode = constantHashCode;
    }

    @Override
    public Object getObjectInstance() {
        return NodeEnvironment.singleton.getConstantMap().get(this.constantHashCode);
    }

    @Override
    public int getIdentityHashCode() {
        // The hash code is a first class identifier of a constant so we can return it, directly.
        return this.constantHashCode;
    }
}

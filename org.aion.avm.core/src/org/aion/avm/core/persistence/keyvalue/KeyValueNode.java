package org.aion.avm.core.persistence.keyvalue;

import org.aion.avm.core.persistence.IRegularNode;


/**
 * The data required to describe a normal object reference, in the serialization layer.
 */
public class KeyValueNode implements IRegularNode {
    private final String instanceClassName;
    private final long instanceId;

    public KeyValueNode(String instanceClassName, long instanceId) {
        this.instanceClassName = instanceClassName;
        this.instanceId = instanceId;
    }

    public String getInstanceClassName() {
        return this.instanceClassName;
    }

    public long getInstanceId() {
        return this.instanceId;
    }
}

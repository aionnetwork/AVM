package org.aion.avm.core.persistence.keyvalue;

import org.aion.avm.core.persistence.SerializedRepresentation;
import org.aion.avm.core.persistence.IRegularNode;


/**
 * The data required to describe a normal object reference, in the serialization layer.
 */
public class KeyValueNode implements IRegularNode {
    private final KeyValueObjectGraph parentGraph;
    private final String instanceClassName;
    private final long instanceId;
    private org.aion.avm.shadow.java.lang.Object resolvedObject;

    // parentGraph is passed in since there is a great deal of context which lives there (these two classes are unavoidably tightly coupled).
    public KeyValueNode(KeyValueObjectGraph parentGraph, String instanceClassName, long instanceId) {
        this.parentGraph = parentGraph;
        this.instanceClassName = instanceClassName;
        this.instanceId = instanceId;
    }

    public String getInstanceClassName() {
        return this.instanceClassName;
    }

    public long getInstanceId() {
        return this.instanceId;
    }

    @Override
    public org.aion.avm.shadow.java.lang.Object getObjectInstance() {
        if (null == this.resolvedObject) {
            this.resolvedObject = this.parentGraph.createInstanceStubForNode(this.instanceClassName, this);
        }
        return this.resolvedObject;
    }

    @Override
    public SerializedRepresentation loadRegularData() {
        byte[] data = this.parentGraph.loadStorageForInstance(this.instanceId);
        return KeyValueCodec.decode(this.parentGraph, data);
    }

    @Override
    public void saveRegularData(SerializedRepresentation extent) {
        byte[] data = KeyValueCodec.encode(extent);
        this.parentGraph.storeDataForInstance(this.instanceId, data);
    }
}

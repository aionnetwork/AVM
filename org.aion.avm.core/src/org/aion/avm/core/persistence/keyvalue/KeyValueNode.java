package org.aion.avm.core.persistence.keyvalue;

import org.aion.avm.core.persistence.SerializedRepresentation;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.avm.core.persistence.IRegularNode;


/**
 * The data required to describe a normal object reference, in the serialization layer.
 */
public class KeyValueNode implements IRegularNode {
    private final KeyValueObjectGraph parentGraph;
    private final int identityHashCode;
    private final String instanceClassName;
    private final long instanceId;
    private final boolean isLoadedFromStorage;
    private org.aion.avm.shadow.java.lang.Object resolvedObject;
    // We store the original representation, in cases where we loaded from storage, for later diff comparison and delta hash computation.
    // (this is lazily discovered, on load)
    private SerializedRepresentation originalRepresentation;
    // We want to make sure that each instance is only saved once (since our delta hash depends on that) so we can only save an instance once.
    private boolean hasBeenSaved;

    // parentGraph is passed in since there is a great deal of context which lives there (these two classes are unavoidably tightly coupled).
    public KeyValueNode(KeyValueObjectGraph parentGraph, int identityHashCode, String instanceClassName, long instanceId, boolean isLoadedFromStorage) {
        this.parentGraph = parentGraph;
        this.identityHashCode = identityHashCode;
        this.instanceClassName = instanceClassName;
        this.instanceId = instanceId;
        this.isLoadedFromStorage = isLoadedFromStorage;
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
    public int getIdentityHashCode() {
        return this.identityHashCode;
    }

    @Override
    public SerializedRepresentation loadOriginalData() {
        if ((null == this.originalRepresentation) && this.isLoadedFromStorage) {
            byte[] data = this.parentGraph.loadStorageForInstance(this.instanceId);
            this.originalRepresentation = KeyValueCodec.decode(this.parentGraph, data);
        }
        return this.originalRepresentation;
    }

    @Override
    public void saveRegularData(SerializedRepresentation extent) {
        // Given that we now cache the load, a store here would be unreadable.
        RuntimeAssertionError.assertTrue(!this.hasBeenSaved);
        // Nobody should be able to save an update to an on-disk node without first loading it.
        if (this.isLoadedFromStorage) {
            RuntimeAssertionError.assertTrue(null != this.originalRepresentation);
        }
        byte[] data = KeyValueCodec.encode(extent);
        this.parentGraph.storeDataForInstance(this.instanceId, data);
        this.hasBeenSaved = true;
    }
}

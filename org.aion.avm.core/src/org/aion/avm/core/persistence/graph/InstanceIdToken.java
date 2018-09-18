package org.aion.avm.core.persistence.graph;

import org.aion.avm.internal.IPersistenceToken;


/**
 * Most likely just a temporary implementation of IPersistenceToken until we can get everything transitioned to the graph package's INode.
 */
public class InstanceIdToken implements IPersistenceToken {
    public final long instanceId;
    
    public InstanceIdToken(long instanceId) {
        this.instanceId = instanceId;
    }
    
    @Override
    public boolean isNormalInstance() {
        return this.instanceId > 0L;
    }
}

package org.aion.avm.core.persistence;

import org.aion.avm.internal.IPersistenceToken;


/**
 * The common case of a persistence token:  the kind used to for regular object instances.
 * Ultimately, this case depends on the implementation-dependent node type so that is all it needs to reference.
 */
public class NodePersistenceToken implements IPersistenceToken {
    public final IRegularNode node;
    // A token is "newly written" if this instance was created in order to serialize an object as opposed to referring to something already in storage.
    public final boolean isNewlyWritten;

    public NodePersistenceToken(IRegularNode node, boolean isNewlyWritten) {
        this.node = node;
        this.isNewlyWritten = isNewlyWritten;
    }
}

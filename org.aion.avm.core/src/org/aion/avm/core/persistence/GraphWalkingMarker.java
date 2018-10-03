package org.aion.avm.core.persistence;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * This is an implementation of IDeserializer only to satisfy the type of the slot we want to use.
 * This type should be used in cases where an identifiable "mark" needs to be applied to objects
 * in the graph which don't actually have IDeserializer instances (that is, objects which are
 * loaded).
 */
public class GraphWalkingMarker implements IDeserializer {
    @Override
    public void startDeserializeInstance(org.aion.avm.shadow.java.lang.Object instance, IPersistenceToken persistenceToken) {
        throw RuntimeAssertionError.unreachable("This marker is not able to deserialize");
    }
}

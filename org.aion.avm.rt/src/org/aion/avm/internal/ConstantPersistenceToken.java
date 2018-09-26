package org.aion.avm.internal;


/**
 * Constants are special, as represented with the persistent graph, since they are long-lived, effectively shared, resources in memory.
 */
public class ConstantPersistenceToken implements IPersistenceToken {
    public final long stableConstantId;

    public ConstantPersistenceToken(long stableConstantId) {
        this.stableConstantId = stableConstantId;
    }
}

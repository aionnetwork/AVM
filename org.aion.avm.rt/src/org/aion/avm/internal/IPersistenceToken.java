package org.aion.avm.internal;


/**
 * Used as the opaque token to associated an instance with its location in storage or in a parent heap (in the reentrant case).
 */
public interface IPersistenceToken {
    /**
     * Potentially only a short-term requirement, this is used to determine if this is a normal instance (that is, not a constant or class).
     * 
     * @return True if this is a normal instance, false if it is a constant or a class.
     */
    boolean isNormalInstance();
}

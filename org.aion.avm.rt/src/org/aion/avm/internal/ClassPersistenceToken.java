package org.aion.avm.internal;


/**
 * Classes are special, as represented with the persistent graph, since they are long-lived, effectively shared, resources in memory.
 */
public class ClassPersistenceToken implements IPersistenceToken {
    public final String className;

    public ClassPersistenceToken(String className) {
        this.className = className;
    }
}

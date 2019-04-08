package org.aion.avm.core.persistence;


public class StandardNameMapper implements IPersistenceNameMapper {
    // TODO:  THIS NEEDS TO MAP INTO CANONICAL TYPES!
    @Override
    public String getStorageClassName(String ourName) {
        // TODO: Build a real mapping.
        return ourName;
    }

    @Override
    public String getInternalClassName(String storageClassName) {
        // TODO: Build a real mapping.
        return storageClassName;
    }
}

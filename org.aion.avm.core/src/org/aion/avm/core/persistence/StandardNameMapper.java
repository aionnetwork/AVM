package org.aion.avm.core.persistence;


public class StandardNameMapper implements IPersistenceNameMapper {
    // TODO (AKI-95):  THIS NEEDS TO MAP INTO CANONICAL TYPES!
    @Override
    public String getStorageClassName(String ourName) {
        // TODO (AKI-95): Build a real mapping.
        return ourName;
    }

    @Override
    public String getInternalClassName(String storageClassName) {
        // TODO (AKI-95): Build a real mapping.
        return storageClassName;
    }
}

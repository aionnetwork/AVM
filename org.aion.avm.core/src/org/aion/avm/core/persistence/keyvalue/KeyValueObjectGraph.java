package org.aion.avm.core.persistence.keyvalue;

import org.aion.avm.core.persistence.IObjectGraphStore;
import org.aion.kernel.KernelInterface;


/**
 * The implementation of IObjectGraphStore built directly on top of the KernelInterface (using its key-value store).
 */
public class KeyValueObjectGraph implements IObjectGraphStore {
    private final KernelInterface store;
    private final byte[] address;

    public KeyValueObjectGraph(KernelInterface store, byte[] address) {
        this.store = store;
        this.address = address;
    }

    @Override
    public byte[] getCode() {
        return this.store.getCode(this.address);
    }

    @Override
    public byte[] getStorage(byte[] key) {
        return this.store.getStorage(this.address, key);
    }

    @Override
    public void putStorage(byte[] key, byte[] value) {
        this.store.putStorage(this.address, key, value);
    }
}

package org.aion.avm.core.persistence.keyvalue;

import java.util.Arrays;

import org.aion.avm.core.persistence.ClassNode;
import org.aion.avm.core.persistence.ConstantNode;
import org.aion.avm.core.persistence.INode;
import org.aion.avm.core.persistence.IObjectGraphStore;
import org.aion.avm.core.persistence.IRegularNode;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.KernelInterface;


/**
 * The implementation of IObjectGraphStore built directly on top of the KernelInterface (using its key-value store).
 */
public class KeyValueObjectGraph implements IObjectGraphStore {
    // Used to fix the billing size calculation:  in the future, we will derive this from the logical content of the Extent but, for now, callers
    // just need a way to remove the overhead introduced by this codec (2 "size" ints).
    public static final int OVERHEAD_BYTES = 8;

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

    public IRegularNode buildExistingRegularNode(String typeName, long instanceId) {
        return new KeyValueNode(typeName, instanceId);
    }

    @Override
    public INode buildConstantNode(long constantId) {
        // This is just to point out that constantIds are always negative.
        RuntimeAssertionError.assertTrue(constantId < 0);
        return new ConstantNode(constantId);
    }

    @Override
    public INode buildClassNode(String className) {
        return new ClassNode(className);
    }

    @Override
    public String toString() {
        return "KeyValueObjectGraph @" + Arrays.toString(this.address);
    }
}

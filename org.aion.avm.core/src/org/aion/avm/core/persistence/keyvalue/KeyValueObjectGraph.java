package org.aion.avm.core.persistence.keyvalue;

import java.util.Arrays;

import org.aion.avm.core.persistence.ClassNode;
import org.aion.avm.core.persistence.ConstantNode;
import org.aion.avm.core.persistence.INode;
import org.aion.avm.core.persistence.IObjectGraphStore;
import org.aion.avm.core.persistence.IRegularNode;
import org.aion.avm.core.persistence.StreamingPrimitiveCodec;
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
    private long nextInstanceId;

    public KeyValueObjectGraph(KernelInterface store, byte[] address) {
        this.store = store;
        this.address = address;
        
        // We will scoop the nextInstanceId out of our hidden key.
        byte[] rawData = this.store.getStorage(this.address, StorageKeys.INTERNAL_DATA);
        if (null != rawData) {
            RuntimeAssertionError.assertTrue(rawData.length == Long.BYTES);
            StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(rawData);
            this.nextInstanceId = decoder.decodeLong();
        } else {
            // This must be new.
            this.nextInstanceId = 1L;
        }
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
    public void flushWrites() {
        StreamingPrimitiveCodec.Encoder encoder = new StreamingPrimitiveCodec.Encoder();
        encoder.encodeLong(this.nextInstanceId);
        byte[] rawData = encoder.toBytes();
        this.store.putStorage(this.address, StorageKeys.INTERNAL_DATA, rawData);
    }

    @Override
    public String toString() {
        return "KeyValueObjectGraph @" + Arrays.toString(this.address);
    }

    // TODO:  Change the nextInstanceId to purely internal once we manage node allocation here.
    public long getNextInstanceId() {
        return this.nextInstanceId;
    }
    public void setNextInstanceId(long nextInstanceId) {
        this.nextInstanceId = nextInstanceId;
    }
}

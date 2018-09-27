package org.aion.avm.core.persistence.keyvalue;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.core.persistence.ClassNode;
import org.aion.avm.core.persistence.ConstantNode;
import org.aion.avm.core.persistence.ConstructorCache;
import org.aion.avm.core.persistence.Extent;
import org.aion.avm.core.persistence.INode;
import org.aion.avm.core.persistence.IObjectGraphStore;
import org.aion.avm.core.persistence.IRegularNode;
import org.aion.avm.core.persistence.StreamingPrimitiveCodec;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.KernelInterface;


/**
 * The implementation of IObjectGraphStore built directly on top of the KernelInterface (using its key-value store).
 */
public class KeyValueObjectGraph implements IObjectGraphStore {
    private final Map<Long, KeyValueNode> idToNodeMap;
    private final KernelInterface store;
    private final byte[] address;
    private long nextInstanceId;

    private ConstructorCache constructorCache;
    private IDeserializer logicalDeserializer;
    private Function<IRegularNode, IPersistenceToken> tokenBuilder;

    public KeyValueObjectGraph(KernelInterface store, byte[] address) {
        this.idToNodeMap = new HashMap<>();
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

    public void setLateComponents(ClassLoader classLoader, IDeserializer logicalDeserializer, Function<IRegularNode, IPersistenceToken> tokenBuilder) {
        this.constructorCache = new ConstructorCache(classLoader);
        this.logicalDeserializer = logicalDeserializer;
        this.tokenBuilder = tokenBuilder;
    }

    @Override
    public byte[] getMetaData() {
        return this.store.getStorage(this.address, StorageKeys.CONTRACT_ENVIRONMENT);
    }

    @Override
    public void setNewMetaData(byte[] data) {
        this.store.putStorage(this.address, StorageKeys.CONTRACT_ENVIRONMENT, data);
    }

    @Override
    public Extent getRoot() {
        // Wipe any stale node state since we are reading the root, again.
        this.idToNodeMap.clear();
        
        byte[] rootBytes = this.store.getStorage(this.address, StorageKeys.CLASS_STATICS);
        RuntimeAssertionError.assertTrue(null != rootBytes);
        return KeyValueExtentCodec.decode(this, rootBytes);
    }

    public void setRoot(Extent root) {
        byte[] rootBytes = KeyValueExtentCodec.encode(root);
        RuntimeAssertionError.assertTrue(null != rootBytes);
        this.store.putStorage(this.address, StorageKeys.CLASS_STATICS, rootBytes);
    }

    @Override
    public IRegularNode buildNewRegularNode(String typeName) {
        // Consume the next ID (make sure it doesn't overflow our limit).
        long instanceId = this.nextInstanceId;
        this.nextInstanceId += 1;
        
        // Create the new node and add it to the map.
        KeyValueNode node = new KeyValueNode(this, typeName, instanceId);
        this.idToNodeMap.put(instanceId, node);
        return node;
    }

    public IRegularNode buildExistingRegularNode(String typeName, long instanceId) {
        KeyValueNode node = this.idToNodeMap.get(instanceId);
        if (null == node) {
            node = new KeyValueNode(this, typeName, instanceId);
            this.idToNodeMap.put(instanceId, node);
        }
        return node;
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

    /**
     * Called by KeyValueNode to create the instance it references (which it, internally, caches for any future requests).
     * 
     * @param instanceClassName
     * @param instanceId
     * @return
     */
    public org.aion.avm.shadow.java.lang.Object createInstanceStubForNode(String instanceClassName, IRegularNode callingNode) {
        IPersistenceToken token = this.tokenBuilder.apply(callingNode);
        try {
            return (org.aion.avm.shadow.java.lang.Object)
                    this.constructorCache.getConstructorForClassName(instanceClassName)
                        .newInstance(this.logicalDeserializer, token);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // Any errors like this would have been caught earlier.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public byte[] loadStorageForInstance(long instanceId) {
        return this.store.getStorage(this.address, StorageKeys.forInstance(instanceId));
    }

    public void storeDataForInstance(long instanceId, byte[] data) {
        this.store.putStorage(this.address, StorageKeys.forInstance(instanceId), data);
    }
}

package org.aion.avm.core.persistence.keyvalue;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.core.IExternalCapabilities;
import org.aion.avm.core.persistence.ClassNode;
import org.aion.avm.core.persistence.ConstantNode;
import org.aion.avm.core.persistence.ConstructorCache;
import org.aion.avm.core.persistence.SerializedRepresentation;
import org.aion.avm.core.persistence.INode;
import org.aion.avm.core.persistence.IObjectGraphStore;
import org.aion.avm.core.persistence.IRegularNode;
import org.aion.avm.core.persistence.StreamingPrimitiveCodec;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.types.Address;
import org.aion.vm.api.interfaces.KernelInterface;


/**
 * The implementation of IObjectGraphStore built directly on top of the KernelInterface (using its key-value store).
 */
public class KeyValueObjectGraph implements IObjectGraphStore {
    private static final long HIGH_RANGE_BIAS = 1_000_000_000L;

    private final IExternalCapabilities capabilities;
    private final Map<Long, KeyValueNode> idToNodeMap;
    private final KernelInterface store;
    private final Address address;
    private long nextInstanceId;
    // Tells us which half of the semi-space we are in:  0L or HIGH_RANGE_BIAS.
    // Note that this instanceId is used for segmented addressing, but is not stored in the stored data.
    private long instanceIdBias;
    private byte[] deltaHash;
    // We store the initial root we read for the delta hash computation.
    private SerializedRepresentation initialRootRepresentation;
    private ConstructorCache constructorCache;
    private IDeserializer logicalDeserializer;
    private Function<IRegularNode, IPersistenceToken> tokenBuilder;

    public KeyValueObjectGraph(IExternalCapabilities capabilities, KernelInterface store, Address address) {
        this.capabilities = capabilities;
        this.idToNodeMap = new HashMap<>();
        this.store = store;
        this.address = address;
        this.deltaHash = new byte[32];

        // We will scoop the nextInstanceId out of our hidden key.
        byte[] rawData = this.store.getStorage(this.address, StorageKeys.INTERNAL_DATA);
        if (null != rawData) {
            StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(rawData);
            this.nextInstanceId = decoder.decodeLong();
            this.instanceIdBias = decoder.decodeLong();
            decoder.decodeBytesInto(this.deltaHash);
        } else {
            // This must be new.
            this.nextInstanceId = 1L;
            this.instanceIdBias = 0L;
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
    public SerializedRepresentation getRoot() {
        // Wipe any stale node state since we are reading the root, again.
        this.idToNodeMap.clear();
        
        this.initialRootRepresentation = loadRootOnly();
        return this.initialRootRepresentation;
    }

    @Override
    public void setRoot(SerializedRepresentation root) {
        byte[] rootBytes = KeyValueCodec.encode(root);
        RuntimeAssertionError.assertTrue(null != rootBytes);
        this.store.putStorage(this.address, StorageKeys.CLASS_STATICS, rootBytes);


        if (null != this.initialRootRepresentation) {
            byte[] initialRootHash = getConsensusHashForRepresentation(this.initialRootRepresentation);
            for (int i = 0; i < this.deltaHash.length; ++i) {
                this.deltaHash[i] ^= initialRootHash[i];
            }
        }
        byte[] rootHash = getConsensusHashForRepresentation(root);
        for (int i = 0; i < this.deltaHash.length; ++i) {
            this.deltaHash[i] ^= rootHash[i];
        }
    }

    @Override
    public IRegularNode buildNewRegularNode(int identityHashCode, String typeName) {
        // Consume the next ID (make sure it doesn't overflow our limit).
        long instanceId = this.nextInstanceId;
        this.nextInstanceId += 1;
        RuntimeAssertionError.assertTrue(this.nextInstanceId < (this.instanceIdBias + HIGH_RANGE_BIAS));
        
        // Create the new node and add it to the map.
        boolean isLoadedFromStorage = false;
        KeyValueNode node = new KeyValueNode(this, identityHashCode, typeName, instanceId, isLoadedFromStorage);
        this.idToNodeMap.put(instanceId, node);
        return node;
    }

    public IRegularNode buildExistingRegularNode(int identityHashCode, String typeName, long instanceId) {
        KeyValueNode node = this.idToNodeMap.get(instanceId);
        if (null == node) {
            boolean isLoadedFromStorage = true;
            node = new KeyValueNode(this, identityHashCode, typeName, instanceId, isLoadedFromStorage);
            this.idToNodeMap.put(instanceId, node);
        }
        return node;
    }

    @Override
    public INode buildConstantNode(int constantHashCode) {
        // We expect this to be a small, positive integer.
        RuntimeAssertionError.assertTrue(constantHashCode > 0);
        // (update this number if we add more constants - just made to catch simple errors).
        RuntimeAssertionError.assertTrue(constantHashCode < 100);
        return new ConstantNode(constantHashCode);
    }

    @Override
    public INode buildClassNode(String className) {
        return new ClassNode(className);
    }

    @Override
    public void flushWrites() {
        StreamingPrimitiveCodec.Encoder encoder = new StreamingPrimitiveCodec.Encoder();
        encoder.encodeLong(this.nextInstanceId);
        encoder.encodeLong(this.instanceIdBias);
        encoder.encodeBytes(this.deltaHash);
        byte[] rawData = encoder.toBytes();
        this.store.putStorage(this.address, StorageKeys.INTERNAL_DATA, rawData);
    }

    @Override
    public byte[] simpleHashCode() {
        return this.deltaHash;
    }


    @Override
    public long gc() {
        // As a relatively simple proof-of-concept, we will build a copying semi-space collector (scavenger) which uses instanceIdBias
        // to define the "low" and "high" spaces (if we assume less than HIGH_RANGE_BIAS instances are ever in storage at once.
        
        // First, determine the target bias and start our new instanceId counter (since we are re-assigning these).
        long targetBias = (0L == this.instanceIdBias) ? HIGH_RANGE_BIAS : 0L;
        long nextInstanceId = 1L;
        
        // Create the old-new instanceId fixup map.
        Map<Long, Long> instanceIdFixups = new HashMap<>();
        
        // Begin the GC:  set the next scan pointer to 0, read the statics extent (the implicit 0), and start the GC.
        // NOTE:  The statics don't move, so we update them in place (just a single SerializedRepresentation).
        long nextScanInstanceId = 0L;
        byte[] currentKey = StorageKeys.CLASS_STATICS;

        // Reset the delta hash since we will recompute the entire thing with reachable data, only.
        this.deltaHash = new byte[32];
        this.idToNodeMap.clear();
        SerializedRepresentation scanningRepresentation = loadRootOnly();
        while (null != scanningRepresentation) {
            byte[] rootHash = getConsensusHashForRepresentation(scanningRepresentation);
            for (int i = 0; i < this.deltaHash.length; ++i) {
                this.deltaHash[i] ^= rootHash[i];
            }
            boolean didWrite = false;
            INode[] refs = scanningRepresentation.references;
            for (int i = 0; i < refs.length; ++i) {
                INode ref = refs[i];
                // Class and constant refs don't change.
                if (ref instanceof KeyValueNode) {
                    KeyValueNode node = (KeyValueNode)ref;
                    long oldTargetInstanceId = node.getInstanceId();
                    long newTargetInstanceId = 0L;
                    if (!instanceIdFixups.containsKey(oldTargetInstanceId)) {
                        // Allocate this instance and copy the object.
                        newTargetInstanceId = nextInstanceId;
                        nextInstanceId += 1;
                        instanceIdFixups.put(oldTargetInstanceId, newTargetInstanceId);
                        byte[] rawData = this.store.getStorage(this.address, StorageKeys.forInstance(oldTargetInstanceId + this.instanceIdBias));
                        RuntimeAssertionError.assertTrue(null != rawData);
                        RuntimeAssertionError.assertTrue(rawData.length > 0);
                        this.store.putStorage(this.address, StorageKeys.forInstance(newTargetInstanceId + targetBias), rawData);
                    } else {
                        newTargetInstanceId = instanceIdFixups.get(oldTargetInstanceId);
                    }
                    // Fixup the reference.
                    if (oldTargetInstanceId != newTargetInstanceId) {
                        // All nodes seen by the GC are from storage.
                        boolean isLoadedFromStorage = true;
                        refs[i] = new KeyValueNode(this, node.getIdentityHashCode(), node.getInstanceClassName(), newTargetInstanceId, isLoadedFromStorage);
                        didWrite = true;
                    }
                }
            }
            if (didWrite) {
                this.store.putStorage(this.address, currentKey, KeyValueCodec.encode(scanningRepresentation));
            }
            nextScanInstanceId += 1;
            if (nextScanInstanceId < nextInstanceId) {
                // Load the next SerializedRepresentation and continue the collection.
                currentKey = StorageKeys.forInstance(nextScanInstanceId + targetBias);
                byte[] nextNewInstanceToScan = this.store.getStorage(this.address, currentKey);
                scanningRepresentation = KeyValueCodec.decode(this, nextNewInstanceToScan);
            } else {
                // We are done so fall out.
                currentKey = null;
                scanningRepresentation = null;
            }
        }
        
        // Save the internal state
        for (long i = 1L; i < this.nextInstanceId; ++i) {
            this.store.putStorage(this.address, StorageKeys.forInstance(i + this.instanceIdBias), new byte[0]);
        }
        // The number of instances freed is just the difference between our next ID incrementor before and after the GC.
        long instancesFreed = (this.nextInstanceId - nextInstanceId);
        this.nextInstanceId = nextInstanceId;
        this.instanceIdBias = targetBias;
        flushWrites();
        return instancesFreed;
    }

    @Override
    public String toString() {
        return "KeyValueObjectGraph @" + this.address;
    }

    /**
     * Called by KeyValueNode to create the instance it references (which it, internally, caches for any future requests).
     * 
     * @param instanceClassName
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
        return this.store.getStorage(this.address, StorageKeys.forInstance(instanceId + this.instanceIdBias));
    }

    public void storeDataForInstance(long instanceId, SerializedRepresentation original, SerializedRepresentation updated) {
        byte[] data = KeyValueCodec.encode(updated);
        this.store.putStorage(this.address, StorageKeys.forInstance(instanceId + this.instanceIdBias), data);

        if (null != original) {
            byte[] originalHash = getConsensusHashForRepresentation(original);
            for (int i = 0; i < this.deltaHash.length; ++i) {
                this.deltaHash[i] ^= originalHash[i];
            }
        }
        byte[] updatedHash = getConsensusHashForRepresentation(updated);
        for (int i = 0; i < this.deltaHash.length; ++i) {
            this.deltaHash[i] ^= updatedHash[i];
        }
    }

    private byte[] getConsensusHashForRepresentation(SerializedRepresentation representation) {
        // Serialize this to a byte array.
        byte[] dataToHash = new byte[representation.references.length * Integer.BYTES + representation.data.length];
        int index = 0;
        for (INode ref : representation.references) {
            int hash = (null != ref)
                    ? ref.getIdentityHashCode()
                    : 0;
            writeIntToBuffer(dataToHash, index, hash);
            index += Integer.BYTES;
        }
        System.arraycopy(representation.data, 0, dataToHash, index, representation.data.length);

        return this.capabilities.keccak256(dataToHash);
    }

    private SerializedRepresentation loadRootOnly() {
        byte[] rootBytes = this.store.getStorage(this.address, StorageKeys.CLASS_STATICS);
        RuntimeAssertionError.assertTrue(null != rootBytes);
        return KeyValueCodec.decode(this, rootBytes);
    }

    private void writeIntToBuffer(byte[] buffer, int offset, int toEncode) {
        buffer[offset] = (byte)(0xff & (toEncode >> 24));
        buffer[offset + 1] = (byte)(0xff & (toEncode >> 16));
        buffer[offset + 2] = (byte)(0xff & (toEncode >> 8));
        buffer[offset + 3] = (byte)(0xff & toEncode);
    }
}

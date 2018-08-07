package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import org.aion.kernel.TransactionContext;


/**
 * Manages the organization of a DApp's root classes serialized shape as well as how to kick-off the serialization/deserialization
 * operations of the entire object graph (since both operations start at the root classes defined within the DApp).
 * Only the class statics and maybe a few specialized instances will be populated here.  The graph is limited by installing instance
 * stubs into fields pointing at objects.
 * 
 * We will store the data for all classes in a single storage key to avoid small IO operations when they are never used partially.
 * 
 * This class was originally just used to house the top-level calls related to serializing and deserializing a DApp but now it also
 * contains information relating to the DApp, in order to accomplish this.
 * Specifically, it now contains the ClassLoader, information about the class instances, and the cache of any reflection data.
 * NOTE:  It does NOT contain any information about the data currently stored within the Class objects associated with the DApp, nor
 * does it have any information about persisted aspects of the DApp (partly because it doesn't know anything about storage versioning).
 */
public class LoadedDApp {
    private final ClassLoader loader;
    private final byte[] address;
    private final List<Class<?>> classes;
    // Note that this fieldCache is populated by the calls to ReflectionStructureCodec.
    private final Map<Class<?>, Field[]> fieldCache;

    /**
     * Creates the LoadedDApp to represent the classes related to DApp at address.
     * 
     * @param loader The class loader to look up shape.
     * @param address The address of the contract.
     * @param classes The list of classes to populate (order must always be the same).
     */
    public LoadedDApp(ClassLoader loader, byte[] address, List<Class<?>> classes) {
        this.loader = loader;
        this.address = address;
        this.classes = classes;
        this.fieldCache = new HashMap<>();
    }

    /**
     * Populates the statics of the DApp classes with the primitives and instance stubs described by the on-disk data.
     * 
     * @param transactionContext The kernel storage API.
     */
    public void populateClassStaticsFromStorage(TransactionContext transactionContext) {
        // We will create the field populator to build objects with the correct canonicalizing caches.
        CacheAwareFieldPopulator populator = new CacheAwareFieldPopulator(this.loader);
        // Create the codec which will make up the long-lived deserialization approach, within the system.
        ReflectionStructureCodec codec = new ReflectionStructureCodec(this.fieldCache, populator, transactionContext, this.address, 0);
        // The populator needs to know to attach the codec, itself, as the IDeserializer of new instances.
        populator.setDeserializer(codec);
        
        // Extract the raw data for the class statics.
        byte[] rawData = transactionContext.getStorage(this.address, StorageKeys.CLASS_STATICS);
        StreamingPrimitiveCodec.Decoder decoder = StreamingPrimitiveCodec.buildDecoder(rawData);
        
        // We will populate the classes, in-order (the order of the serialization/deserialization must always be the same).
        for (Class<?> clazz : this.classes) {
            codec.deserializeClass(decoder, clazz);
        }
    }

    /**
     * Serializes the static fields of the DApp classes and stores them on disk.
     * 
     * @param nextInstanceId The next instanceId to assign to an object which needs to be serialized.
     * @param transactionContext The kernel storage API.
     */
    public long saveClassStaticsToStorage(long nextInstanceId, TransactionContext transactionContext) {
        // Build the encoder.
        ReflectionStructureCodec codec = new ReflectionStructureCodec(this.fieldCache, null, transactionContext, this.address, nextInstanceId);
        StreamingPrimitiveCodec.Encoder encoder = StreamingPrimitiveCodec.buildEncoder();
        
        // Create the queue of instances reachable from here and consumer abstraction.
        Queue<org.aion.avm.shadow.java.lang.Object> instancesToWrite = new LinkedList<>();
        Consumer<org.aion.avm.shadow.java.lang.Object> instanceSink = new Consumer<>() {
            @Override
            public void accept(org.aion.avm.shadow.java.lang.Object t) {
                instancesToWrite.add(t);
            }};
        
        // We will serialize the classes, in-order (the order of the serialization/deserialization must always be the same).
        for (Class<?> clazz : this.classes) {
            codec.serializeClass(encoder, clazz, instanceSink);
        }
        
        // Save the raw bytes.
        byte[] rawData = encoder.toBytes();
        transactionContext.putStorage(this.address, StorageKeys.CLASS_STATICS, rawData);
        
        // Now, drain the queue.
        while (!instancesToWrite.isEmpty()) {
            org.aion.avm.shadow.java.lang.Object instance = instancesToWrite.poll();
            codec.serializeInstance(instance, instanceSink);
        }
        
        // We need to pull out the nextInstanceId so that it can be set in the codec, when we are next invoked.
        return codec.getNextInstanceId();
    }
}
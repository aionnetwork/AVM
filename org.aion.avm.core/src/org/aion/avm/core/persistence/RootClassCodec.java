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
 * Manages the organization of a contract's root classes serialized shape as well as how to kick-off the serialization/deserialization
 * operations of the entire object graph (since both operations start at the root classes defined within the contract).
 * Only the class statics and maybe a few specialized instances will be populated here.  The graph is limited by installing instance
 * stubs into fields pointing at objects.
 * 
 * We will store the data for all classes in a single storage key to avoid small IO operations when they are never used partially.
 */
public class RootClassCodec {
    /**
     * Populates the statics of the given classes with the primitives and instance stubs described by the on-disk data.
     * 
     * @param loader The class loader to look up shape.
     * @param cb The kernel storage API.
     * @param address The address of the contract.
     * @param classes The list of classes to populate (order must always be the same).
     */
    public static void populateClassStaticsFromStorage(ClassLoader loader, TransactionContext cb, byte[] address, List<Class<?>> classes) {
        // We will create the field populator to build objects with the correct canonicalizing caches.
        CacheAwareFieldPopulator populator = new CacheAwareFieldPopulator(loader);
        // Create the codec which will make up the long-lived deserialization approach, within the system.
        // TODO:  This fieldCache needs to be part of something longer-lived.
        Map<Class<?>, Field[]> fieldCache = new HashMap<>();
        ReflectionStructureCodec codec = new ReflectionStructureCodec(fieldCache, populator, cb, address, 0);
        // The populator needs to know to attach the codec, itself, as the IDeserializer of new instances.
        populator.setDeserializer(codec);
        
        // Extract the raw data for the class statics.
        byte[] rawData = cb.getStorage(address, StorageKeys.CLASS_STATICS);
        StreamingPrimitiveCodec.Decoder decoder = StreamingPrimitiveCodec.buildDecoder(rawData);
        
        // We will populate the classes, in-order (the order of the serialization/deserialization must always be the same).
        for (Class<?> clazz : classes) {
            codec.deserializeClass(decoder, clazz);
        }
    }

    /**
     * Serializes the static fields of the given classes and stores them on disk.
     * 
     * @param nextInstanceId The next instanceId to assign to an object which needs to be serialized.
     * @param cb The kernel storage API.
     * @param address The address of the contract.
     * @param classes The list of classes to save (order must always be the same).
     * @return The new nextInstanceId to save for the next invocation.
     */
    public static long saveClassStaticsToStorage(long nextInstanceId, TransactionContext cb, byte[] address, List<Class<?>> classes) {
        // Build the encoder.
        // TODO:  This fieldCache needs to be part of something longer-lived.
        Map<Class<?>, Field[]> fieldCache = new HashMap<>();
        ReflectionStructureCodec codec = new ReflectionStructureCodec(fieldCache, null, cb, address, nextInstanceId);
        StreamingPrimitiveCodec.Encoder encoder = StreamingPrimitiveCodec.buildEncoder();
        
        // Create the queue of instances reachable from here and consumer abstraction.
        Queue<org.aion.avm.shadow.java.lang.Object> instancesToWrite = new LinkedList<>();
        Consumer<org.aion.avm.shadow.java.lang.Object> instanceSink = new Consumer<>() {
            @Override
            public void accept(org.aion.avm.shadow.java.lang.Object t) {
                instancesToWrite.add(t);
            }};
        
        // We will serialize the classes, in-order (the order of the serialization/deserialization must always be the same).
        for (Class<?> clazz : classes) {
            codec.serializeClass(encoder, clazz, instanceSink);
        }
        
        // Save the raw bytes.
        byte[] rawData = encoder.toBytes();
        cb.putStorage(address, StorageKeys.CLASS_STATICS, rawData);
        
        // Now, drain the queue.
        while (!instancesToWrite.isEmpty()) {
            org.aion.avm.shadow.java.lang.Object instance = instancesToWrite.poll();
            codec.serializeInstance(instance, instanceSink);
        }
        
        // We need to pull out the nextInstanceId so that it can be set in the codec, when we are next invoked.
        return codec.getNextInstanceId();
    }
}

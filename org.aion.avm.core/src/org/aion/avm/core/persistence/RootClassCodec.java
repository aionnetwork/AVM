package org.aion.avm.core.persistence;

import java.util.List;

import org.aion.kernel.KernelApi;


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
    public static void populateClassStaticsFromStorage(ClassLoader loader, KernelApi cb, byte[] address, List<Class<?>> classes) {
        // Extract the raw data.
        byte[] rawData = cb.getStorage(address, StorageKeys.CLASS_STATICS);
        ReflectionStructureCodec codec = new ReflectionStructureCodec(loader, 0);
        StreamingPrimitiveCodec.Decoder decoder = StreamingPrimitiveCodec.buildDecoder(rawData);
        
        // We will populate the classes, in-order (the order of the serialization/deserialization must always be the same).
        for (Class<?> clazz : classes) {
            codec.deserialize(decoder, clazz, null);
        }
    }

    /**
     * Serializes the static fields of the given classes and stores them on disk.
     * 
     * @param loader The class loader to look up shape.
     * @param nextInstanceId The next instanceId to assign to an object which needs to be serialized.
     * @param cb The kernel storage API.
     * @param address The address of the contract.
     * @param classes The list of classes to save (order must always be the same).
     */
    public static void saveClassStaticsToStorage(ClassLoader loader, long nextInstanceId, KernelApi cb, byte[] address, List<Class<?>> classes) {
        // Build the encoder.
        ReflectionStructureCodec codec = new ReflectionStructureCodec(loader, nextInstanceId);
        StreamingPrimitiveCodec.Encoder encoder = StreamingPrimitiveCodec.buildEncoder();
        
        // We will serialize the classes, in-order (the order of the serialization/deserialization must always be the same).
        for (Class<?> clazz : classes) {
            codec.serialize(encoder, clazz, null);
        }
        
        // Save the raw bytes.
        byte[] rawData = encoder.toBytes();
        cb.putStorage(address, StorageKeys.CLASS_STATICS, rawData);
    }
}

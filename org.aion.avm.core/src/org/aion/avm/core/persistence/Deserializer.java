package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.aion.avm.internal.RuntimeAssertionError;


public class Deserializer {
    public static int deserializeEntireGraphAndNextHashCode(ByteBuffer inputBuffer, List<Object> existingObjectIndex, IGlobalResolver resolver, SortedFieldCache cache, IPersistenceNameMapper classNameMapper, Class<?>[] sortedRoots) {
        // We define the storage as big-endian.
        RuntimeAssertionError.assertTrue(ByteOrder.BIG_ENDIAN == inputBuffer.order());
        
        // Deserialization requires that we walk the input data twice, since we need to create all the instances on the first pass and attach them all on the second.
        // So, we skip the hashcode on the first pass.
        inputBuffer.getInt();
        // Create the pre-pass deserializer, just to walk consistently.
        ByteBufferObjectDeserializer prePassDeserializer = new ByteBufferObjectDeserializer(inputBuffer, null, cache, resolver, classNameMapper);
        // Now, we need walk the statics, but only to advance the cursor through the buffer (since we will read the same data, but just won't be able to find the instances).
        deserializeClassStatics(prePassDeserializer, cache, sortedRoots);
        
        // Now, walk the rest of the data, deserializing each object, but this is just to find out the instance types and advance through the buffer, consistently.
        List<Object> instanceList = createAllInstancesFromBuffer(prePassDeserializer, existingObjectIndex, cache, classNameMapper);
        
        // Now, we have enough information to build the graph.
        // Reset the buffer and read it again.
        inputBuffer.rewind();
        
        int nextHashCode = inputBuffer.getInt();
        // Create te real deserializer (this one has the instance list for building the connections from that index).
        ByteBufferObjectDeserializer objectDeserializer = new ByteBufferObjectDeserializer(inputBuffer, instanceList, cache, resolver, classNameMapper);
        
        // Next, we deserialize all the class statics for the user's classes.
        deserializeClassStatics(objectDeserializer, cache, sortedRoots);
        
        // We can now use the real deserializer to populate all instance fields and connections.
        populateAllInstancesFromBuffer(objectDeserializer, instanceList, cache);
        
        return nextHashCode;
    }



    private static void deserializeClassStatics(ByteBufferObjectDeserializer objectDeserializer, SortedFieldCache cache, Class<?>[] sortedRoots) {
        for (Class<?> clazz : sortedRoots) {
            deserializeOneClass(objectDeserializer, cache, clazz);
        }
    }

    private static void deserializeOneClass(ByteBufferObjectDeserializer objectDeserializer, SortedFieldCache cache, Class<?> clazz) {
        // Note that we don't serialize the class name - the roots are in the same sorted order for reading and writing.
        // First, we serialize the constants.
        Field[] constants = cache.getConstantFields(clazz);
        deserializeFieldsForClass(objectDeserializer, constants);
        
        // Then, we serialize the user-defined static fields.
        Field[] fields = cache.getUserStaticFields(clazz);
        deserializeFieldsForClass(objectDeserializer, fields);
    }

    private static List<Object> createAllInstancesFromBuffer(ByteBufferObjectDeserializer objectDeserializer, List<Object> existingObjectIndex, SortedFieldCache cache, IPersistenceNameMapper classNameMapper) {
        Method deserializeSelfMethod = cache.getDeserializeSelfMethod();
        List<Object> instanceList = new ArrayList<>();
        // We want to tell each instance which index we read them as - this is useful in the case of reentrant calls so we can track the
        // instance we can write back into.
        int readIndex = 0;
        
        // Reentrant case:  If this is being called to deserialize back from the CALLEE serialized data to the CALLER graph:
        // 1) We want to see if there is an existing object we should copy into (since these could be referenced from the stack)
        // 2) We need to give any new instances a -1 readIndex, so they correctly show up as new to any meta-caller frames
        boolean isDeserializingIntoCallerObjects = (null != existingObjectIndex);
        
        // We walk the entire buffer, ending when we fall off the end (the exception).
        try {
            boolean keepRunning = true;
            while (keepRunning) {
                // We only expect the error when reading the class name, so only check it there (other cases would be errors).
                String internalClassName = null;
                try {
                    internalClassName = objectDeserializer.readClassName();
                } catch (BufferUnderflowException done) {
                    // This was expected - means we fell off the end of the buffer.
                    keepRunning = false;
                }
                if (keepRunning) {
                    // Note that we might be re-using an old instance (if we are returning from a reentrant call).
                    // Even if there is a different object instance we want to re-use, we still need to create the instance in order to advance the stream.
                    Object instance = (isDeserializingIntoCallerObjects && (null != existingObjectIndex.get(readIndex)))
                            ? existingObjectIndex.get(readIndex)
                            : cache.getNewInstance(internalClassName, isDeserializingIntoCallerObjects ? -1 : readIndex);
                    deserializeSelfMethod.invoke(instance, null, objectDeserializer);
                    instanceList.add(instance);
                    readIndex += 1;
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // Reflection errors can't happen since we set this up so we could access it.
            throw RuntimeAssertionError.unexpected(e);
        }
        return instanceList;
    }

    private static void populateAllInstancesFromBuffer(ByteBufferObjectDeserializer objectDeserializer, List<Object> instanceList, SortedFieldCache cache) {
        Method deserializeSelfMethod = cache.getDeserializeSelfMethod();
        
        // We walk the entire instanceList, assuming that it is the full content of the storage.
        try {
            for (Object instance : instanceList) {
                // Read the class name, but just to advance the cursor.
                objectDeserializer.readClassName();
                // Now, deserialize the instance.
                deserializeSelfMethod.invoke(instance, null, objectDeserializer);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // Reflection errors can't happen since we set this up so we could access it.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    private static void deserializeFieldsForClass(ByteBufferObjectDeserializer objectDeserializer, Field[] fields) {
        try {
            for (Field field : fields) {
                // We need to crack the type, here.
                Class<?> type = field.getType();
                if (boolean.class == type) {
                    boolean val = objectDeserializer.readBoolean();
                    field.setBoolean(null, val);
                } else if (byte.class == type) {
                    byte val = objectDeserializer.readByte();
                    field.setByte(null, val);
                } else if (short.class == type) {
                    short val = objectDeserializer.readShort();
                    field.setShort(null, val);
                } else if (char.class == type) {
                    char val = objectDeserializer.readChar();
                    field.setChar(null, val);
                } else if (int.class == type) {
                    int val = objectDeserializer.readInt();
                    field.setInt(null, val);
                } else if (float.class == type) {
                    float val = objectDeserializer.readFloat();
                    field.setFloat(null, val);
                } else if (long.class == type) {
                    long val = objectDeserializer.readLong();
                    field.setLong(null, val);
                } else if (double.class == type) {
                    double val = objectDeserializer.readDouble();
                    field.setDouble(null, val);
                } else {
                    Object val = objectDeserializer.readObject();
                    field.set(null, val);
                }
            }
        } catch (IllegalAccessException e) {
            // Reflection errors can't happen since we set this up so we could access it.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
}

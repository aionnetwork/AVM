package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IObjectDeserializer;
import org.aion.avm.internal.IObjectSerializer;
import org.aion.avm.internal.RuntimeAssertionError;
import org.aion.kernel.KernelApi;


/**
 * Responsible for the high-level reading/writing of actual object instances.  Internally, uses reflection to determine how to make sense
 * of object data.
 * This class is also responsible for much of the partial graph management:  Creating and tracking stub instances and knowing how to assign
 * instanceIds to objects being serialized.
 * 
 * See issue-127 for more information.
 * 
 * TODO:  Test the benefit of caching the reflected field access instances for user-defined classes (there is a great deal of re-use).
 */
public class ReflectionStructureCodec implements IDeserializer {
    private final ClassLoader classLoader;
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> instanceStubMap;
    private final KernelApi kernel;
    private final byte[] address;
    // We cache the method entry-point we will use to invoke the load operation on any instance.
    private final Method deserializeSelf;
    private final Method serializeSelf;
    // We only hold the deserializerField because we need to check if it is null when traversing the graph for objects to serialize.
    private final Field deserializerField;
    private final Field instanceIdField;
    private long nextInstanceId;

    public ReflectionStructureCodec(ClassLoader classLoader, KernelApi kernel, byte[] address, long nextInstanceId) {
        this.classLoader = classLoader;
        this.instanceStubMap = new HashMap<>();
        this.kernel = kernel;
        this.address = address;
        try {
            this.deserializeSelf = org.aion.avm.shadow.java.lang.Object.class.getDeclaredMethod("deserializeSelf", IObjectDeserializer.class);
            this.serializeSelf = org.aion.avm.shadow.java.lang.Object.class.getDeclaredMethod("serializeSelf", IObjectSerializer.class);
            this.deserializerField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("deserializer");
            this.deserializerField.setAccessible(true);
            this.instanceIdField = org.aion.avm.shadow.java.lang.Object.class.getDeclaredField("instanceId");
            this.instanceIdField.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException | NoSuchMethodException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
        this.nextInstanceId = nextInstanceId;
    }

    public void serialize(StreamingPrimitiveCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) {
        try {
            safeSerialize(encoder, clazz, object);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public void deserialize(StreamingPrimitiveCodec.Decoder decoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) {
        try {
            safeDeserialize(decoder, clazz, object);
        } catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }


    private void safeSerialize(StreamingPrimitiveCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) throws IllegalArgumentException, IllegalAccessException {
        // This method is recursive since we are looking to find the root where we need to begin:
        // -for Objects this is the shadow Object
        // -for interfaces, it is when we hit null (since they have no super-class but the interface may have statics)
        boolean isAtTop = (org.aion.avm.shadow.java.lang.Object.class == clazz)
                || (null == clazz);
        if (isAtTop) {
            // Perform the special logic for the root object.
            // There are no shadow static fields of note.
            if (null != object) {
                // Note that this is a temporary stop-gap to show how the serialization system crosses the boundary:
                // We call serializeSelf so that the Object can encode its hashCode but this will be further
                // generalized, later.
                SingleInstanceSerializer objectSerializer = new SingleInstanceSerializer(encoder);
                try {
                    this.serializeSelf.invoke(object, objectSerializer);
                } catch (InvocationTargetException e) {
                    // Runtime errors in the serialization path are not recoverable.
                    throw RuntimeAssertionError.unexpected(e);
                }
            }
        } else {
            // Call the superclass and serialize this class.
            safeSerialize(encoder, clazz.getSuperclass(), object);
            safeSerializeOneClass(encoder, clazz, object);
        }
    }

    private void safeSerializeOneClass(StreamingPrimitiveCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) throws IllegalArgumentException, IllegalAccessException {
        // Note that we serialize objects and classes the same way, just looking for instance versus static fields.
        int expectedModifier = (null == object)
                ? Modifier.STATIC
                : 0x0;
        
        for (Field field : clazz.getDeclaredFields()) {
            if (expectedModifier == (Modifier.STATIC & field.getModifiers())) {
                // Note that this "setAccessible" will fail if the module is not properly "open".
                field.setAccessible(true);
                Class<?> type = field.getType();
                if (boolean.class == type) {
                    boolean val = field.getBoolean(object);
                    encoder.encodeByte((byte) (val ? 0x1 : 0x0));
                } else if (byte.class == type) {
                    byte val = field.getByte(object);
                    encoder.encodeByte(val);
                } else if (short.class == type) {
                    short val = field.getShort(object);
                    encoder.encodeShort(val);
                } else if (char.class == type) {
                    char val = field.getChar(object);
                    encoder.encodeChar(val);
                } else if (int.class == type) {
                    int val = field.getInt(object);
                    encoder.encodeInt(val);
                } else if (float.class == type) {
                    float actual = field.getFloat(object);
                    int val = Float.floatToIntBits(actual);
                    encoder.encodeInt(val);
                } else if (long.class == type) {
                    long val = field.getLong(object);
                    encoder.encodeLong(val);
                } else if (double.class == type) {
                    double actual = field.getDouble(object);
                    long val = Double.doubleToLongBits(actual);
                    encoder.encodeLong(val);
                } else {
                    // This should be a shadow object.
                    if (!org.aion.avm.shadow.java.lang.Object.class.isAssignableFrom(type)) {
                        throw new RuntimeAssertionError("Attempted to encode non-shadow object: " + type);
                    }
                    // Shape:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
                    // Null:  (int)0.
                    org.aion.avm.shadow.java.lang.Object contents = (org.aion.avm.shadow.java.lang.Object)field.get(object);
                    if (null != contents) {
                        String typeName = contents.getClass().getName();
                        byte[] utf8Name = typeName.getBytes(StandardCharsets.UTF_8);
                        encoder.encodeInt(utf8Name.length);
                        encoder.encodeBytes(utf8Name);
                        long instanceId = this.instanceIdField.getLong(contents);
                        if (0 == instanceId) {
                            // We have to assign this.
                            instanceId = this.nextInstanceId;
                            this.nextInstanceId += 1;
                            this.instanceIdField.setLong(contents, instanceId);
                        }
                        encoder.encodeLong(instanceId);
                    } else {
                        encoder.encodeInt(0);
                    }
                }
            }
        } 
    }

    private void safeDeserialize(StreamingPrimitiveCodec.Decoder decoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // This method is recursive since we are looking to find the root where we need to begin:
        // -for Objects this is the shadow Object
        // -for interfaces, it is when we hit null (since they have no super-class but the interface may have statics)
        boolean isAtTop = (org.aion.avm.shadow.java.lang.Object.class == clazz)
                || (null == clazz);
        if (isAtTop) {
            // Perform the special logic for the root object.
            // There are no shadow static fields of note.
            if (null != object) {
                // Note that this is a temporary stop-gap to show how the deserialization system crosses the boundary:
                // We call deserializeSelf so that the Object can decode its hashCode but this will be further
                // generalized, later.
                SingleInstanceDeserializer objectDeserializer = new SingleInstanceDeserializer(decoder);
                try {
                    this.deserializeSelf.invoke(object, objectDeserializer);
                } catch (InvocationTargetException e) {
                    // Runtime errors in the serialization path are not recoverable.
                    throw RuntimeAssertionError.unexpected(e);
                }
            }
        } else {
            // Call the superclass and serialize this class.
            safeDeserialize(decoder, clazz.getSuperclass(), object);
            safeDeserializeOneClass(decoder, clazz, object);
        }
    }

    private void safeDeserializeOneClass(StreamingPrimitiveCodec.Decoder decoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // Note that we deserialize objects and classes the same way, just looking for instance versus static fields.
        int expectedModifier = (null == object)
                ? Modifier.STATIC
                : 0x0;
        
        for (Field field : clazz.getDeclaredFields()) {
            if (expectedModifier == (Modifier.STATIC & field.getModifiers())) {
                // Note that this "setAccessible" will fail if the module is not properly "open".
                field.setAccessible(true);
                Class<?> type = field.getType();
                if (boolean.class == type) {
                    boolean val = (0x1 == decoder.decodeByte());
                    field.setBoolean(object, val);
                } else if (byte.class == type) {
                    byte val = decoder.decodeByte();
                    field.setByte(object, val);
                } else if (short.class == type) {
                    short val = decoder.decodeShort();
                    field.setShort(object, val);
                } else if (char.class == type) {
                    char val = decoder.decodeChar();
                    field.setChar(object, val);
                } else if (int.class == type) {
                    int val = decoder.decodeInt();
                    field.setInt(object, val);
                } else if (float.class == type) {
                    int val = decoder.decodeInt();
                    float actual = Float.intBitsToFloat(val);
                    field.setFloat(object, actual);
                } else if (long.class == type) {
                    long val = decoder.decodeLong();
                    field.setLong(object, val);
                } else if (double.class == type) {
                    long val = decoder.decodeLong();
                    double actual = Double.longBitsToDouble(val);
                    field.setDouble(object, actual);
                } else {
                    
                    // This should be a shadow object.
                    if (!org.aion.avm.shadow.java.lang.Object.class.isAssignableFrom(type)) {
                        throw new RuntimeAssertionError("Attempted to decode non-shadow object: " + type);
                    }
                    // Shape:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
                    // Null:  (int)0.
                    org.aion.avm.shadow.java.lang.Object instanceToStore = null;
                    int length = decoder.decodeInt();
                    if (0 != length) {
                        // Decode this (note that we will need to look up the instance stub).
                        byte[] utf8Name = new byte[length];
                        decoder.decodeBytesInto(utf8Name);
                        String className = new String(utf8Name, StandardCharsets.UTF_8);
                        long instanceId = decoder.decodeLong();
                        instanceToStore = findInstanceStub(className, instanceId);
                    }
                    field.set(object, instanceToStore);
                }
            }
        } 
    }

    private org.aion.avm.shadow.java.lang.Object findInstanceStub(String className, long instanceId) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        org.aion.avm.shadow.java.lang.Object stub = this.instanceStubMap.get(instanceId);
        if (null == stub) {
            // Create the new stub and put it in the map.
            Class<?> contentClass = this.classLoader.loadClass(className);
            // NOTE:  This line is why all our shadow objects and all the transformed user code needs an empty constructor.
            stub = (org.aion.avm.shadow.java.lang.Object)contentClass.getConstructor(IDeserializer.class, long.class).newInstance(this, instanceId);
            this.instanceStubMap.put(instanceId, stub);
        }
        return stub;
    }

    @Override
    public void startDeserializeInstance(org.aion.avm.shadow.java.lang.Object instance, long instanceId) {
        // This instance cannot be 0 since that implies we are deserializing a new object.
        // (we also want to define negative numbers as something special, in the future).
        RuntimeAssertionError.assertTrue(instanceId > 0);
        
        // This is called from the shadow Object "lazyLoad()".  We just want to load the data for this instance and then create the deserializer to pass back to them.
        byte[] rawData = this.kernel.getStorage(address, StorageKeys.forInstance(instanceId));
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(rawData);
        deserialize(decoder, instance.getClass(), instance);
    }
}

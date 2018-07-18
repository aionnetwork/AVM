package org.aion.avm.core.persistence;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
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
 * Note that the "automatic" entry-points for both serialization and deserialization are also here (see issue-132 for more details on that).
 * 
 * TODO:  Test the benefit of caching the reflected field access instances for user-defined classes (there is a great deal of re-use).
 */
public class ReflectionStructureCodec implements IDeserializer, SingleInstanceDeserializer.IAutomatic, SingleInstanceSerializer.IAutomatic {
    // This DEBUG flag will probably evolve into something different, later on, but is now provided for greater visibility during testing.
    private static boolean DEBUG = false;

    private static IDeserializer DONE_MARKER = new IDeserializer() {
        @Override
        public void startDeserializeInstance(org.aion.avm.shadow.java.lang.Object instance, long instanceId) {
        }};
    // There are no constants for stub descriptors greater than 0 since that is a string length field.
    private static final int STUB_DESCRIPTOR_NULL = 0;
    private static final int STUB_DESCRIPTOR_CONSTANT = -1;
    private static final int STUB_DESCRIPTOR_CLASS = -2;

    private final ClassLoader classLoader;
    // All keys in instanceStubMap are positive values.
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> instanceStubMap;
    // All keys in shadowConstantMap are negative values.
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> shadowConstantMap;
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
        this.shadowConstantMap = NodeEnvironment.singleton.getConstantMap();
        this.kernel = kernel;
        this.address = address;
        try {
            this.deserializeSelf = org.aion.avm.shadow.java.lang.Object.class.getDeclaredMethod("deserializeSelf", java.lang.Class.class, IObjectDeserializer.class);
            this.serializeSelf = org.aion.avm.shadow.java.lang.Object.class.getDeclaredMethod("serializeSelf", java.lang.Class.class, IObjectSerializer.class, Consumer.class);
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

    public long getNextInstanceId() {
        return this.nextInstanceId;
    }

    public void serializeClass(StreamingPrimitiveCodec.Encoder encoder, Class<?> clazz, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        if (DEBUG) {
            System.out.println("SERIALIZE CLASS: " + clazz.getName());
        }
        try {
            // Note that only direct class statics are serialized with the class:  superclass statics are saved in the superclass.
            // Hence, just call the serializer, directly.
            safeSerializeOneClass(encoder, clazz, null, nextObjectQueue);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    public void deserializeClass(StreamingPrimitiveCodec.Decoder decoder, Class<?> clazz) {
        if (DEBUG) {
            System.out.println("DESERIALIZE CLASS: " + clazz.getName());
        }
        try {
            // Note that only direct class statics are deserialized with the class:  superclass statics are loaded from the superclass.
            // Hence, just call the deserializer, directly.
            safeDeserializeOneClass(decoder, clazz, null);
        } catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }


    private void safeSerialize(StreamingPrimitiveCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) throws IllegalArgumentException, IllegalAccessException {
        // This method is recursive since we are looking to find the root where we need to begin:
        // -for Objects this is the shadow Object
        // -for interfaces, it is when we hit null (since they have no super-class but the interface may have statics)
        boolean isAtTop = (org.aion.avm.shadow.java.lang.Object.class == clazz)
                || (null == clazz);
        if (isAtTop) {
            // This is the root so we want to terminate here.
            // There are no statics in this class and we have no automatic decoding of any of its instance variables.
        } else if (clazz == firstManualClass) {
            // We CANNOT deserialize this, since it is the first manual class, but our caller can, so pass null as the manual class to them.
            safeSerialize(encoder, clazz.getSuperclass(), object, null, nextObjectQueue);
        } else {
            // Call the superclass and serialize this class.
            safeSerialize(encoder, clazz.getSuperclass(), object, firstManualClass, nextObjectQueue);
            // If we got null as the first manual class, we can automatically deserialize.
            if (null == firstManualClass) {
                safeSerializeOneClass(encoder, clazz, object, nextObjectQueue);
            }
        }
    }

    private void safeSerializeOneClass(StreamingPrimitiveCodec.Encoder encoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) throws IllegalArgumentException, IllegalAccessException {
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
                    org.aion.avm.shadow.java.lang.Object contents = (org.aion.avm.shadow.java.lang.Object)field.get(object);
                    // This should be a shadow object.
                    if (contents != null && !org.aion.avm.shadow.java.lang.Object.class.isAssignableFrom(contents.getClass())) {
                        throw new RuntimeAssertionError("Attempted to encode non-shadow object: " + contents.getClass());
                    }
                    // Shape:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
                    // Null:  (int)0.
                    deflateInstanceAsStub(encoder, contents, nextObjectQueue);
                }
            }
        } 
    }

    private void deflateInstanceAsStub(StreamingPrimitiveCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object contents, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        try {
            // See issue-147 for more information regarding this interpretation:
            // - null: (int)0.
            // - -1: (int)-1, (long) instanceId (of constant - negative).
            // - -2: (int)-2, (int) buffer length, (n) UTF-8 class name buffer
            // - >0:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
            // Reason for order of evaluation:
            // - null goes first, since it is easy to detect on either side (and probably a common case).
            // - constants go second since they are arbitrary objects, including some Class objects, and already have the correct instanceId.
            // - Classes go third since we will we don't to look at their instanceIds (we will see the 0 and take the wrong action).
            // - normal references go last (includes those with 0 or >0 instanceIds).
            if (null == contents) {
                // Null has the least data.
                encoder.encodeInt(STUB_DESCRIPTOR_NULL);
            } else {
                // Check the instanceId to see if this is a special-case.
                long instanceId = this.instanceIdField.getLong(contents);
                if (instanceId < 0) {
                    // Constants.
                    encoder.encodeInt(STUB_DESCRIPTOR_CONSTANT);
                    // Write the constant instanceId.
                    encoder.encodeLong(instanceId);
                } else if (contents instanceof org.aion.avm.shadow.java.lang.Class) {
                    // Non-constant Class reference.
                    encoder.encodeInt(STUB_DESCRIPTOR_CLASS);
                    
                    // Get the class name.
                    String className = ((org.aion.avm.shadow.java.lang.Class<?>)contents).getRealClass().getName();
                    byte[] utf8Name = className.getBytes(StandardCharsets.UTF_8);
                    
                    // Write the length and the bytes.
                    encoder.encodeInt(utf8Name.length);
                    encoder.encodeBytes(utf8Name);
                } else {
                    // Common case of a normal reference (may or may not already have an instanceId assigned.
                    // This a normal reference (although might need an instanceId assigned).
                    String typeName = contents.getClass().getName();
                    byte[] utf8Name = typeName.getBytes(StandardCharsets.UTF_8);
                    if (0 == instanceId) {
                        // We have to assign this.
                        instanceId = this.nextInstanceId;
                        this.nextInstanceId += 1;
                        this.instanceIdField.setLong(contents, instanceId);
                    }
                    
                    // Now, serialize the standard form.
                    encoder.encodeInt(utf8Name.length);
                    encoder.encodeBytes(utf8Name);
                    encoder.encodeLong(instanceId);
                    
                    if (DEBUG) {
                        System.out.println(" WRITESTUB(" + instanceId + "): " + typeName);
                    }
                    
                    // If this instance has been loaded, set it to not loaded and add it to the queue.
                    if (null == this.deserializerField.get(contents)) {
                        this.deserializerField.set(contents, DONE_MARKER);
                        nextObjectQueue.accept(contents);
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // If there are any problems with this access, we must have resolved it before getting to this point.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    private void safeDeserialize(StreamingPrimitiveCodec.Decoder decoder, Class<?> clazz, org.aion.avm.shadow.java.lang.Object object, Class<?> firstManualClass) throws IllegalArgumentException, IllegalAccessException, ClassNotFoundException, InstantiationException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // This method is recursive since we are looking to find the root where we need to begin:
        // -for Objects this is the shadow Object
        // -for interfaces, it is when we hit null (since they have no super-class but the interface may have statics)
        boolean isAtTop = (org.aion.avm.shadow.java.lang.Object.class == clazz)
                || (null == clazz);
        if (isAtTop) {
            // This is the root so we want to terminate here.
            // There are no statics in this class and we have no automatic decoding of any of its instance variables.
        } else if (clazz == firstManualClass) {
            // We CANNOT deserialize this, since it is the first manual class, but our caller can, so pass null as the manual class to them.
            safeDeserialize(decoder, clazz.getSuperclass(), object, null);
        } else {
            // Call the superclass and serialize this class.
            safeDeserialize(decoder, clazz.getSuperclass(), object, firstManualClass);
            // If we got null as the first manual class, we can automatically deserialize.
            if (null == firstManualClass) {
                safeDeserializeOneClass(decoder, clazz, object);
            }
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
                    // Shape:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
                    // Null:  (int)0.
                    org.aion.avm.shadow.java.lang.Object instanceToStore = inflateStubAsInstance(decoder);
                    field.set(object, instanceToStore);
                }
            }
        } 
    }

    private org.aion.avm.shadow.java.lang.Object inflateStubAsInstance(StreamingPrimitiveCodec.Decoder decoder) {
        try {
            // See issue-147 for more information regarding this interpretation:
            // - null: (int)0.
            // - -1: (int)-1, (long) instanceId (of constant - negative).
            // - -2: (int)-2, (int) buffer length, (n) UTF-8 class name buffer
            // - >0:  (int) buffer length, (n) UTF-8 buffer, (long) instanceId.
            // Reason for order of evaluation:
            // - null goes first, since it is easy to detect on either side (and probably a common case).
            // - constants go second since they are arbitrary objects, including some Class objects, and already have the correct instanceId.
            // - Classes go third since we will we don't to look at their instanceIds (we will see the 0 and take the wrong action).
            // - normal references go last (includes those with 0 or >0 instanceIds).
            org.aion.avm.shadow.java.lang.Object instanceToStore = null;
            int stubDescriptor = decoder.decodeInt();
            if (STUB_DESCRIPTOR_NULL == stubDescriptor) {
                // This is a null object:
                // -nothing else to read.
            } else if (STUB_DESCRIPTOR_CONSTANT == stubDescriptor) {
                // This is a constant reference:
                // -load the constant instance ID.
                long instanceId = decoder.decodeLong();
                // Constants have negative instance IDs.
                RuntimeAssertionError.assertTrue(instanceId < 0);
                
                // Look this up in the constant map.
                instanceToStore = this.shadowConstantMap.get(instanceId);
                // We can't fail to find these.
                RuntimeAssertionError.assertTrue(null != instanceToStore);
            } else if (STUB_DESCRIPTOR_CLASS == stubDescriptor) {
                // This is a reference to a Class.
                // -load the size of the class name.
                int classNameLength = decoder.decodeInt();
                // -load the bytes as a string.
                byte[] utf8Name = new byte[classNameLength];
                decoder.decodeBytesInto(utf8Name);
                String className = new String(utf8Name, StandardCharsets.UTF_8);
                
                // Create an instance of the Class (we rely on the helper since this needs to be interned, per-contract).
                Class<?> jdkClass = Class.forName(className);
                instanceToStore = IHelper.currentContractHelper.get().externalWrapAsClass(jdkClass);
            } else {
                // This is a normal object:
                // -descriptor is type name length.
                int typeNameLength = stubDescriptor;
                // -load that many bytes as the name
                byte[] utf8Name = new byte[typeNameLength];
                decoder.decodeBytesInto(utf8Name);
                String className = new String(utf8Name, StandardCharsets.UTF_8);
                // -load the instanceId.
                long instanceId = decoder.decodeLong();
                
                // This instance might already exist so ask our helper which will instantiate, if need be.
                instanceToStore = findInstanceStub(className, instanceId);
            }
            return instanceToStore;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // If there are any problems with this access, we must have resolved it before getting to this point.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    private org.aion.avm.shadow.java.lang.Object findInstanceStub(String className, long instanceId) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        // We can only call this helper with positive instanceId.
        RuntimeAssertionError.assertTrue(instanceId > 0);
        
        org.aion.avm.shadow.java.lang.Object stub = this.instanceStubMap.get(instanceId);
        if (null == stub) {
            // Create the new stub and put it in the map.
            Class<?> contentClass = this.classLoader.loadClass(className);
            stub = (org.aion.avm.shadow.java.lang.Object)contentClass.getConstructor(IDeserializer.class, long.class).newInstance(this, instanceId);
            this.instanceStubMap.put(instanceId, stub);
            if (DEBUG) {
                System.out.println(" READSTUB(" + instanceId + "): " + className);
            }
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
        if (DEBUG) {
            System.out.println("DESERIALIZE(" + instanceId + ")");
        }
        deserializeInstance(instance, rawData);
    }

    public void serializeInstance(org.aion.avm.shadow.java.lang.Object instance, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectSink) {
        try {
            long instanceId = this.instanceIdField.getLong(instance);
            if (DEBUG) {
                System.out.println("SERIALIZE(" + instanceId + ")");
            }
            byte[] serialized = internalSerializeInstance(instance, nextObjectSink);
            this.kernel.putStorage(this.address, StorageKeys.forInstance(instanceId), serialized);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // If there are any problems with this access, we must have resolved it before getting to this point.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    // Note that this is only public so tests can use it.
    public byte[] internalSerializeInstance(org.aion.avm.shadow.java.lang.Object instance, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectSink) {
        StreamingPrimitiveCodec.Encoder encoder = new StreamingPrimitiveCodec.Encoder();
        SingleInstanceSerializer singleSerializer = new SingleInstanceSerializer(this, encoder);
        try {
            this.serializeSelf.invoke(instance, null, singleSerializer, nextObjectSink);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // If there are any problems with this access, we must have resolved it before getting to this point.
            throw RuntimeAssertionError.unexpected(e);
        }
        return encoder.toBytes();
    }

    // Note that this is only public so tests can use it.
    public void deserializeInstance(org.aion.avm.shadow.java.lang.Object instance, byte[] rawData) {
        // To see it referenced here, we must have saved this data, in the past.
        RuntimeAssertionError.assertTrue(null != rawData);
        
        StreamingPrimitiveCodec.Decoder decoder = new StreamingPrimitiveCodec.Decoder(rawData);
        SingleInstanceDeserializer singleDeserializer = new SingleInstanceDeserializer(this, decoder);
        try {
            this.deserializeSelf.invoke(instance, null, singleDeserializer);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // If there are any problems with this access, we must have resolved it before getting to this point.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void partialAutomaticDeserializeInstance(StreamingPrimitiveCodec.Decoder decoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass) {
        try {
            safeDeserialize(decoder, instance.getClass(), instance, firstManualClass);
        } catch (IllegalArgumentException | IllegalAccessException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void partialAutomaticSerializeInstance(StreamingPrimitiveCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object instance, Class<?> firstManualClass, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        try {
            safeSerialize(encoder, instance.getClass(), instance, firstManualClass, nextObjectQueue);
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
            // This would be a serious mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }

    @Override
    public void encodeAsStub(StreamingPrimitiveCodec.Encoder encoder, org.aion.avm.shadow.java.lang.Object object, Consumer<org.aion.avm.shadow.java.lang.Object> nextObjectQueue) {
        deflateInstanceAsStub(encoder, object, nextObjectQueue);
    }

    @Override
    public org.aion.avm.shadow.java.lang.Object decodeStub(StreamingPrimitiveCodec.Decoder decoder) {
        return inflateStubAsInstance(decoder);
    }
}

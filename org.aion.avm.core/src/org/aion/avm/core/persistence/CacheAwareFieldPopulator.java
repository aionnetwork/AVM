package org.aion.avm.core.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;


/**
 * An implementation of IFieldPopulator which knows how to delegate to the appropriate canonicalizing caches.
 * 
 * Its implementation of all the field interaction methods is the literal version expected but its implementation
 * of the instance creation calls utilize these canonicalizing caches.
 */
public class CacheAwareFieldPopulator implements ReflectionStructureCodec.IFieldPopulator {
    private final ClassLoader loader;
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> instanceStubMap;
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> shadowConstantMap;
    private final Map<String, Constructor<?>> constructorCacheMap;
    private IDeserializer deserializer;

    public CacheAwareFieldPopulator(ClassLoader loader) {
        this.loader = loader;
        this.instanceStubMap = new HashMap<>();
        this.shadowConstantMap = NodeEnvironment.singleton.getConstantMap();
        this.constructorCacheMap = new HashMap<>();
    }
    public void setDeserializer(IDeserializer deserializer) {
        this.deserializer = deserializer;
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object createRegularInstance(String className, long instanceId) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        org.aion.avm.shadow.java.lang.Object stub = this.instanceStubMap.get(instanceId);
        if (null == stub) {
            // Create the new stub and put it in the map.
            Constructor<?> con = getConstructorForClassName(className);
            stub = (org.aion.avm.shadow.java.lang.Object)con.newInstance(this.deserializer, instanceId);
            this.instanceStubMap.put(instanceId, stub);
        }
        return stub;
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object createClass(String className) throws ClassNotFoundException {
        Class<?> jdkClass = Class.forName(className);
        return IHelper.currentContractHelper.get().externalWrapAsClass(jdkClass);
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object createConstant(long instanceId) {
        // Look this up in the constant map.
        return this.shadowConstantMap.get(instanceId);
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object createNull() {
        return null;
    }

    @Override
    public void setBoolean(Field field, org.aion.avm.shadow.java.lang.Object object, boolean val) throws IllegalArgumentException, IllegalAccessException {
        field.setBoolean(object, val);
    }
    @Override
    public void setDouble(Field field, org.aion.avm.shadow.java.lang.Object object, double val) throws IllegalArgumentException, IllegalAccessException {
        field.setDouble(object, val);
    }
    @Override
    public void setLong(Field field, org.aion.avm.shadow.java.lang.Object object, long val) throws IllegalArgumentException, IllegalAccessException {
        field.setLong(object, val);
    }
    @Override
    public void setFloat(Field field, org.aion.avm.shadow.java.lang.Object object, float val) throws IllegalArgumentException, IllegalAccessException {
        field.setFloat(object, val);
    }
    @Override
    public void setInt(Field field, org.aion.avm.shadow.java.lang.Object object, int val) throws IllegalArgumentException, IllegalAccessException {
        field.setInt(object, val);
    }
    @Override
    public void setChar(Field field, org.aion.avm.shadow.java.lang.Object object, char val) throws IllegalArgumentException, IllegalAccessException {
        field.setChar(object, val);
    }
    @Override
    public void setShort(Field field, org.aion.avm.shadow.java.lang.Object object, short val) throws IllegalArgumentException, IllegalAccessException {
        field.setShort(object, val);
    }
    @Override
    public void setByte(Field field, org.aion.avm.shadow.java.lang.Object object, byte val) throws IllegalArgumentException, IllegalAccessException {
        field.setByte(object, val);
    }
    @Override
    public void setObject(Field field, org.aion.avm.shadow.java.lang.Object object, org.aion.avm.shadow.java.lang.Object val) throws IllegalArgumentException, IllegalAccessException {
        field.set(object, val);
    }


    private Constructor<?> getConstructorForClassName(String className) throws ClassNotFoundException, NoSuchMethodException, SecurityException {
        Constructor<?> constructor = this.constructorCacheMap.get(className);
        if (null == constructor) {
            Class<?> contentClass = this.loader.loadClass(className);
            constructor = contentClass.getConstructor(IDeserializer.class, long.class);
            constructor.setAccessible(true);
            this.constructorCacheMap.put(className, constructor);
        }
        return constructor;
    }
}

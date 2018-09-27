package org.aion.avm.core.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.core.persistence.keyvalue.KeyValueNode;
import org.aion.avm.internal.ConstantPersistenceToken;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * An implementation of IFieldPopulator which knows how to delegate to the appropriate canonicalizing caches.
 * 
 * Its implementation of all the field interaction methods is the literal version expected but its implementation
 * of the instance creation calls utilize these canonicalizing caches.
 */
public class CacheAwareFieldPopulator implements ReflectionStructureCodec.IFieldPopulator {
    private final ConstructorCache constructorCache;
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> instanceStubMap;
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> shadowConstantMap;
    private IDeserializer deserializer;

    public CacheAwareFieldPopulator(ClassLoader loader) {
        this.constructorCache = new ConstructorCache(loader);
        this.instanceStubMap = new HashMap<>();
        this.shadowConstantMap = NodeEnvironment.singleton.getConstantMap();
    }
    public void setDeserializer(IDeserializer deserializer) {
        this.deserializer = deserializer;
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object createRegularInstance(String className, IPersistenceToken persistenceToken) {
        long instanceId = ((KeyValueNode)((NodePersistenceToken)persistenceToken).node).getInstanceId();
        org.aion.avm.shadow.java.lang.Object stub = this.instanceStubMap.get(instanceId);
        if (null == stub) {
            // Create the new stub and put it in the map.
            Constructor<?> con = this.constructorCache.getConstructorForClassName(className);
            try {
                stub = (org.aion.avm.shadow.java.lang.Object)con.newInstance(this.deserializer, persistenceToken);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // Any error like this means a serious bug or a fatal mis-configuration.
                throw RuntimeAssertionError.unexpected(e);
            }
            this.instanceStubMap.put(instanceId, stub);
        }
        return stub;
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object createClass(String className) {
        try {
            Class<?> jdkClass = Class.forName(className);
            return IHelper.currentContractHelper.get().externalWrapAsClass(jdkClass);
        } catch (ClassNotFoundException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object createConstant(IPersistenceToken persistenceToken) {
        long instanceId = ((ConstantPersistenceToken)persistenceToken).stableConstantId;
        // Look this up in the constant map.
        return this.shadowConstantMap.get(instanceId);
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object createNull() {
        return null;
    }

    @Override
    public void setBoolean(Field field, org.aion.avm.shadow.java.lang.Object object, boolean val) {
        try {
            field.setBoolean(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setDouble(Field field, org.aion.avm.shadow.java.lang.Object object, double val) {
        try {
            field.setDouble(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setLong(Field field, org.aion.avm.shadow.java.lang.Object object, long val) {
        try {
            field.setLong(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setFloat(Field field, org.aion.avm.shadow.java.lang.Object object, float val) {
        try {
            field.setFloat(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setInt(Field field, org.aion.avm.shadow.java.lang.Object object, int val) {
        try {
            field.setInt(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    @Override
    public void setChar(Field field, org.aion.avm.shadow.java.lang.Object object, char val) {
        try {
            field.setChar(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setShort(Field field, org.aion.avm.shadow.java.lang.Object object, short val) {
        try {
            field.setShort(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setByte(Field field, org.aion.avm.shadow.java.lang.Object object, byte val) {
        try {
            field.setByte(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
    @Override
    public void setObject(Field field, org.aion.avm.shadow.java.lang.Object object, org.aion.avm.shadow.java.lang.Object val) {
        try {
            field.set(object, val);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // Any error like this means a serious bug or a fatal mis-configuration.
            throw RuntimeAssertionError.unexpected(e);
        }
    }
}

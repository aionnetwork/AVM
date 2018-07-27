package org.aion.avm.core.persistence;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.aion.avm.core.NodeEnvironment;
import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IHelper;


/**
 * An implementation of IFieldPopulator which knows how to delegate to the appropriate canonicalizing caches.
 */
public class CacheAwareFieldPopulator implements ReflectionStructureCodec.IFieldPopulator {
    private final ClassLoader loader;
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> instanceStubMap;
    private final Map<Long, org.aion.avm.shadow.java.lang.Object> shadowConstantMap;
    private IDeserializer deserializer;

    public CacheAwareFieldPopulator(ClassLoader loader) {
        this.loader = loader;
        this.instanceStubMap = new HashMap<>();
        this.shadowConstantMap = NodeEnvironment.singleton.getConstantMap();
    }
    public void setDeserializer(IDeserializer deserializer) {
        this.deserializer = deserializer;
    }
    @Override
    public org.aion.avm.shadow.java.lang.Object createRegularInstance(String className, long instanceId) throws ClassNotFoundException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        org.aion.avm.shadow.java.lang.Object stub = this.instanceStubMap.get(instanceId);
        if (null == stub) {
            // Create the new stub and put it in the map.
            Class<?> contentClass = this.loader.loadClass(className);
            Constructor<?> con = contentClass.getConstructor(IDeserializer.class, long.class);
            con.setAccessible(true);
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
}

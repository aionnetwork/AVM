package org.aion.avm.core.persistence;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.aion.avm.internal.IDeserializer;
import org.aion.avm.internal.IPersistenceToken;
import org.aion.avm.internal.RuntimeAssertionError;


/**
 * A cache for constructors resolved via reflection, looked up by name.
 * Specifically, this refers to the (IDeserializer.class, IPersistenceToken.class) constructors used by the persistence path.
 */
public class ConstructorCache {
    private final ClassLoader loader;
    private final Map<String, Constructor<?>> constructorCacheMap;

    public ConstructorCache(ClassLoader loader) {
        this.loader = loader;
        this.constructorCacheMap = new HashMap<>();
    }

    public Constructor<?> getConstructorForClassName(String className) {
        Constructor<?> constructor = this.constructorCacheMap.get(className);
        if (null == constructor) {
            try {
                Class<?> contentClass = this.loader.loadClass(className);
                constructor = contentClass.getConstructor(IDeserializer.class, IPersistenceToken.class);
                constructor.setAccessible(true);
                this.constructorCacheMap.put(className, constructor);
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                // This would mean the generated code was seriously broken, so this is not expected.
                throw RuntimeAssertionError.unexpected(e);
            }
        }
        return constructor;
    }

}

package org.aion.avm.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aion.avm.core.util.Assert;


/**
 * We are slowly evolving this class loader into one we will likely use for the rest of the system, though it
 * was originally used only in tests.
 * This allows a bytecode transformer to be provided and then class bytecode can be injected for both classes
 * which can be rewritten (with the transformer) or loaded as-is.
 */
public class TestClassLoader extends ClassLoader {
    private final Map<String, byte[]> injectedClasses;
    private final Map<String, Class<?>> cache;

    public TestClassLoader(Map<String, byte[]> injectedClasses) {
        this.injectedClasses = Collections.unmodifiableMap(injectedClasses);
        this.cache = new HashMap<>();
    }

    /**
     * A helper which will attempt to load the given resource path as bytes.
     * Any failure in the load is considered fatal.
     * 
     * @param resourcePath The path to this resource, within the parent class loader.
     * @return The bytes
     */
    public static byte[] loadRequiredResourceAsBytes(String resourcePath) {
        InputStream stream = TestClassLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        byte[] raw = null;
        try {
            raw = stream.readAllBytes();
        } catch (IOException e) {
            Assert.unexpected(e);
        }
        return raw;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // NOTE:  We override this, instead of findClass, since we want to circumvent the normal delegation process of class loaders.
        Class<?> result = null;
        boolean shouldResolve = resolve;
        
        // Since we are using our own loadClass, we need our own cache.
        Class<?> cached = this.cache.get(name);
        if (null == cached) {
            byte[] injected = this.injectedClasses.get(name);
            if (null != injected) {
                result = defineClass(name, injected, 0, injected.length);
            } else {
                result = getParent().loadClass(name);
                // We got this from the parent so don't resolve.
                shouldResolve = false;
            }
            
            if (null != result) {
                this.cache.put(name, result);
            }
        } else {
            result = cached;
            // We got this from the cache so don't resolve.
            shouldResolve = false;
        }
        
        if ((null != result) && shouldResolve) {
            resolveClass(result);
        }
        return result;
    }
}

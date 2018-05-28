package org.aion.avm.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.aion.avm.core.util.Assert;


/**
 * We are slowly evolving this class loader into one we will likely use for the rest of the system, though it
 * was originally used only in tests.
 * This allows a bytecode transformer to be provided and then class bytecode can be injected for both classes
 * which can be rewritten (with the transformer) or loaded as-is.
 */
public class TestClassLoader extends ClassLoader {
    private final Map<String, byte[]> classesToRewrite;
    private final Function<byte[], byte[]> loadHandler;
    private final Map<String, byte[]> injectedClasses;
    private final Map<String, Class<?>> cache;

    public TestClassLoader(Function<byte[], byte[]> loadHandler) {
        this.classesToRewrite = new HashMap<>();
        this.loadHandler = loadHandler;
        this.injectedClasses = new HashMap<>();
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

    /**
     * Adds another class to be loaded through the re-writing path.
     * 
     * @param classNameToProvide The name of the class to load (in the style of "org.test.Class$Inner").
     * @param raw The raw bytecode of the class.
     */
    public void addClassForRewrite(String classNameToProvide, byte[] raw) {
        this.classesToRewrite.put(classNameToProvide, raw);
    }

    /**
     * Adds another class to be loaded as-is.
     * 
     * @param classNameToProvide The name of the class to load (in the style of "org.test.Class$Inner").
     * @param raw The raw bytecode of the class.
     */
    public void addClassDirectLoad(String classNameToProvide, byte[] raw) {
        this.injectedClasses.put(classNameToProvide, raw);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // NOTE:  We override this, instead of findClass, since we want to circumvent the normal delegation process of class loaders.
        Class<?> result = null;
        boolean shouldResolve = resolve;
        
        // Since we are using our own loadClass, we need our own cache.
        Class<?> cached = this.cache.get(name);
        if (null == cached) {
            byte[] raw = this.classesToRewrite.get(name);
            if (null != raw) {
                byte[] rewrittten = this.loadHandler.apply(raw);
                result = defineClass(name, rewrittten, 0, rewrittten.length);
            } else if (this.injectedClasses.containsKey(name)) {
                byte[] prepared = this.injectedClasses.get(name);
                result = defineClass(name, prepared, 0, prepared.length);
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

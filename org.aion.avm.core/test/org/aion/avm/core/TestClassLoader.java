package org.aion.avm.core;

import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * We use this classloader, within the test, to get the raw bytes of the test we want to modify and then pass
 * into the ClassRewriter, for the test.
 */
public class TestClassLoader extends ClassLoader {
    private final Map<String, byte[]> classesToRewrite;
    private final Function<byte[], byte[]> loadHandler;
    private final Map<String, byte[]> injectedClasses;
    private final Map<String, Class<?>> cache;

    public TestClassLoader(ClassLoader parent, String classNameToProvide, Function<byte[], byte[]> loadHandler, Map<String, byte[]> injectedClasses) {
        super(parent);
        this.classesToRewrite = new HashMap<>();
        InputStream stream = getParent().getResourceAsStream(classNameToProvide.replaceAll("\\.", "/") + ".class");
        byte[] raw = null;
        try {
            raw = stream.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
        this.classesToRewrite.put(classNameToProvide, raw);
        
        this.loadHandler = loadHandler;
        this.injectedClasses = Collections.unmodifiableMap(injectedClasses);
        this.cache = new HashMap<>();
    }

    /**
     * Adds another class to be loaded through the re-writing path.
     * 
     * @param classNameToProvide The name of the class to load (in the style of "org.test.Class$Inner").
     * @param raw The raw bytecode of the class.
     */
    public void addClass(String classNameToProvide, byte[] raw) {
        this.classesToRewrite.put(classNameToProvide, raw);
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
